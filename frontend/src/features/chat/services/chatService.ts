import { io, type Socket } from "socket.io-client";
import { apiRequest } from "../../../services/apiClient";
import { getAccessToken } from "../../auth/services/accessTokenStore";
import type { CursorPage } from "../../../shared/pagination/CursorPage";

export type ChatSession = {
  groupId: string;
  userCode: string;
  userName: string;
  userImageUrl: string | null;
};

export type ChatMessage = {
  id: string;
  groupId: string;
  authorCode: string;
  authorName: string;
  authorImageUrl: string | null;
  content: string;
  createdAt: string;
};

export type ChatMessagePage = CursorPage<ChatMessage>;

type SendMessageResult = {
  ok: boolean;
  message?: string;
};

export function getChatSession(): Promise<ChatSession> {
  return apiRequest<ChatSession>("/chat/session");
}

export function getChatMessages(
  cursor?: string | null,
  limit = 30,
): Promise<ChatMessagePage> {
  const params = new URLSearchParams({ limit: String(limit) });
  if (cursor) {
    params.set("cursor", cursor);
  }
  return apiRequest<ChatMessagePage>(`/chat/messages?${params.toString()}`);
}

export function createChatSocket(): Socket {
  const socketUrl = import.meta.env.VITE_SOCKET_URL;

  return io(socketUrl || undefined, {
    autoConnect: false,
    auth: (callback) => {
      callback({ token: getAccessToken() });
    },
  });
}

export function sendChatMessage(
  socket: Socket,
  content: string,
): Promise<void> {
  return new Promise((resolve, reject) => {
    socket.timeout(10_000).emit(
      "chat:send",
      { content },
      (timeoutError: Error | null, result?: SendMessageResult) => {
        if (timeoutError) {
          reject(new Error("O chat demorou para responder."));
          return;
        }
        if (!result?.ok) {
          reject(new Error(
            result?.message ?? "Não foi possível enviar a mensagem.",
          ));
          return;
        }
        resolve();
      },
    );
  });
}
