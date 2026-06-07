import { Dumbbell } from "lucide-react";
import { useEffect, useState } from "react";
import { Navigate, Outlet } from "react-router-dom";
import { ApiError } from "../../../services/apiClient";
import { getCurrentUser } from "../services/authService";

type AuthGuardProps = {
  mode: "require" | "guest";
};

type AuthStatus = "checking" | "authenticated" | "guest";

export function AuthGuard({ mode }: AuthGuardProps) {
  const [status, setStatus] = useState<AuthStatus>("checking");

  useEffect(() => {
    let active = true;

    getCurrentUser()
      .then(() => {
        if (active) {
          setStatus("authenticated");
        }
      })
      .catch((error: unknown) => {
        if (!active) {
          return;
        }

        if (error instanceof ApiError && error.status === 401) {
          setStatus("guest");
          return;
        }

        setStatus("guest");
      });

    return () => {
      active = false;
    };
  }, []);

  if (status === "checking") {
    return <AuthGuardLoading />;
  }

  if (mode === "require" && status === "guest") {
    return <Navigate to="/login" replace />;
  }

  if (mode === "guest" && status === "authenticated") {
    return <Navigate to="/grupo" replace />;
  }

  return <Outlet />;
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
