import { CirclePlus, Info, Trophy } from "lucide-react";
import { NavLink } from "react-router-dom";

const navigationItems = [
  {
    label: "Ranking",
    icon: Trophy,
    to: "/grupo/ranking",
    primary: false,
  },
  {
    label: "Fazer check-in",
    icon: CirclePlus,
    to: "/grupo/check-in",
    primary: true,
  },
  {
    label: "Detalhes do grupo",
    icon: Info,
    to: "/grupo/detalhes",
    primary: false,
  },
] as const;

export function GroupNavigation() {
  return (
    <nav
      aria-label="Navegação do grupo"
      className="fixed inset-x-0 bottom-4 z-40 flex justify-center px-4 sm:bottom-6"
    >
      <div className="flex items-center gap-1 rounded-2xl border border-zinc-200/90 bg-white/95 p-1.5 shadow-2xl shadow-zinc-400/25 backdrop-blur sm:gap-2 sm:rounded-3xl sm:p-2">
        {navigationItems.map(({ label, icon: Icon, primary, to }) => (
          <NavLink
            key={label}
            to={to}
            aria-label={label}
            title={label}
            className={({ isActive }) => {
              if (primary) {
                return `inline-flex h-12 items-center justify-center gap-2 rounded-xl px-4 text-sm font-extrabold text-white shadow-lg transition sm:rounded-2xl sm:px-5 ${
                  isActive
                    ? "bg-brand-700 shadow-red-300"
                    : "bg-brand-600 shadow-red-200 hover:bg-brand-700"
                }`;
              }

              return `inline-flex h-12 items-center justify-center gap-2 rounded-xl px-4 text-sm font-extrabold transition sm:rounded-2xl sm:px-5 ${
                isActive
                  ? "bg-brand-50 text-brand-700"
                  : "text-zinc-600 hover:bg-zinc-100 hover:text-zinc-950"
              }`;
            }}
          >
            <Icon size={20} />
            <span className="hidden sm:inline">{label}</span>
          </NavLink>
        ))}
      </div>
    </nav>
  );
}
