import { Navigate, Route, Routes } from "react-router-dom";
import { LoginPage } from "./features/auth/pages/LoginPage";
import { RegisterPage } from "./features/auth/pages/RegisterPage";
import {
  GuestOnly,
  RequireAuth,
} from "./features/auth/components/AuthGuard";
import { GroupPage } from "./features/groups/pages/GroupPage";

export default function App() {
  return (
    <Routes>
      <Route element={<GuestOnly />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<RegisterPage />} />
      </Route>
      <Route element={<RequireAuth />}>
        <Route path="/grupo" element={<GroupPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
