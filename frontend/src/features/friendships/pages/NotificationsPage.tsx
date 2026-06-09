import { ArrowLeft, Bell, Check, UserRound, UserX } from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { AuthenticatedHeader } from "../../auth/components/AuthenticatedHeader";
import { GroupNavigation } from "../../groups/components/GroupNavigation";
import {
  ApiError,
  getApiErrorMessage,
} from "../../../services/apiClient";
import {
  acceptFriendshipRequest,
  getFriendshipRequests,
  notifyFriendshipRequestsChanged,
  rejectFriendshipRequest,
  type FriendshipRequest,
} from "../services/friendshipService";

export function NotificationsPage() {
  const navigate = useNavigate();
  const [requests, setRequests] = useState<FriendshipRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [processingId, setProcessingId] = useState("");

  useEffect(() => {
    let active = true;

    getFriendshipRequests()
      .then((response) => active && setRequests(response))
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
    };
  }, [navigate]);

  async function handleRequest(
    requestId: string,
    action: "accept" | "reject",
  ) {
    setError("");
    setProcessingId(requestId);

    try {
      if (action === "accept") {
        await acceptFriendshipRequest(requestId);
      } else {
        await rejectFriendshipRequest(requestId);
      }
      setRequests((current) =>
        current.filter((request) => request.id !== requestId),
      );
      notifyFriendshipRequestsChanged();
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setProcessingId("");
    }
  }

  return (
    <main className="min-h-screen bg-zinc-50">
      <AuthenticatedHeader page="group" />
      <section className="mx-auto max-w-4xl px-5 pb-32 pt-10 sm:px-8 sm:pb-36 sm:pt-16">
        <Link
          to="/grupo"
          className="inline-flex items-center gap-2 text-sm font-extrabold text-zinc-500 transition hover:text-brand-700"
        >
          <ArrowLeft size={18} />
          Voltar aos check-ins
        </Link>

        <div className="mt-6 overflow-hidden rounded-[2rem] border border-zinc-200 bg-white shadow-xl shadow-zinc-200/60">
          <header className="border-b border-zinc-100 p-6 sm:p-8">
            <span className="grid size-11 place-items-center rounded-2xl bg-brand-50 text-brand-600">
              <Bell size={21} />
            </span>
            <h1 className="mt-5 text-3xl font-black tracking-[-0.04em] text-ink-950">
              Notificações
            </h1>
            <p className="mt-2 text-sm text-zinc-500">
              Solicitações de conexão das pessoas do seu grupo.
            </p>
          </header>

          <div className="p-6 sm:p-8">
            {error && (
              <p className="mb-5 rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                {error}
              </p>
            )}

            {loading ? (
              <p className="py-8 text-center text-sm font-bold text-zinc-400">
                Carregando notificações...
              </p>
            ) : requests.length === 0 ? (
              <div className="py-12 text-center">
                <Bell className="mx-auto text-zinc-300" size={30} />
                <h2 className="mt-4 font-extrabold text-ink-950">
                  Nenhuma solicitação
                </h2>
                <p className="mt-2 text-sm text-zinc-500">
                  Novos pedidos de conexão aparecerão aqui.
                </p>
              </div>
            ) : (
              <div className="divide-y divide-zinc-100">
                {requests.map((request) => {
                  const isProcessing = processingId === request.id;

                  return (
                    <article
                      key={request.id}
                      className="flex flex-col gap-4 py-5 first:pt-0 last:pb-0 sm:flex-row sm:items-center"
                    >
                      <Link
                        to={`/perfil/${request.requesterCode}`}
                        className="flex min-w-0 flex-1 items-center gap-3"
                      >
                        <span className="grid size-11 shrink-0 overflow-hidden rounded-xl bg-brand-50 text-sm font-black text-brand-700">
                          {request.requesterImageUrl ? (
                            <img
                              src={request.requesterImageUrl}
                              alt=""
                              className="size-full object-cover"
                            />
                          ) : request.requesterName ? (
                            <span className="m-auto">
                              {request.requesterName.charAt(0).toUpperCase()}
                            </span>
                          ) : (
                            <UserRound className="m-auto" size={18} />
                          )}
                        </span>
                        <span className="min-w-0">
                          <strong className="block truncate text-sm text-ink-950">
                            {request.requesterName}
                          </strong>
                          <span className="mt-0.5 block truncate text-xs text-zinc-500">
                            @{request.requesterUsername} quer se conectar
                          </span>
                        </span>
                      </Link>

                      <div className="flex gap-2 sm:shrink-0">
                        <button
                          type="button"
                          disabled={isProcessing}
                          onClick={() =>
                            void handleRequest(request.id, "reject")
                          }
                          className="inline-flex h-9 flex-1 items-center justify-center gap-1.5 rounded-lg border border-zinc-200 px-3 text-xs font-extrabold text-zinc-600 transition hover:bg-zinc-100 disabled:opacity-50 sm:flex-none"
                        >
                          <UserX size={15} />
                          Recusar
                        </button>
                        <button
                          type="button"
                          disabled={isProcessing}
                          onClick={() =>
                            void handleRequest(request.id, "accept")
                          }
                          className="inline-flex h-9 flex-1 items-center justify-center gap-1.5 rounded-lg bg-brand-600 px-3 text-xs font-extrabold text-white transition hover:bg-brand-700 disabled:opacity-50 sm:flex-none"
                        >
                          <Check size={15} />
                          {isProcessing ? "Aguarde..." : "Aceitar"}
                        </button>
                      </div>
                    </article>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      </section>
      <GroupNavigation />
    </main>
  );
}
