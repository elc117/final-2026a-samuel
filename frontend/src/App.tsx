import { Navigate, Route, Routes } from "react-router-dom";
import { LoginPage } from "./features/auth/pages/LoginPage";
import { RegisterPage } from "./features/auth/pages/RegisterPage";
import { GroupPage } from "./features/groups/pages/GroupPage";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/cadastro" element={<RegisterPage />} />
      <Route path="/grupo" element={<GroupPage />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
