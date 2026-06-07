import { zodResolver } from "@hookform/resolvers/zod";
import { LockKeyhole, Mail } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link } from "react-router-dom";
import { AuthLayout } from "../../../layouts/AuthLayout";
import { AuthField } from "../components/AuthField";
import {
  loginSchema,
  type LoginFormData,
} from "../schemas/authSchemas";

export function LoginPage() {
  const [message, setMessage] = useState("");
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    mode: "onBlur",
    defaultValues: {
      email: "",
      password: "",
    },
  });

  async function onSubmit() {
    setMessage("");

    await new Promise((resolve) => window.setTimeout(resolve, 450));
    setMessage(
      "Dados validados. A API de autenticação será conectada em seguida.",
    );
  }

  return (
    <AuthLayout
      eyebrow="Bem-vindo de volta"
      title="Entre na sua conta"
      description="Continue acompanhando seus amigos e mantendo sua sequência de treinos."
    >
      <form
        className="space-y-5"
        onSubmit={handleSubmit(onSubmit)}
        noValidate
      >
        <AuthField
          id="email"
          type="email"
          label="E-mail"
          icon={Mail}
          placeholder="voce@exemplo.com"
          autoComplete="email"
          error={errors.email?.message}
          {...register("email")}
        />
        <div>
          <AuthField
            id="password"
            type="password"
            label="Senha"
            icon={LockKeyhole}
            placeholder="Digite sua senha"
            autoComplete="current-password"
            error={errors.password?.message}
            {...register("password")}
          />
          <div className="mt-2 flex justify-end">
            <button
              type="button"
              className="text-sm font-bold text-brand-600 transition hover:text-brand-700 focus:outline-none focus:underline"
            >
              Esqueci minha senha
            </button>
          </div>
        </div>

        {message && (
          <p role="status" className="rounded-xl bg-brand-50 px-4 py-3 text-sm text-brand-700">
            {message}
          </p>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
          className="flex h-12 w-full items-center justify-center rounded-xl bg-brand-600 px-5 text-sm font-extrabold text-white shadow-lg shadow-red-200 transition hover:bg-brand-700 focus:outline-none focus:ring-4 focus:ring-brand-100 disabled:cursor-wait disabled:opacity-70"
        >
          {isSubmitting ? "Entrando..." : "Entrar"}
        </button>
      </form>

      <p className="mt-8 text-center text-sm text-zinc-500">
        Ainda não tem uma conta?{" "}
        <Link to="/cadastro" className="font-extrabold text-brand-600 hover:text-brand-700">
          Criar conta
        </Link>
      </p>
    </AuthLayout>
  );
}
