import { z } from "zod";

export const createGroupSchema = z.object({
  name: z
    .string()
    .trim()
    .min(3, "Use pelo menos 3 caracteres.")
    .max(100, "O nome deve ter no máximo 100 caracteres."),
});

export type CreateGroupFormData = z.infer<typeof createGroupSchema>;
