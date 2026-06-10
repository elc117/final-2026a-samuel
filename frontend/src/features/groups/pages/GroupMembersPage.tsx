import {
  ArrowLeft,
  LoaderCircle,
  ShieldCheck,
  UserRound,
  Users,
} from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { AuthenticatedHeader } from "../../auth/components/AuthenticatedHeader";
import { GroupNavigation } from "../components/GroupNavigation";
import {
  ApiError,
  getApiErrorMessage,
} from "../../../services/apiClient";
import {
  getCurrentGroup,
  getCurrentGroupMembers,
  type Group,
  type GroupMember,
} from "../services/groupService";

export function GroupMembersPage() {
  const navigate = useNavigate();
  const [group, setGroup] = useState<Group | null>(null);
  const [members, setMembers] = useState<GroupMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    Promise.all([getCurrentGroup(), getCurrentGroupMembers()])
      .then(([loadedGroup, loadedMembers]) => {
        if (!active) return;
        setGroup(loadedGroup);
        setMembers(loadedMembers);
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

  return (
    <main className="min-h-screen bg-zinc-50">
      <AuthenticatedHeader page="group" />
      <section className="mx-auto max-w-4xl px-5 pb-32 pt-10 sm:px-8 sm:pb-36 sm:pt-16">
        <Link
          to="/grupo"
          className="inline-flex items-center gap-2 text-sm font-extrabold text-zinc-500 transition hover:text-brand-700"
        >
          <ArrowLeft size={18} />
          Voltar ao grupo
        </Link>

        <div className="mt-6 overflow-hidden rounded-[2rem] border border-zinc-200 bg-white shadow-xl shadow-zinc-200/60">
          <header className="border-b border-zinc-100 p-6 sm:p-8">
            <span className="grid size-11 place-items-center rounded-2xl bg-brand-50 text-brand-600">
              <Users size={21} />
            </span>
            <h1 className="mt-5 text-3xl font-black tracking-[-0.04em] text-ink-950">
              Participantes
            </h1>
            <p className="mt-2 text-sm text-zinc-500">
              {group
                ? `${group.memberCount} de 10 pessoas em ${group.name}`
                : "Pessoas que participam do seu grupo."}
            </p>
          </header>

          <div className="p-6 sm:p-8">
            {error && (
              <p className="rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                {error}
              </p>
            )}

            {loading ? (
              <div className="grid min-h-48 place-items-center text-zinc-400">
                <LoaderCircle className="animate-spin" size={24} />
              </div>
            ) : members.length === 0 ? (
              <p className="py-10 text-center text-sm font-bold text-zinc-400">
                Nenhum participante encontrado.
              </p>
            ) : (
              <div className="divide-y divide-zinc-100">
                {members.map((member) => (
                  <Link
                    key={member.code}
                    to={`/perfil/${member.code}`}
                    className="flex items-center gap-3 py-4 transition first:pt-0 last:pb-0 hover:text-brand-700"
                  >
                    <span className="grid size-12 shrink-0 overflow-hidden rounded-xl bg-brand-50 text-sm font-black text-brand-700">
                      {member.profileImageUrl ? (
                        <img
                          src={member.profileImageUrl}
                          alt=""
                          className="size-full object-cover"
                        />
                      ) : member.name ? (
                        <span className="m-auto">
                          {member.name.charAt(0).toUpperCase()}
                        </span>
                      ) : (
                        <UserRound className="m-auto" size={18} />
                      )}
                    </span>
                    <span className="min-w-0 flex-1">
                      <strong className="block truncate text-sm text-ink-950">
                        {member.name}
                      </strong>
                      <span className="mt-0.5 block truncate text-xs text-zinc-500">
                        @{member.username}
                      </span>
                    </span>
                    {member.administrator && (
                      <span className="inline-flex shrink-0 items-center gap-1.5 rounded-full bg-brand-50 px-3 py-1.5 text-xs font-extrabold text-brand-700">
                        <ShieldCheck size={14} />
                        Administrador
                      </span>
                    )}
                  </Link>
                ))}
              </div>
            )}
          </div>
        </div>
      </section>
      <GroupNavigation />
    </main>
  );
}
