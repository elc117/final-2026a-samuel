import {ArrowLeft, CirclePlus, Info, Trophy, type LucideIcon} from "lucide-react";
import { Link } from "react-router-dom";
import { AuthenticatedHeader } from "../../auth/components/AuthenticatedHeader";
import { GroupNavigation } from "../components/GroupNavigation";

type GroupSection = "ranking" | "check-in" | "details";

type SectionContent = {
  eyebrow: string;
  title: string;
  description: string;
  icon: LucideIcon;
};

const sectionContent: Record<GroupSection, SectionContent> = {
  ranking: {
    eyebrow: "Desempenho do grupo",
    title: "Ranking",
    description:
      "A classificação dos participantes aparecerá aqui quando os desafios forem implementados.",
    icon: Trophy,
  },
  "check-in": {
    eyebrow: "Registrar atividade",
    title: "Fazer check-in",
    description:
      "O formulário para publicar fotos e exercícios será implementado nesta área.",
    icon: CirclePlus,
  },
  details: {
    eyebrow: "Informações",
    title: "Detalhes do grupo",
    description:
      "Participantes, administrador e configurações do grupo serão exibidos nesta área.",
    icon: Info,
  },
};

export function GroupSectionPage({ section }: { section: GroupSection }) {
  const content = sectionContent[section];
  const Icon = content.icon;

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

        <div className="mt-6 grid min-h-96 place-items-center rounded-[2rem] border border-zinc-200 bg-white px-6 py-14 text-center shadow-xl shadow-zinc-200/60">
          <div className="max-w-lg">
            <span className="mx-auto grid size-16 place-items-center rounded-2xl bg-brand-50 text-brand-600">
              <Icon size={29} />
            </span>
            <p className="mt-6 text-xs font-black uppercase tracking-[0.16em] text-brand-600">
              {content.eyebrow}
            </p>
            <h1 className="mt-2 text-3xl font-black tracking-[-0.04em] text-ink-950 sm:text-4xl">
              {content.title}
            </h1>
            <p className="mt-4 text-sm leading-7 text-zinc-500 sm:text-base">
              {content.description}
            </p>
          </div>
        </div>
      </section>

      <GroupNavigation />
    </main>
  );
}
