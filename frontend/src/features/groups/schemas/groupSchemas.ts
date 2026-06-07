import { z } from "zod";

export const createGroupSchema = z.object({
  name: z
    .string()
    .trim()
    .min(3, "Use pelo menos 3 caracteres.")
    .max(100, "O nome deve ter no máximo 100 caracteres."),
  imageUrl: z
    .string()
    .trim()
    .max(2048, "A URL deve ter no máximo 2048 caracteres.")
    .refine(
      (value) => value === "" || URL.canParse(value),
      "Informe uma URL válida.",
    ),
});

export type CreateGroupFormData = z.infer<typeof createGroupSchema>;
