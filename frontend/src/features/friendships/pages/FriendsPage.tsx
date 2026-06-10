import { ArrowLeft, LoaderCircle, UserRound, Users } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { AuthenticatedHeader } from "../../auth/components/AuthenticatedHeader";
import {
  ApiError,
  getApiErrorMessage,
} from "../../../services/apiClient";
import {
  getProfile,
  type UserProfile,
} from "../../profile/services/profileService";
import {
  getFriends,
  type Friend,
} from "../services/friendshipService";
import { useInfiniteScroll } from "../../../shared/pagination/useInfiniteScroll";

export function FriendsPage() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState<UserProfile>();
  const [friends, setFriends] = useState<Friend[]>([]);
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    Promise.all([getProfile(), getFriends()])
      .then(([loadedProfile, page]) => {
        if (!active) return;
        setProfile(loadedProfile);
        setFriends(page.items);
        setNextCursor(page.nextCursor);
        setHasMore(page.hasMore);
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
    };
  }, [navigate]);

  const loadMore = useCallback(async () => {
    if (!nextCursor || !hasMore || loadingMore) return;

    setLoadingMore(true);
    setError("");
    try {
      const page = await getFriends(nextCursor);
      setFriends((current) => {
        const codes = new Set(current.map((friend) => friend.code));
        return [
          ...current,
          ...page.items.filter((friend) => !codes.has(friend.code)),
        ];
      });
      setNextCursor(page.nextCursor);
      setHasMore(page.hasMore);
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setLoadingMore(false);
    }
  }, [hasMore, loadingMore, nextCursor]);

  const endRef = useInfiniteScroll({
    enabled: hasMore && !loadingMore && !error,
    onLoad: () => void loadMore(),
  });

  return (
    <main className="min-h-screen bg-zinc-50">
      <AuthenticatedHeader page="profile" />
      <section className="mx-auto max-w-4xl px-5 py-10 sm:px-8 sm:py-16">
        <Link
          to="/perfil"
          className="inline-flex items-center gap-2 text-sm font-extrabold text-zinc-500 transition hover:text-brand-700"
        >
          <ArrowLeft size={18} />
          Voltar ao perfil
        </Link>

        <div className="mt-6 overflow-hidden rounded-[2rem] border border-zinc-200 bg-white shadow-xl shadow-zinc-200/60">
          <header className="border-b border-zinc-100 p-6 sm:p-8">
            <span className="grid size-11 place-items-center rounded-2xl bg-brand-50 text-brand-600">
              <Users size={21} />
            </span>
            <h1 className="mt-5 text-3xl font-black tracking-[-0.04em] text-ink-950">
              Amigos
            </h1>
            <p className="mt-2 text-sm text-zinc-500">
              {profile
                ? `${profile.friendCount} ${
                    profile.friendCount === 1 ? "conexão" : "conexões"
                  }`
                : "Suas conexões no GymSocial."}
            </p>
          </header>

          <div className="p-6 sm:p-8">
            {error && (
              <p className="mb-5 rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                {error}
              </p>
            )}

            {loading ? (
              <div className="grid min-h-48 place-items-center text-zinc-400">
                <LoaderCircle className="animate-spin" size={24} />
              </div>
            ) : friends.length === 0 ? (
              <div className="py-12 text-center">
                <Users className="mx-auto text-zinc-300" size={30} />
                <h2 className="mt-4 font-extrabold text-ink-950">
                  Nenhum amigo ainda
                </h2>
                <p className="mt-2 text-sm text-zinc-500">
                  Conecte-se com as pessoas do seu grupo.
                </p>
              </div>
            ) : (
              <div className="divide-y divide-zinc-100">
                {friends.map((friend) => (
                  <Link
                    key={friend.code}
                    to={`/perfil/${friend.code}`}
                    className="flex items-center gap-3 py-4 transition first:pt-0 hover:text-brand-700"
                  >
                    <span className="grid size-12 shrink-0 overflow-hidden rounded-xl bg-brand-50 text-sm font-black text-brand-700">
                      {friend.profileImageUrl ? (
                        <img
                          src={friend.profileImageUrl}
                          alt=""
                          className="size-full object-cover"
                        />
                      ) : friend.name ? (
                        <span className="m-auto">
                          {friend.name.charAt(0).toUpperCase()}
                        </span>
                      ) : (
                        <UserRound className="m-auto" size={18} />
                      )}
                    </span>
                    <span className="min-w-0 flex-1">
                      <strong className="block truncate text-sm text-ink-950">
                        {friend.name}
                      </strong>
                      <span className="mt-0.5 block truncate text-xs text-zinc-500">
                        @{friend.username}
                      </span>
                    </span>
                    <time
                      dateTime={friend.connectedAt}
                      className="hidden text-xs text-zinc-400 sm:block"
                    >
                      Desde {formatConnectionDate(friend.connectedAt)}
                    </time>
                  </Link>
                ))}

                {hasMore && (
                  <div ref={endRef} className="py-5 text-center">
                    {error ? (
                      <button
                        type="button"
                        onClick={() => void loadMore()}
                        className="text-sm font-extrabold text-brand-700"
                      >
                        Tentar novamente
                      </button>
                    ) : (
                      <span className="text-sm font-bold text-zinc-400">
                        {loadingMore ? "Carregando amigos..." : "Carregando..."}
                      </span>
                    )}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </section>
    </main>
  );
}

function formatConnectionDate(value: string) {
  return new Intl.DateTimeFormat("pt-BR", {
    month: "short",
    year: "numeric",
  }).format(new Date(value));
}
