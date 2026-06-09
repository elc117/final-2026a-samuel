import { z } from "zod";

const today = () => new Date().toISOString().slice(0, 10);

export const createChallengeSchema = z
  .object({
    title: z
      .string()
      .trim()
      .min(3, "Use pelo menos 3 caracteres.")
      .max(100, "O título deve ter no máximo 100 caracteres."),
    description: z
      .string()
      .trim()
      .max(1000, "A descrição deve ter no máximo 1000 caracteres."),
    period: z.enum([
      "WEEKLY",
      "QUARTERLY",
      "SEMIANNUAL",
      "ANNUAL",
      "CUSTOM",
    ]),
    endsAt: z.string(),
    allowMultipleCheckInsPerDay: z.boolean(),
  })
  .superRefine((data, context) => {
    if (data.period !== "CUSTOM") {
      return;
    }

    if (!data.endsAt) {
      context.addIssue({
        code: "custom",
        path: ["endsAt"],
        message: "Informe a data final personalizada.",
      });
    } else if (data.endsAt < today()) {
      context.addIssue({
        code: "custom",
        path: ["endsAt"],
        message: "A data final não pode estar no passado.",
      });
    }
  });

export type CreateChallengeFormData = z.infer<
  typeof createChallengeSchema
>;
