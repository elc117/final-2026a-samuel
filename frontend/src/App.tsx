import { lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { GuestOnly, RequireAuth } from "./features/auth/components/AuthGuard";

const LoginPage = lazy(() =>
  import("./features/auth/pages/LoginPage").then((module) => ({
    default: module.LoginPage,
  })),
);
const RegisterPage = lazy(() =>
  import("./features/auth/pages/RegisterPage").then((module) => ({
    default: module.RegisterPage,
  })),
);
const GroupPage = lazy(() =>
  import("./features/groups/pages/GroupPage").then((module) => ({
    default: module.GroupPage,
  })),
);
const GroupInvitationPage = lazy(() =>
  import("./features/groups/pages/GroupInvitationPage").then((module) => ({
    default: module.GroupInvitationPage,
  })),
);
const GroupMembersPage = lazy(() =>
  import("./features/groups/pages/GroupMembersPage").then((module) => ({
    default: module.GroupMembersPage,
  })),
);
const CreateCheckInPage = lazy(() =>
  import("./features/checkins/pages/CreateCheckInPage").then((module) => ({
    default: module.CreateCheckInPage,
  })),
);
const CheckInDetailsPage = lazy(() =>
  import("./features/checkins/pages/CheckInDetailsPage").then((module) => ({
    default: module.CheckInDetailsPage,
  })),
);
const ProfilePage = lazy(() =>
  import("./features/profile/pages/ProfilePage").then((module) => ({
    default: module.ProfilePage,
  })),
);
const PublicProfilePage = lazy(() =>
  import("./features/profile/pages/PublicProfilePage").then((module) => ({
    default: module.PublicProfilePage,
  })),
);
const ChallengePage = lazy(() =>
  import("./features/challenges/pages/ChallengePage").then((module) => ({
    default: module.ChallengePage,
  })),
);
const NotificationsPage = lazy(() =>
  import("./features/friendships/pages/NotificationsPage").then((module) => ({
    default: module.NotificationsPage,
  })),
);
const ChatPage = lazy(() =>
  import("./features/chat/pages/ChatPage").then((module) => ({
    default: module.ChatPage,
  })),
);
const FriendsPage = lazy(() =>
  import("./features/friendships/pages/FriendsPage").then((module) => ({
    default: module.FriendsPage,
  })),
);

export default function App() {
  return (
    <Suspense fallback={<RouteLoading />}>
      <Routes>
        <Route element={<GuestOnly />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/cadastro" element={<RegisterPage />} />
        </Route>
        <Route element={<RequireAuth />}>
          <Route path="/grupo" element={<GroupPage />} />
          <Route
            path="/grupo/participantes"
            element={<GroupMembersPage />}
          />
          <Route path="/grupo/ranking" element={<ChallengePage />} />
          <Route path="/grupo/check-in" element={<CreateCheckInPage />} />
          <Route
            path="/grupo/check-ins/:checkInId"
            element={<CheckInDetailsPage />}
          />
          <Route
            path="/grupo/notificacoes"
            element={<NotificationsPage />}
          />
          <Route path="/grupo/chat" element={<ChatPage />} />
          <Route path="/convite/:token" element={<GroupInvitationPage />} />
          <Route path="/perfil" element={<ProfilePage />} />
          <Route path="/perfil/amigos" element={<FriendsPage />} />
          <Route path="/perfil/:userCode" element={<PublicProfilePage />} />
        </Route>
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </Suspense>
  );
}

function RouteLoading() {
  return (
    <main className="grid min-h-screen place-items-center bg-zinc-50">
      <p className="text-sm font-bold text-zinc-500">Carregando página...</p>
    </main>
  );
}
