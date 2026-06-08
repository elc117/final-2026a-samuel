import {
  ArrowRight,
  Dumbbell,
  ShieldCheck,
  Users,
} from "lucide-react";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  getApiErrorMessage,
} from "../../../services/apiClient";
import { GroupPage } from "./GroupPage";
import {
  acceptGroupInvitation,
  getGroupInvitation,
  type GroupInvitation,
} from "../services/groupInvitationService";

export function GroupInvitationPage() {
  const navigate = useNavigate();
  const { token = "" } = useParams();
  const [invitation, setInvitation] = useState<GroupInvitation>();
  const [loadError, setLoadError] = useState("");
  const [acceptError, setAcceptError] = useState("");
  const [isAccepting, setIsAccepting] = useState(false);

  useEffect(() => {
    let active = true;

    getGroupInvitation(token)
      .then((response) => {
        if (active) {
          setInvitation(response);
        }
      })
      .catch((error: unknown) => {
        if (active) {
          setLoadError(getApiErrorMessage(error));
        }
      });

    return () => {
      active = false;
    };
  }, [token]);

  async function acceptInvitation() {
    setAcceptError("");
    setIsAccepting(true);

    try {
      await acceptGroupInvitation(token);
      navigate("/grupo", { replace: true });
    } catch (error) {
      setAcceptError(getApiErrorMessage(error));
      setIsAccepting(false);
    }
  }

  return (
    <>
      <GroupPage />
      <div
        className="fixed inset-0 z-50 grid place-items-center overflow-y-auto bg-zinc-950/75 p-4 backdrop-blur-sm"
        role="presentation"
      >
        <section
          role="dialog"
          aria-modal="true"
          aria-labelledby="invitation-title"
          className="my-auto w-full max-w-lg overflow-hidden rounded-[2rem] bg-white shadow-2xl"
        >
          {loadError ? (
            <div className="p-8 text-center sm:p-10">
              <span className="mx-auto grid size-14 place-items-center rounded-2xl bg-red-50 text-red-600">
                <Dumbbell size={25} />
              </span>
              <h1
                id="invitation-title"
                className="mt-5 text-2xl font-black text-ink-950"
              >
                Convite indisponível
              </h1>
              <p className="mt-3 text-sm leading-6 text-zinc-500">
                {loadError}
              </p>
            </div>
          ) : !invitation ? (
            <div className="p-10 text-center">
              <span className="mx-auto grid size-14 animate-pulse place-items-center rounded-2xl bg-brand-600 text-white">
                <Dumbbell size={25} />
              </span>
              <p className="mt-4 text-sm font-bold text-zinc-500">
                Carregando convite...
              </p>
            </div>
          ) : (
            <>
              <div className="relative h-52 bg-brand-600">
                {invitation.groupImageUrl ? (
                  <img
                    src={invitation.groupImageUrl}
                    alt=""
                    className="absolute inset-0 size-full object-cover"
                  />
                ) : (
                  <div className="auth-grid absolute inset-0 opacity-40" />
                )}
                <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/15 to-transparent" />
                <div className="absolute inset-x-0 bottom-0 p-6 text-white">
                  <p className="text-xs font-black uppercase tracking-[0.18em] text-white/70">
                    Convite para o grupo
                  </p>
                  <h1
                    id="invitation-title"
                    className="mt-2 text-3xl font-black tracking-[-0.04em]"
                  >
                    {invitation.groupName}
                  </h1>
                </div>
              </div>

              <div className="p-6 sm:p-8">
                <div className="grid gap-3 sm:grid-cols-2">
                  <div className="flex items-center gap-3 rounded-2xl bg-zinc-50 p-4">
                    <Users className="text-brand-600" size={21} />
                    <div>
                      <strong className="block text-sm text-ink-950">
                        {invitation.memberCount} de{" "}
                        {invitation.maximumMembers}
                      </strong>
                      <span className="text-xs text-zinc-500">
                        participantes
                      </span>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 rounded-2xl bg-zinc-50 p-4">
                    <ShieldCheck className="text-brand-600" size={21} />
                    <div>
                      <strong className="block text-sm text-ink-950">
                        Link permanente
                      </strong>
                      <span className="text-xs text-zinc-500">
                        válido enquanto o grupo existir
                      </span>
                    </div>
                  </div>
                </div>

                <p className="mt-6 text-center text-sm leading-6 text-zinc-500">
                  {invitation.alreadyMember
                    ? "Você já faz parte deste grupo. Continue para acessar a tela do grupo."
                    : "Aceite o convite para entrar no grupo e começar a acompanhar os treinos e desafios."}
                </p>

                {acceptError && (
                  <p
                    role="alert"
                    className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-center text-sm font-medium text-red-700"
                  >
                    {acceptError}
                  </p>
                )}

                <button
                  type="button"
                  disabled={isAccepting}
                  onClick={() => void acceptInvitation()}
                  className="mt-6 flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-brand-600 px-5 text-sm font-extrabold text-white shadow-lg shadow-red-200 transition hover:bg-brand-700 disabled:cursor-wait disabled:opacity-70"
                >
                  {isAccepting
                    ? "Entrando no grupo..."
                    : invitation.alreadyMember
                      ? "Continuar para o grupo"
                      : "Aceitar convite"}
                  {!isAccepting && <ArrowRight size={18} />}
                </button>
              </div>
            </>
          )}
        </section>
      </div>
    </>
  );
}
