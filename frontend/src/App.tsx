import { Navigate, Route, Routes } from "react-router-dom";
import { LoginPage } from "./features/auth/pages/LoginPage";
import { RegisterPage } from "./features/auth/pages/RegisterPage";
import { GuestOnly, RequireAuth } from "./features/auth/components/AuthGuard";
import { GroupPage } from "./features/groups/pages/GroupPage";
import { GroupInvitationPage } from "./features/groups/pages/GroupInvitationPage";
import { GroupSectionPage } from "./features/groups/pages/GroupSectionPage";
import { CreateCheckInPage } from "./features/checkins/pages/CreateCheckInPage";
import { CheckInDetailsPage } from "./features/checkins/pages/CheckInDetailsPage";
import { ProfilePage } from "./features/profile/pages/ProfilePage";

export default function App() {
  return (
    <Routes>
      <Route element={<GuestOnly />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<RegisterPage />} />
      </Route>
      <Route element={<RequireAuth />}>
        <Route path="/grupo" element={<GroupPage />} />
        <Route
          path="/grupo/ranking"
          element={<GroupSectionPage section="ranking" />}
        />
        <Route
          path="/grupo/check-in"
          element={<CreateCheckInPage />}
        />
        <Route
          path="/grupo/check-ins/:checkInId"
          element={<CheckInDetailsPage />}
        />
        <Route
          path="/grupo/detalhes"
          element={<GroupSectionPage section="details" />}
        />
        <Route path="/convite/:token" element={<GroupInvitationPage />} />
        <Route path="/perfil" element={<ProfilePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
