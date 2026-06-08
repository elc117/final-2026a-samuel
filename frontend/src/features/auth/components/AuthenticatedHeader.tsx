import { ArrowLeft, LogOut, UserRound } from "lucide-react";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { logout } from "../services/authService";
import { BrandMark } from "./BrandMark";

type AuthenticatedHeaderProps = {
  page: "group" | "profile";
};

export function AuthenticatedHeader({ page }: AuthenticatedHeaderProps) {
  const navigate = useNavigate();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const isGroupPage = page === "group";

  async function handleLogout() {
    if (isLoggingOut) {
      return;
    }

    setIsLoggingOut(true);

    try {
      await logout();
    } finally {
      navigate("/login", { replace: true });
    }
  }

  return (
    <header className="border-b border-zinc-200 bg-white">
      <div
        className={`mx-auto flex h-20 items-center justify-between px-5 sm:px-8 ${
          isGroupPage ? "max-w-7xl" : "max-w-6xl"
        }`}
      >
        <BrandMark />

        <div className="flex items-center gap-2">
          <Link
            to={isGroupPage ? "/perfil" : "/grupo"}
            className={
              isGroupPage
                ? "inline-flex items-center gap-2 rounded-xl bg-brand-50 px-3 py-2 text-sm font-extrabold text-brand-700 transition hover:bg-brand-100"
                : "inline-flex items-center gap-2 rounded-xl px-3 py-2 text-sm font-extrabold text-zinc-600 transition hover:bg-zinc-100 hover:text-zinc-950"
            }
          >
            {isGroupPage ? <UserRound size={17} /> : <ArrowLeft size={18} />}
            <span className="hidden sm:inline">
              {isGroupPage ? "Meu perfil" : "Voltar ao grupo"}
            </span>
          </Link>

          <button
            type="button"
            disabled={isLoggingOut}
            onClick={() => void handleLogout()}
            className="inline-flex items-center gap-2 rounded-xl px-3 py-2 text-sm font-extrabold text-zinc-600 transition hover:bg-red-50 hover:text-red-700 disabled:cursor-wait disabled:opacity-60"
            aria-label="Sair da conta"
          >
            <LogOut size={18} />
            <span className="hidden sm:inline">
              {isLoggingOut ? "Saindo..." : "Sair"}
            </span>
          </button>
        </div>
      </div>
    </header>
  );
}
