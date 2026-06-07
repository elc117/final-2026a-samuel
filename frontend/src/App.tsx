import { Navigate, Route, Routes } from "react-router-dom";
import { LoginPage } from "./features/auth/pages/LoginPage";
import { RegisterPage } from "./features/auth/pages/RegisterPage";
import { AuthGuard } from "./features/auth/components/AuthGuard";
import { GroupPage } from "./features/groups/pages/GroupPage";

export default function App() {
  return (
    <Routes>
      <Route element={<AuthGuard mode="guest" />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<RegisterPage />} />
      </Route>
      <Route element={<AuthGuard mode="require" />}>
        <Route path="/grupo" element={<GroupPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
