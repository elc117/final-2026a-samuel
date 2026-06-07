import { AtSign, LockKeyhole, Mail, UserRound } from "lucide-react";
import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { AuthLayout } from "../../../layouts/AuthLayout";
import { AuthField } from "../components/AuthField";

type RegisterErrors = {
  password?: string;
  passwordConfirmation?: string;
};

export function RegisterPage() {
  const [errors, setErrors] = useState<RegisterErrors>({});
  const [message, setMessage] = useState("");

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const password = String(form.get("password"));
    const passwordConfirmation = String(form.get("passwordConfirmation"));
    const nextErrors: RegisterErrors = {};

    if (password.length < 8) {
      nextErrors.password = "Use pelo menos 8 caracteres.";
    }
    if (password !== passwordConfirmation) {
      nextErrors.passwordConfirmation = "As senhas não coincidem.";
    }

    setErrors(nextErrors);
    setMessage(
      Object.keys(nextErrors).length === 0
        ? "Cadastro validado. A integração com a API será conectada em seguida."
        : "",
    );
  }

  return (
    <AuthLayout
      eyebrow="Comece agora"
      title="Crie sua conta"
      description="Monte seu perfil e encontre motivação junto do seu grupo de treino."
    >
      <form className="space-y-4" onSubmit={handleSubmit} noValidate>
        <div className="grid gap-4 sm:grid-cols-2">
          <AuthField
            id="name"
            name="name"
            label="Nome"
            icon={UserRound}
            placeholder="Seu nome"
            autoComplete="name"
            required
          />
          <AuthField
            id="username"
            name="username"
            label="Usuário"
            icon={AtSign}
            placeholder="seu.usuario"
            autoComplete="username"
            pattern="[a-zA-Z0-9._]+"
            required
          />
        </div>
        <AuthField
          id="register-email"
          name="email"
          type="email"
          label="E-mail"
          icon={Mail}
          placeholder="voce@exemplo.com"
          autoComplete="email"
          required
        />
        <AuthField
          id="register-password"
          name="password"
          type="password"
          label="Senha"
          icon={LockKeyhole}
          placeholder="Mínimo de 8 caracteres"
          autoComplete="new-password"
          error={errors.password}
          required
        />
        <AuthField
          id="password-confirmation"
          name="passwordConfirmation"
          type="password"
          label="Confirmar senha"
          icon={LockKeyhole}
          placeholder="Digite a senha novamente"
          autoComplete="new-password"
          error={errors.passwordConfirmation}
          required
        />

        <label className="flex cursor-pointer items-start gap-3 pt-1 text-sm leading-5 text-zinc-500">
          <input
            type="checkbox"
            required
            className="mt-0.5 size-4 shrink-0 accent-brand-600"
          />
          <span>
            Concordo com os{" "}
            <button type="button" className="font-bold text-brand-600 hover:underline">
              Termos de Uso
            </button>{" "}
            e a{" "}
            <button type="button" className="font-bold text-brand-600 hover:underline">
              Política de Privacidade
            </button>
            .
          </span>
        </label>

        {message && (
          <p role="status" className="rounded-xl bg-brand-50 px-4 py-3 text-sm text-brand-700">
            {message}
          </p>
        )}

        <button
          type="submit"
          className="flex h-12 w-full items-center justify-center rounded-xl bg-brand-600 px-5 text-sm font-extrabold text-white shadow-lg shadow-red-200 transition hover:bg-brand-700 focus:outline-none focus:ring-4 focus:ring-brand-100"
        >
          Criar minha conta
        </button>
      </form>

      <p className="mt-7 text-center text-sm text-zinc-500">
        Já possui uma conta?{" "}
        <Link to="/login" className="font-extrabold text-brand-600 hover:text-brand-700">
          Entrar
        </Link>
      </p>
    </AuthLayout>
  );
}
