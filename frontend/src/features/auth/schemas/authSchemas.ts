import { z } from "zod";

const emailSchema = z
  .string()
  .trim()
  .min(1, "Informe seu e-mail.")
  .email("Informe um e-mail válido.");

const passwordSchema = z
  .string()
  .min(8, "Use pelo menos 8 caracteres.")
  .regex(/[a-z]/, "Inclua pelo menos uma letra minúscula.")
  .regex(/[A-Z]/, "Inclua pelo menos uma letra maiúscula.")
  .regex(/[0-9]/, "Inclua pelo menos um número.");

export const loginSchema = z.object({
  email: emailSchema,
  password: z.string().min(1, "Informe sua senha."),
});

export const registerSchema = z
  .object({
    name: z
      .string()
      .trim()
      .min(2, "Informe seu nome.")
      .max(100, "O nome deve ter no máximo 100 caracteres."),
    username: z
      .string()
      .trim()
      .min(3, "Use pelo menos 3 caracteres.")
      .max(30, "O usuário deve ter no máximo 30 caracteres.")
      .regex(
        /^[a-zA-Z0-9._]+$/,
        "Use somente letras, números, ponto ou sublinhado.",
      ),
    email: emailSchema,
    password: passwordSchema,
    passwordConfirmation: z.string().min(1, "Confirme sua senha."),
    acceptedTerms: z.boolean().refine((accepted) => accepted, {
      message: "Você precisa aceitar os termos para continuar.",
    }),
  })
  .refine((data) => data.password === data.passwordConfirmation, {
    message: "As senhas não coincidem.",
    path: ["passwordConfirmation"],
  });

export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
