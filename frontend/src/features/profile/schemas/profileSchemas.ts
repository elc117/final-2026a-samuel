import { z } from "zod";

export const updateProfileSchema = z.object({
  name: z
    .string()
    .trim()
    .min(2, "Use pelo menos 2 caracteres.")
    .max(100, "O nome deve ter no máximo 100 caracteres."),
});

export type UpdateProfileFormData = z.infer<typeof updateProfileSchema>;
