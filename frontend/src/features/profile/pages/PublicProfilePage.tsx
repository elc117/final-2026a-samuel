import { ArrowLeft, UserRound, Users } from "lucide-react";
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
  const { userId = "" } = useParams();
  const [profile, setProfile] = useState<UserProfile>();
  const [error, setError] = useState("");
  const parsedUserId = Number(userId);
  const hasInvalidUserId =
    !Number.isSafeInteger(parsedUserId) || parsedUserId <= 0;

  useEffect(() => {
    let active = true;

    if (hasInvalidUserId) {
      return;
    }

    getPublicProfile(parsedUserId)
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
  }, [hasInvalidUserId, navigate, parsedUserId]);

  const visibleError = hasInvalidUserId
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
              <div className="-mt-16 flex flex-col items-start gap-5 sm:flex-row sm:items-end sm:justify-between">
                <div className="flex flex-col items-start gap-4 sm:flex-row sm:items-end">
                  <div className="grid size-32 overflow-hidden rounded-[2rem] border-4 border-white bg-brand-50 text-brand-600 shadow-xl">
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

                  <div className="pb-1">
                    <p className="text-sm font-bold text-brand-600">
                      @{profile.username}
                    </p>
                    <h1 className="mt-1 text-3xl font-black tracking-[-0.04em] text-ink-950 sm:text-4xl">
                      {profile.name}
                    </h1>
                  </div>
                </div>

                <div className="flex min-w-40 items-center gap-3 rounded-2xl bg-brand-50 px-5 py-4 text-brand-700">
                  <Users size={24} />
                  <div>
                    <strong className="block text-2xl font-black leading-none">
                      {profile.friendCount}
                    </strong>
                    <span className="text-xs font-extrabold uppercase tracking-wider">
                      {profile.friendCount === 1 ? "amigo" : "amigos"}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </article>
        )}
      </section>
    </main>
  );
}
