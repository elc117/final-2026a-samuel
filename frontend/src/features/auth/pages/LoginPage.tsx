import { LockKeyhole, Mail } from "lucide-react";
import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { AuthLayout } from "../../../layouts/AuthLayout";
import { AuthField } from "../components/AuthField";

export function LoginPage() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setMessage("");

    window.setTimeout(() => {
      setIsSubmitting(false);
      setMessage("A interface está pronta. A API de autenticação será conectada em seguida.");
    }, 450);
  }

  return (
    <AuthLayout
      eyebrow="Bem-vindo de volta"
      title="Entre na sua conta"
      description="Continue acompanhando seus amigos e mantendo sua sequência de treinos."
    >
      <form className="space-y-5" onSubmit={handleSubmit}>
        <AuthField
          id="email"
          name="email"
          type="email"
          label="E-mail"
          icon={Mail}
          placeholder="voce@exemplo.com"
          autoComplete="email"
          required
        />
        <div>
          <AuthField
            id="password"
            name="password"
            type="password"
            label="Senha"
            icon={LockKeyhole}
            placeholder="Digite sua senha"
            autoComplete="current-password"
            minLength={8}
            required
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
