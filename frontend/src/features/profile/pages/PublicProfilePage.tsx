import { ArrowLeft, Check, UserPlus, UserRound, Users } from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { AuthenticatedHeader } from "../../auth/components/AuthenticatedHeader";
import {
  ApiError,
  getApiErrorMessage,
} from "../../../services/apiClient";
import {
  getPublicProfile,
  type UserProfile,
} from "../services/profileService";

export function PublicProfilePage() {
  const navigate = useNavigate();
  const { userCode = "" } = useParams();
  const [profile, setProfile] = useState<UserProfile>();
  const [error, setError] = useState("");
  const [connectionRequested, setConnectionRequested] = useState(false);
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

                  <button
                    type="button"
                    onClick={() => setConnectionRequested((current) => !current)}
                    className={`inline-flex h-9 items-center justify-center gap-1.5 rounded-lg border px-3.5 text-xs font-extrabold transition ${
                      connectionRequested
                        ? "border-brand-200 bg-brand-50 text-brand-700 hover:bg-brand-100"
                        : "border-brand-200 bg-white text-brand-700 hover:bg-brand-50"
                    }`}
                  >
                    {connectionRequested ? (
                      <>
                        <Check size={18} />
                        Solicitação enviada
                      </>
                    ) : (
                      <>
                        <UserPlus size={18} />
                        Conectar
                      </>
                    )}
                  </button>
                </div>
              </div>
            </div>
          </article>
        )}
      </section>
    </main>
  );
}
