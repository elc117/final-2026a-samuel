import { ArrowLeft, Bell, Check, UserPlus, UserRound, Users } from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { AuthenticatedHeader } from "../../auth/components/AuthenticatedHeader";
import {ApiError, getApiErrorMessage } from "../../../services/apiClient";
import { getPublicProfile, type UserProfile } from "../services/profileService";
import { sendFriendshipRequest } from "../../friendships/services/friendshipService";

export function PublicProfilePage() {
  const navigate = useNavigate();
  const { userCode = "" } = useParams();
  const [profile, setProfile] = useState<UserProfile>();
  const [error, setError] = useState("");
  const [connectionError, setConnectionError] = useState("");
  const [isConnecting, setIsConnecting] = useState(false);
  const hasInvalidUserCode = !/^[A-Za-z0-9]{10,}$/.test(userCode);

  useEffect(() => {
    let active = true;

    if (hasInvalidUserCode) {
      return;
    }

    getPublicProfile(userCode)
      .then((response) => active && setProfile(response))
      .catch((requestError: unknown) => {
        if (requestError instanceof ApiError && requestError.status === 401) {
          navigate("/login", { replace: true });
          return;
        }
        if (active) setError(getApiErrorMessage(requestError));
      });

    return () => {
      active = false;
    };
  }, [hasInvalidUserCode, navigate, userCode]);

  const visibleError = hasInvalidUserCode
    ? "Perfil não encontrado."
    : error;

  async function connect() {
    if (!profile || profile.relationship !== "NONE" || isConnecting) {
      return;
    }

    setConnectionError("");
    setIsConnecting(true);

    try {
      await sendFriendshipRequest(profile.code);
      setProfile((current) =>
        current ? { ...current, relationship: "PENDING_SENT" } : current,
      );
    }
    catch (requestError) {
      setConnectionError(getApiErrorMessage(requestError));
    }
    finally {
      setIsConnecting(false);
    }
  }

  return (
    <main className="min-h-screen bg-zinc-50">
      <AuthenticatedHeader page="group" />
      <section className="mx-auto max-w-4xl px-5 py-10 sm:px-8 sm:py-16">
        <Link
          to="/grupo"
          className="inline-flex items-center gap-2 text-sm font-extrabold text-zinc-500 transition hover:text-brand-700"
        >
          <ArrowLeft size={18} />
          Voltar ao grupo
        </Link>

        {!profile && !visibleError ? (
          <p className="mt-8 text-sm font-bold text-zinc-500">
            Carregando perfil...
          </p>
        ) : visibleError || !profile ? (
          <p className="mt-6 rounded-2xl bg-red-50 px-5 py-4 text-sm font-semibold text-red-700">
            {visibleError || "Perfil não encontrado."}
          </p>
        ) : (
          <article className="mt-6 overflow-hidden rounded-[2rem] border border-zinc-200 bg-white shadow-xl shadow-zinc-200/60">
            <div className="auth-grid relative h-40 bg-brand-600 sm:h-52">
              <div className="absolute inset-0 bg-gradient-to-br from-brand-700/20 to-black/20" />
            </div>

            <div className="px-6 pb-8 sm:px-10 sm:pb-10">
              <div className="flex flex-col gap-6 sm:flex-row sm:items-end sm:justify-between">
                <div className="min-w-0">
                  <div className="relative z-10 -mt-16 grid size-32 overflow-hidden rounded-[2rem] border-4 border-white bg-brand-50 text-brand-600 shadow-xl">
                    {profile.profileImageUrl ? (
                      <img
                        src={profile.profileImageUrl}
                        alt={`Foto de ${profile.name}`}
                        className="size-full object-cover"
                      />
                    ) : (
                      <UserRound className="m-auto" size={52} />
                    )}
                  </div>

                  <div className="mt-4">
                    <p className="text-sm font-bold text-brand-600">
                      @{profile.username}
                    </p>
                    <h1 className="mt-1 break-words text-3xl font-black tracking-[-0.04em] text-ink-950 sm:text-4xl">
                      {profile.name}
                    </h1>
                  </div>
                </div>

                <div className="flex w-full flex-wrap items-center gap-4 sm:w-auto sm:justify-end">
                  <div className="flex items-center gap-2 text-sm font-bold text-zinc-500">
                    <Users size={18} className="text-brand-600" />
                    <span>
                      <strong className="text-ink-950">
                        {profile.friendCount}
                      </strong>{" "}
                      {profile.friendCount === 1 ? "amigo" : "amigos"}
                    </span>
                  </div>

                  <ConnectionAction
                    relationship={profile.relationship}
                    isConnecting={isConnecting}
                    onConnect={() => void connect()}
                  />
                </div>
              </div>
              {connectionError && (
                <p className="mt-5 rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                  {connectionError}
                </p>
              )}
            </div>
          </article>
        )}
      </section>
    </main>
  );
}

function ConnectionAction({
  relationship,
  isConnecting,
  onConnect,
}: {
  relationship: UserProfile["relationship"];
  isConnecting: boolean;
  onConnect: () => void;
}) {
  const baseClass =
    "inline-flex h-9 items-center justify-center gap-1.5 rounded-lg border px-3.5 text-xs font-extrabold transition";

  if (relationship === "SELF") {
    return null;
  }

  if (relationship === "CONNECTED") {
    return (
      <span
        className={`${baseClass} border-emerald-200 bg-emerald-50 text-emerald-700`}
      >
        <Check size={16} />
        Conectado
      </span>
    );
  }

  if (relationship === "PENDING_SENT") {
    return (
      <span
        className={`${baseClass} border-brand-200 bg-brand-50 text-brand-700`}
      >
        <Check size={16} />
        Solicitação enviada
      </span>
    );
  }

  if (relationship === "PENDING_RECEIVED") {
    return (
      <Link
        to="/grupo/notificacoes"
        className={`${baseClass} border-brand-200 bg-brand-50 text-brand-700 hover:bg-brand-100`}
      >
        <Bell size={16} />
        Responder solicitação
      </Link>
    );
  }

  return (
    <button
      type="button"
      disabled={isConnecting}
      onClick={onConnect}
      className={`${baseClass} border-brand-200 bg-white text-brand-700 hover:bg-brand-50 disabled:cursor-wait disabled:opacity-60`}
    >
      <UserPlus size={16} />
      {isConnecting ? "Enviando..." : "Conectar"}
    </button>
  );
}
