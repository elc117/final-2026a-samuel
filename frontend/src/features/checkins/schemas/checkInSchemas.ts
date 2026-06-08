import { z } from "zod";

export const createCheckInSchema = z.object({
  title: z
    .string()
    .trim()
    .min(3, "Use pelo menos 3 caracteres.")
    .max(100, "O título deve ter no máximo 100 caracteres."),
  description: z
    .string()
    .trim()
    .max(1000, "A descrição deve ter no máximo 1000 caracteres."),
});

export type CreateCheckInFormData = z.infer<typeof createCheckInSchema>;
