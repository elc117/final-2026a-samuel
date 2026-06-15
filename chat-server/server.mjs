import { createServer } from "node:http";
import { Server } from "socket.io";

const port = Number.parseInt(
  process.env.PORT ?? process.env.CHAT_PORT ?? "7001",
  10,
);
const apiUrl = process.env.API_INTERNAL_URL ?? "http://localhost:7000";
const allowedOrigins = (
  process.env.CORS_ALLOWED_ORIGIN ??
  "http://localhost:5173,http://127.0.0.1:5173"
)
  .split(",")
  .map((origin) => origin.trim())
  .filter(Boolean);

const httpServer = createServer((request, response) => {
  if (request.url === "/health") {
    response.writeHead(200, { "Content-Type": "application/json" });
    response.end('{"status":"UP"}');
    return;
  }

  response.writeHead(404);
  response.end();
});

const io = new Server(httpServer, {
  cors: {
    origin: allowedOrigins,
    credentials: true,
  },
});

io.use(async (socket, next) => {
  const token = socket.handshake.auth?.token;
  if (typeof token !== "string" || token.length === 0) {
    next(new Error("Token de acesso ausente."));
    return;
  }

  try {
    const session = await requestApi("/chat/session", token);
    socket.data.accessToken = token;
    socket.data.session = session;
    next();
  } catch {
    next(new Error("Não foi possível autenticar o chat."));
  }
});

io.on("connection", (socket) => {
  const room = `group:${socket.data.session.groupId}`;
  socket.join(room);

  socket.on("chat:send", async (payload, acknowledge) => {
    const respond =
      typeof acknowledge === "function" ? acknowledge : () => undefined;

    try {
      const message = await requestApi(
        "/chat/messages",
        socket.data.accessToken,
        {
          method: "POST",
          body: JSON.stringify({ content: payload?.content }),
        },
      );
      io.to(room).emit("chat:message", message);
      respond({ ok: true });
    } catch (error) {
      respond({
        ok: false,
        message:
          error instanceof Error
            ? error.message
            : "Não foi possível enviar a mensagem.",
      });
    }
  });
});

httpServer.listen(port, () => {
  console.log(`Chat Socket.IO listening on port ${port}`);
});

async function requestApi(path, token, options = {}) {
  const response = await fetch(`${apiUrl}${path}`, {
    ...options,
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
      ...options.headers,
    },
  });

  if (!response.ok) {
    const body = await response.json().catch(() => null);
    const fieldMessage = body?.errors
      ? Object.values(body.errors)[0]
      : undefined;
    throw new Error(
      fieldMessage ??
        body?.message ??
        "Não foi possível concluir a solicitação.",
    );
  }

  return response.json();
}
