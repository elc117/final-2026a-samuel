import {
  ArrowLeft,
  LoaderCircle,
  MessageCircle,
  Send,
  UserRound,
} from "lucide-react";
import {
  type FormEvent,
  type KeyboardEvent,
  useEffect,
  useRef,
  useState,
} from "react";
import { Link, useNavigate } from "react-router-dom";
import type { Socket } from "socket.io-client";
import { AuthenticatedHeader } from "../../auth/components/AuthenticatedHeader";
import { GroupNavigation } from "../../groups/components/GroupNavigation";
import {
  ApiError,
  getApiErrorMessage,
} from "../../../services/apiClient";
import {
  createChatSocket,
  getChatMessages,
  getChatSession,
  sendChatMessage,
  type ChatMessage,
  type ChatSession,
} from "../services/chatService";

export function ChatPage() {
  const navigate = useNavigate();
  const socketRef = useRef<Socket | null>(null);
  const messagesContainerRef = useRef<HTMLDivElement | null>(null);
  const loadingOlderRef = useRef(false);
  const [session, setSession] = useState<ChatSession | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(false);
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadingOlder, setLoadingOlder] = useState(false);
  const [sending, setSending] = useState(false);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    let socket: Socket | null = null;

    Promise.all([getChatSession(), getChatMessages()])
      .then(([loadedSession, page]) => {
        if (!active) return;

        setSession(loadedSession);
        setMessages([...page.items].reverse());
        setNextCursor(page.nextCursor);
        setHasMore(page.hasMore);

        socket = createChatSocket();
        socketRef.current = socket;
        socket.on("connect", () => setConnected(true));
        socket.on("disconnect", () => setConnected(false));
        socket.on("connect_error", (socketError) => {
          setConnected(false);
          setError(socketError.message);
        });
        socket.on("chat:message", (message: ChatMessage) => {
          setMessages((current) =>
            current.some((item) => item.id === message.id)
              ? current
              : [...current, message],
          );
        });
        socket.connect();
      })
      .catch((requestError: unknown) => {
        if (requestError instanceof ApiError && requestError.status === 401) {
          navigate("/login", { replace: true });
          return;
        }
        if (active) setError(getApiErrorMessage(requestError));
      })
      .finally(() => active && setLoading(false));

    return () => {
      active = false;
      socket?.disconnect();
      socketRef.current = null;
    };
  }, [navigate]);

  useEffect(() => {
    if (!loadingOlderRef.current) {
      const container = messagesContainerRef.current;
      container?.scrollTo({
        top: container.scrollHeight,
        behavior: "smooth",
      });
    }
  }, [messages.length]);

  async function loadOlderMessages() {
    if (!nextCursor || loadingOlder) return;
    loadingOlderRef.current = true;
    setLoadingOlder(true);
    setError("");

    try {
      const page = await getChatMessages(nextCursor);
      setMessages((current) => [
        ...[...page.items].reverse(),
        ...current,
      ]);
      setNextCursor(page.nextCursor);
      setHasMore(page.hasMore);
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      loadingOlderRef.current = false;
      setLoadingOlder(false);
    }
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const message = content.trim();
    const socket = socketRef.current;
    if (!message || !socket || sending) return;

    setSending(true);
    setError("");
    try {
      await sendChatMessage(socket, message);
      setContent("");
    } catch (sendError) {
      setError(
        sendError instanceof Error
          ? sendError.message
          : "Não foi possível enviar a mensagem.",
      );
    } finally {
      setSending(false);
    }
  }

  function handleKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      event.currentTarget.form?.requestSubmit();
    }
  }

  return (
    <main className="min-h-screen bg-zinc-50">
      <AuthenticatedHeader page="group" />
      <section className="mx-auto max-w-4xl px-4 pb-32 pt-8 sm:px-8 sm:pb-36 sm:pt-12">
        <Link
          to="/grupo"
          className="inline-flex items-center gap-2 text-sm font-extrabold text-zinc-500 transition hover:text-brand-700"
        >
          <ArrowLeft size={18} />
          Voltar aos check-ins
        </Link>

        <div className="mt-6 flex h-[calc(100dvh-12rem)] min-h-[32rem] max-h-[44rem] flex-col overflow-hidden rounded-[2rem] border border-zinc-200 bg-white shadow-xl shadow-zinc-200/60">
          <header className="flex shrink-0 items-center justify-between border-b border-zinc-100 px-5 py-4 sm:px-7">
            <div className="flex min-w-0 items-center gap-3">
              <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-brand-50 text-brand-600">
                <MessageCircle size={20} />
              </span>
              <div className="min-w-0">
                <h1 className="truncate text-lg font-black text-ink-950">
                  Chat do grupo
                </h1>
                <p className="text-xs font-medium text-zinc-400">
                  {connected ? "Conectado" : "Conectando..."}
                </p>
              </div>
            </div>
            <span
              className={`size-2.5 rounded-full ${
                connected ? "bg-emerald-500" : "bg-zinc-300"
              }`}
              aria-label={connected ? "Chat conectado" : "Chat desconectado"}
            />
          </header>

          <div className="flex min-h-0 flex-1 flex-col overflow-hidden">
            <div
              ref={messagesContainerRef}
              className="brand-scrollbar min-h-0 flex-1 overflow-y-auto overscroll-contain px-4 py-5 sm:px-7"
            >
              {error && (
                <p className="mb-4 rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                  {error}
                </p>
              )}

              {loading ? (
                <div className="grid h-full min-h-72 place-items-center text-zinc-400">
                  <LoaderCircle className="animate-spin" size={24} />
                </div>
              ) : (
                <>
                  {hasMore && (
                    <div className="mb-6 text-center">
                      <button
                        type="button"
                        disabled={loadingOlder}
                        onClick={() => void loadOlderMessages()}
                        className="text-xs font-extrabold text-brand-600 transition hover:text-brand-700 disabled:opacity-50"
                      >
                        {loadingOlder
                          ? "Carregando..."
                          : "Carregar mensagens anteriores"}
                      </button>
                    </div>
                  )}

                  {messages.length === 0 ? (
                    <div className="grid min-h-72 place-items-center text-center">
                      <div>
                        <MessageCircle
                          className="mx-auto text-zinc-300"
                          size={30}
                        />
                        <p className="mt-3 text-sm font-bold text-zinc-500">
                          O chat ainda está vazio.
                        </p>
                        <p className="mt-1 text-xs text-zinc-400">
                          Envie a primeira mensagem para o grupo.
                        </p>
                      </div>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {messages.map((message) => (
                        <MessageBubble
                          key={message.id}
                          message={message}
                          own={message.authorCode === session?.userCode}
                        />
                      ))}
                    </div>
                  )}
                </>
              )}
            </div>

            <form
              onSubmit={(event) => void handleSubmit(event)}
              className="shrink-0 border-t border-zinc-100 bg-white px-4 py-4 sm:px-7"
            >
              <div className="flex items-end gap-2 rounded-2xl bg-zinc-100 p-2 pl-4">
                <textarea
                  value={content}
                  maxLength={2000}
                  rows={1}
                  disabled={!connected || sending}
                  onChange={(event) => setContent(event.target.value)}
                  onKeyDown={handleKeyDown}
                  placeholder="Mensagem para o grupo..."
                  className="max-h-28 min-h-10 flex-1 resize-none bg-transparent py-2 text-sm text-ink-950 outline-none placeholder:text-zinc-400 disabled:opacity-60"
                />
                <button
                  type="submit"
                  disabled={!connected || sending || !content.trim()}
                  aria-label="Enviar mensagem"
                  className="grid size-10 shrink-0 place-items-center rounded-xl bg-brand-600 text-white transition hover:bg-brand-700 disabled:bg-zinc-300"
                >
                  {sending ? (
                    <LoaderCircle className="animate-spin" size={18} />
                  ) : (
                    <Send size={18} />
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      </section>
      <GroupNavigation />
    </main>
  );
}

function MessageBubble({
  message,
  own,
}: {
  message: ChatMessage;
  own: boolean;
}) {
  return (
    <article
      className={`flex items-end gap-2 ${own ? "justify-end" : "justify-start"}`}
    >
      {!own && (
        <Link
          to={`/perfil/${message.authorCode}`}
          className="grid size-8 shrink-0 overflow-hidden rounded-lg bg-brand-50 text-xs font-black text-brand-700"
          aria-label={`Abrir perfil de ${message.authorName}`}
        >
          {message.authorImageUrl ? (
            <img
              src={message.authorImageUrl}
              alt=""
              className="size-full object-cover"
            />
          ) : message.authorName ? (
            <span className="m-auto">
              {message.authorName.charAt(0).toUpperCase()}
            </span>
          ) : (
            <UserRound className="m-auto" size={15} />
          )}
        </Link>
      )}
      <div className={`max-w-[78%] sm:max-w-[68%] ${own ? "text-right" : ""}`}>
        {!own && (
          <Link
            to={`/perfil/${message.authorCode}`}
            className="mb-1 block truncate px-1 text-xs font-extrabold text-zinc-500 hover:text-brand-700"
          >
            {message.authorName}
          </Link>
        )}
        <div
          className={`rounded-2xl px-4 py-2.5 text-left text-sm leading-relaxed ${
            own
              ? "rounded-br-md bg-brand-600 text-white"
              : "rounded-bl-md bg-zinc-100 text-ink-950"
          }`}
        >
          <p className="whitespace-pre-wrap break-words">{message.content}</p>
          <time
            dateTime={message.createdAt}
            className={`mt-1 block text-[10px] ${
              own ? "text-red-100" : "text-zinc-400"
            }`}
          >
            {formatMessageTime(message.createdAt)}
          </time>
        </div>
      </div>
    </article>
  );
}

function formatMessageTime(value: string) {
  return new Intl.DateTimeFormat("pt-BR", {
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value));
}
