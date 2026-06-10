import { Dumbbell } from "lucide-react";
import { useEffect, useState } from "react";
import {
  Navigate,
  Outlet,
  useLocation,
} from "react-router-dom";
import { restoreSession } from "../../../services/apiClient";
import { buildAuthPath, getAuthRedirect } from "../services/authRedirect";

type AuthGuardProps = {
  mode: "require" | "guest";
};

type AuthStatus = "checking" | "authenticated" | "guest";

function AuthGuard({ mode }: AuthGuardProps) {
  const location = useLocation();
  const [status, setStatus] = useState<AuthStatus>("checking");

  useEffect(() => {
    let active = true;

    restoreSession()
      .then((authenticated) => {
        if (active) {
          setStatus(authenticated ? "authenticated" : "guest");
        }
      })
      .catch(() => {
        if (active) {
          setStatus("guest");
        }
      });

    return () => {
      active = false;
    };
  }, []);

  if (status === "checking") {
    return <AuthGuardLoading />;
  }

  if (mode === "require" && status === "guest") {
    const currentPath = `${location.pathname}${location.search}`;
    return (
      <Navigate
        to={buildAuthPath("/login", currentPath)}
        replace
      />
    );
  }

  if (mode === "guest" && status === "authenticated") {
    return (
      <Navigate
        to={getAuthRedirect(location.search)}
        replace
      />
    );
  }

  return <Outlet />;
}

export function GuestOnly() {
  return <AuthGuard mode="guest" />;
}

export function RequireAuth() {
  return <AuthGuard mode="require" />;
}

function AuthGuardLoading() {
  return (
    <main className="grid min-h-screen place-items-center bg-zinc-50">
      <div className="text-center">
        <span className="mx-auto grid size-14 animate-pulse place-items-center rounded-2xl bg-brand-600 text-white">
          <Dumbbell size={26} />
        </span>
        <p className="mt-4 text-sm font-bold text-zinc-500">
          Verificando sua sessão...
        </p>
      </div>
    </main>
  );
}
