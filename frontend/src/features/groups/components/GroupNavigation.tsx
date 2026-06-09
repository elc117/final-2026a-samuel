import { Bell, CirclePlus, Trophy } from "lucide-react";
import { useEffect, useState } from "react";
import { NavLink } from "react-router-dom";
import {
  FRIENDSHIP_REQUESTS_CHANGED_EVENT,
  getFriendshipRequestCount,
} from "../../friendships/services/friendshipService";

const navigationItems = [
  {
    label: "Ranking",
    icon: Trophy,
    to: "/grupo/ranking",
    primary: false,
    notifications: false,
  },
  {
    label: "Fazer check-in",
    icon: CirclePlus,
    to: "/grupo/check-in",
    primary: true,
    notifications: false,
  },
  {
    label: "Notificações",
    icon: Bell,
    to: "/grupo/notificacoes",
    primary: false,
    notifications: true,
  },
] as const;

export function GroupNavigation() {
  const [notificationCount, setNotificationCount] = useState(0);

  useEffect(() => {
    let active = true;

    function loadCount(force = false) {
      getFriendshipRequestCount(force)
        .then((count) => {
          if (active) setNotificationCount(count);
        })
        .catch(() => undefined);
    }

    function handleRequestsChanged() {
      loadCount(true);
    }

    loadCount();
    const refreshInterval = window.setInterval(() => loadCount(true), 30_000);
    window.addEventListener(
      FRIENDSHIP_REQUESTS_CHANGED_EVENT,
      handleRequestsChanged,
    );
    window.addEventListener("focus", handleRequestsChanged);

    return () => {
      active = false;
      window.clearInterval(refreshInterval);
      window.removeEventListener(
        FRIENDSHIP_REQUESTS_CHANGED_EVENT,
        handleRequestsChanged,
      );
      window.removeEventListener("focus", handleRequestsChanged);
    };
  }, []);

  return (
    <nav
      aria-label="Navegação do grupo"
      className="fixed inset-x-0 bottom-4 z-40 flex justify-center px-4 sm:bottom-6"
    >
      <div className="flex items-center gap-1 rounded-2xl border border-zinc-200/90 bg-white/95 p-1.5 shadow-2xl shadow-zinc-400/25 backdrop-blur sm:gap-2 sm:rounded-3xl sm:p-2">
        {navigationItems.map(
          ({ label, icon: Icon, primary, notifications, to }) => (
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
                  : notifications && notificationCount > 0
                    ? "text-brand-700 hover:bg-brand-50"
                    : "text-zinc-600 hover:bg-zinc-100 hover:text-zinc-950"
              }`;
            }}
          >
            <span className="relative">
              <Icon
                size={20}
                className={
                  notifications && notificationCount > 0
                    ? "text-brand-600"
                    : undefined
                }
              />
              {notifications && notificationCount > 0 && (
                <span className="absolute -right-2.5 -top-2.5 grid min-w-4 place-items-center rounded-full bg-brand-600 px-1 text-[9px] font-black leading-4 text-white ring-2 ring-white">
                  {notificationCount > 99 ? "99+" : notificationCount}
                </span>
              )}
            </span>
            <span className="hidden sm:inline">{label}</span>
          </NavLink>
          ),
        )}
      </div>
    </nav>
  );
}
