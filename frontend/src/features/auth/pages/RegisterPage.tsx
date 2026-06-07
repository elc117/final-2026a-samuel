import { zodResolver } from "@hookform/resolvers/zod";
import { AtSign, LockKeyhole, Mail, UserRound } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link } from "react-router-dom";
import { AuthLayout } from "../../../layouts/AuthLayout";
import { getApiErrorMessage } from "../../../services/apiClient";
import { AuthField } from "../components/AuthField";
import {
  registerSchema,
  type RegisterFormData,
} from "../schemas/authSchemas";
import { register as registerUser } from "../services/authService";

export function RegisterPage() {
  const [message, setMessage] = useState("");
  const [submitError, setSubmitError] = useState("");
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    mode: "onBlur",
    defaultValues: {
      name: "",
      username: "",
      email: "",
      password: "",
      passwordConfirmation: "",
      acceptedTerms: false,
    },
  });

  async function onSubmit(data: RegisterFormData) {
    setMessage("");
    setSubmitError("");

    try {
      const response = await registerUser({
        name: data.name,
        username: data.username,
        email: data.email,
        password: data.password,
      });

      setMessage(`Conta criada com sucesso. Bem-vindo, ${response.user.name}!`);
    } catch (error) {
      setSubmitError(getApiErrorMessage(error));
    }
  }

  return (
    <AuthLayout
      eyebrow="Comece agora"
      title="Crie sua conta"
      description="Monte seu perfil e encontre motivação junto do seu grupo de treino."
    >
      <form
        className="space-y-4"
        onSubmit={handleSubmit(onSubmit)}
        noValidate
      >
        <div className="grid gap-4 sm:grid-cols-2">
          <AuthField
            id="name"
            label="Nome"
            icon={UserRound}
            placeholder="Seu nome"
            autoComplete="name"
            error={errors.name?.message}
            {...register("name")}
          />
          <AuthField
            id="username"
            label="Usuário"
            icon={AtSign}
            placeholder="seu.usuario"
            autoComplete="username"
            error={errors.username?.message}
            {...register("username")}
          />
        </div>
        <AuthField
          id="register-email"
          type="email"
          label="E-mail"
          icon={Mail}
          placeholder="voce@exemplo.com"
          autoComplete="email"
          error={errors.email?.message}
          {...register("email")}
        />
        <AuthField
          id="register-password"
          type="password"
          label="Senha"
          icon={LockKeyhole}
          placeholder="8+ caracteres, maiúscula, número e símbolo"
          autoComplete="new-password"
          error={errors.password?.message}
          {...register("password")}
        />
        <AuthField
          id="password-confirmation"
          type="password"
          label="Confirmar senha"
          icon={LockKeyhole}
          placeholder="Digite a senha novamente"
          autoComplete="new-password"
          error={errors.passwordConfirmation?.message}
          {...register("passwordConfirmation")}
        />

        <label className="flex cursor-pointer items-start gap-3 pt-1 text-sm leading-5 text-zinc-500">
          <input
            type="checkbox"
            {...register("acceptedTerms")}
            aria-invalid={Boolean(errors.acceptedTerms)}
            aria-describedby={
              errors.acceptedTerms ? "accepted-terms-error" : undefined
            }
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
        {errors.acceptedTerms && (
          <p
            id="accepted-terms-error"
            className="-mt-2 text-sm font-medium text-red-600"
          >
            {errors.acceptedTerms.message}
          </p>
        )}

        {submitError && (
          <p role="alert" className="rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {submitError}
          </p>
        )}

        {message && (
          <p role="status" className="rounded-xl bg-green-50 px-4 py-3 text-sm font-medium text-green-700">
            {message}
          </p>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
          className="flex h-12 w-full items-center justify-center rounded-xl bg-brand-600 px-5 text-sm font-extrabold text-white shadow-lg shadow-red-200 transition hover:bg-brand-700 focus:outline-none focus:ring-4 focus:ring-brand-100 disabled:cursor-wait disabled:opacity-70"
        >
          {isSubmitting ? "Criando conta..." : "Criar minha conta"}
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
