import { zodResolver } from "@hookform/resolvers/zod";
import {
  ArrowLeft,
  CalendarDays,
  CheckCircle2,
  Flag,
  Medal,
  ShieldCheck,
  Trophy,
} from "lucide-react";
import type React from "react";
import { useEffect, useState } from "react";
import {
  Controller,
  useForm,
  useWatch,
  type Control,
} from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { Select, type SelectOption } from "../../../shared/components/Select";
import { AuthenticatedHeader } from "../../auth/components/AuthenticatedHeader";
import { getCurrentUser } from "../../auth/services/authService";
import { GroupNavigation } from "../../groups/components/GroupNavigation";
import {
  getCurrentGroup,
  type Group,
} from "../../groups/services/groupService";
import { getApiErrorMessage } from "../../../services/apiClient";
import {
  createChallengeSchema,
  type CreateChallengeFormData,
} from "../schemas/challengeSchemas";
import {
  createChallenge,
  endCurrentChallenge,
  getCurrentChallenge,
  type Challenge,
  type ChallengePeriod,
} from "../services/challengeService";

const periodLabels: Record<ChallengePeriod, string> = {
  WEEKLY: "Semanal",
  QUARTERLY: "Trimestral",
  SEMIANNUAL: "Semestral",
  ANNUAL: "Anual",
  CUSTOM: "Personalizado",
};

const periodOptions: SelectOption[] = [
  { value: "WEEKLY", label: "Semanal" },
  { value: "QUARTERLY", label: "Trimestral" },
  { value: "SEMIANNUAL", label: "Semestral" },
  { value: "ANNUAL", label: "Anual" },
  { value: "CUSTOM", label: "Data personalizada" },
];

export function ChallengePage() {
  const navigate = useNavigate();
  const [group, setGroup] = useState<Group>();
  const [challenge, setChallenge] = useState<Challenge | null>(null);
  const [currentUserId, setCurrentUserId] = useState<number>();
  const [loadError, setLoadError] = useState("");
  const [actionError, setActionError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isEnding, setIsEnding] = useState(false);
  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors, isSubmitting },
  } = useForm<CreateChallengeFormData>({
    resolver: zodResolver(createChallengeSchema),
    defaultValues: {
      title: "",
      description: "",
      period: "WEEKLY",
      endsAt: "",
      allowMultipleCheckInsPerDay: false,
    },
  });
  const selectedPeriod = useWatch({ control, name: "period" });

  useEffect(() => {
    let active = true;

    Promise.all([
      getCurrentGroup(),
      getCurrentUser(),
      getCurrentChallenge(),
    ])
      .then(([currentGroup, currentUser, currentChallenge]) => {
        if (!active) return;
        if (!currentGroup) {
          navigate("/grupo", { replace: true });
          return;
        }
        setGroup(currentGroup);
        setCurrentUserId(currentUser.id);
        setChallenge(currentChallenge);
      })
      .catch((error) => active && setLoadError(getApiErrorMessage(error)))
      .finally(() => active && setIsLoading(false));

    return () => {
      active = false;
    };
  }, [navigate]);

  async function onSubmit(data: CreateChallengeFormData) {
    setActionError("");
    try {
      const created = await createChallenge({
        ...data,
        description: data.description || undefined,
        endsAt: data.period === "CUSTOM" ? data.endsAt : undefined,
      });
      setChallenge(created);
      reset();
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    }
  }

  async function endChallenge() {
    setActionError("");
    setIsEnding(true);
    try {
      await endCurrentChallenge();
      setChallenge(null);
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    } finally {
      setIsEnding(false);
    }
  }

  const isAdministrator = group?.adminUserId === currentUserId;

  return (
    <main className="min-h-screen bg-zinc-50">
      <AuthenticatedHeader page="group" />
      <section className="mx-auto max-w-5xl px-5 pb-32 pt-10 sm:px-8 sm:pb-36 sm:pt-16">
        <Link
          to="/grupo"
          className="inline-flex items-center gap-2 text-sm font-extrabold text-zinc-500 transition hover:text-brand-700"
        >
          <ArrowLeft size={18} />
          Voltar aos check-ins
        </Link>

        {isLoading ? (
          <p className="mt-8 text-sm font-bold text-zinc-500">
            Carregando desafio...
          </p>
        ) : loadError ? (
          <ErrorMessage message={loadError} />
        ) : challenge ? (
          <ActiveChallenge
            challenge={challenge}
            isAdministrator={isAdministrator}
            isEnding={isEnding}
            error={actionError}
            onEnd={() => void endChallenge()}
          />
        ) : isAdministrator ? (
          <ChallengeForm
            register={register}
            control={control}
            errors={errors}
            isSubmitting={isSubmitting}
            error={actionError}
            selectedPeriod={selectedPeriod}
            onSubmit={handleSubmit(onSubmit)}
          />
        ) : (
          <EmptyChallenge />
        )}
      </section>
      <GroupNavigation />
    </main>
  );
}

function ActiveChallenge({
  challenge,
  isAdministrator,
  isEnding,
  error,
  onEnd,
}: {
  challenge: Challenge;
  isAdministrator: boolean;
  isEnding: boolean;
  error: string;
  onEnd: () => void;
}) {
  return (
    <div className="mt-6 overflow-hidden rounded-[2rem] border border-zinc-200 bg-white shadow-xl shadow-zinc-200/60">
      <div className="bg-zinc-950 p-7 text-white sm:p-9">
        <div className="flex flex-wrap items-start justify-between gap-5">
          <div>
            <p className="text-xs font-black uppercase tracking-[0.16em] text-brand-500">
              Desafio {periodLabels[challenge.period].toLowerCase()}
            </p>
            <h1 className="mt-2 text-3xl font-black tracking-[-0.04em] sm:text-4xl">
              {challenge.title}
            </h1>
            {challenge.description && (
              <p className="mt-3 max-w-2xl text-sm leading-6 text-zinc-400">
                {challenge.description}
              </p>
            )}
          </div>
          {isAdministrator && (
            <button
              type="button"
              disabled={isEnding}
              onClick={onEnd}
              className="rounded-xl border border-white/15 px-4 py-2 text-sm font-extrabold text-white transition hover:bg-white/10 disabled:opacity-50"
            >
              {isEnding ? "Encerrando..." : "Encerrar desafio"}
            </button>
          )}
        </div>

        <div className="mt-7 flex flex-wrap gap-3 text-sm">
          <InfoPill icon={CalendarDays}>
            Até {formatDate(challenge.endsAt)}
          </InfoPill>
          <InfoPill icon={CheckCircle2}>
            {challenge.allowMultipleCheckInsPerDay
              ? "Todos os check-ins contam"
              : "1 check-in por dia"}
          </InfoPill>
        </div>
      </div>

      <div className="p-6 sm:p-8">
        <h2 className="text-xl font-black text-ink-950">Ranking atual</h2>
        <div className="mt-5 divide-y divide-zinc-100">
          {challenge.ranking.map((entry, index) => (
            <div
              key={entry.userId}
              className="flex items-center gap-4 py-4 first:pt-0 last:pb-0"
            >
              <span className="w-7 text-center text-sm font-black text-zinc-400">
                {index < 3 ? (
                  <Medal size={19} className="mx-auto text-brand-600" />
                ) : (
                  index + 1
                )}
              </span>
              <Link
                to={`/perfil/${entry.userId}`}
                className="grid size-10 shrink-0 overflow-hidden rounded-xl bg-brand-50 text-sm font-black text-brand-700"
              >
                {entry.profileImageUrl ? (
                  <img
                    src={entry.profileImageUrl}
                    alt=""
                    className="size-full object-cover"
                  />
                ) : (
                  <span className="m-auto">
                    {entry.name.charAt(0).toUpperCase()}
                  </span>
                )}
              </Link>
              <Link
                to={`/perfil/${entry.userId}`}
                className="min-w-0 flex-1 truncate text-sm font-extrabold text-ink-950 transition hover:text-brand-700"
              >
                {entry.name}
              </Link>
              <strong className="text-sm text-brand-700">
                {entry.score} {entry.score === 1 ? "check-in" : "check-ins"}
              </strong>
            </div>
          ))}
        </div>
        {error && <ErrorMessage message={error} />}
      </div>
    </div>
  );
}

type FormProps = {
  register: ReturnType<typeof useForm<CreateChallengeFormData>>["register"];
  control: Control<CreateChallengeFormData>;
  errors: ReturnType<
    typeof useForm<CreateChallengeFormData>
  >["formState"]["errors"];
  isSubmitting: boolean;
  error: string;
  selectedPeriod: ChallengePeriod;
  onSubmit: React.SubmitEventHandler<HTMLFormElement>;
};

function ChallengeForm({
  register,
  control,
  errors,
  isSubmitting,
  error,
  selectedPeriod,
  onSubmit,
}: FormProps) {
  return (
    <div className="mt-6 overflow-hidden rounded-[2rem] border border-zinc-200 bg-white shadow-xl shadow-zinc-200/60">
      <div className="border-b border-zinc-100 p-6 sm:p-8">
        <span className="grid size-12 place-items-center rounded-2xl bg-brand-50 text-brand-600">
          <Trophy size={23} />
        </span>
        <h1 className="mt-5 text-3xl font-black tracking-[-0.04em] text-ink-950">
          Criar desafio
        </h1>
        <p className="mt-2 text-sm leading-6 text-zinc-500">
          Todos os integrantes do grupo participarão automaticamente.
        </p>
      </div>

      <form className="space-y-5 p-6 sm:p-8" onSubmit={onSubmit} noValidate>
        <Field label="Título" error={errors.title?.message}>
          <input
            {...register("title")}
            placeholder="Ex.: Consistência no treino"
            className={inputClass}
          />
        </Field>

        <Field label="Descrição (opcional)" error={errors.description?.message}>
          <textarea
            {...register("description")}
            rows={4}
            placeholder="Explique o objetivo do desafio..."
            className={`${inputClass} h-auto resize-none py-3`}
          />
        </Field>

        <Field label="Período" error={errors.period?.message}>
          <Controller
            name="period"
            control={control}
            render={({ field }) => (
              <Select
                ref={field.ref}
                name={field.name}
                value={field.value}
                options={periodOptions}
                ariaLabel="Período do desafio"
                onBlur={field.onBlur}
                onChange={field.onChange}
              />
            )}
          />
        </Field>

        {selectedPeriod === "CUSTOM" && (
          <Field
            label="Data final personalizada"
            error={errors.endsAt?.message}
          >
            <input
              type="date"
              min={new Date().toISOString().slice(0, 10)}
              {...register("endsAt")}
              className={inputClass}
            />
          </Field>
        )}

        <fieldset className="rounded-2xl border border-zinc-200 p-4 sm:p-5">
          <legend className="px-2 text-sm font-black text-ink-950">
            Regras do desafio
          </legend>
          <label className="flex cursor-pointer items-start gap-3">
            <input
              type="checkbox"
              {...register("allowMultipleCheckInsPerDay")}
              className="mt-0.5 size-4 accent-brand-600"
            />
            <span>
              <strong className="block text-sm text-ink-950">
                Contar mais de um check-in por dia
              </strong>
              <span className="mt-1 block text-xs leading-5 text-zinc-500">
                Quando desativado, cada pessoa soma no máximo um ponto por dia.
              </span>
            </span>
          </label>
        </fieldset>

        {error && <ErrorMessage message={error} />}

        <button
          type="submit"
          disabled={isSubmitting}
          className="flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-brand-600 px-5 text-sm font-extrabold text-white transition hover:bg-brand-700 disabled:opacity-60 sm:w-auto"
        >
          <Flag size={18} />
          {isSubmitting ? "Criando..." : "Criar desafio"}
        </button>
      </form>
    </div>
  );
}

function EmptyChallenge() {
  return (
    <div className="mt-6 grid min-h-80 place-items-center rounded-[2rem] border border-zinc-200 bg-white p-8 text-center shadow-xl shadow-zinc-200/60">
      <div>
        <ShieldCheck className="mx-auto text-zinc-400" size={35} />
        <h1 className="mt-4 text-2xl font-black text-ink-950">
          Nenhum desafio ativo
        </h1>
        <p className="mt-2 text-sm text-zinc-500">
          O administrador do grupo ainda não criou um desafio.
        </p>
      </div>
    </div>
  );
}

function InfoPill({
  icon: Icon,
  children,
}: {
  icon: typeof CalendarDays;
  children: React.ReactNode;
}) {
  return (
    <span className="inline-flex items-center gap-2 rounded-full bg-white/10 px-4 py-2">
      <Icon size={16} />
      {children}
    </span>
  );
}

function Field({
  label,
  error,
  children,
}: {
  label: string;
  error?: string;
  children: React.ReactNode;
}) {
  return (
    <label className="block">
      <span className="mb-2 block text-sm font-extrabold text-zinc-700">
        {label}
      </span>
      {children}
      {error && <span className="mt-2 block text-sm text-red-600">{error}</span>}
    </label>
  );
}

function ErrorMessage({ message }: { message: string }) {
  return (
    <p className="mt-5 rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
      {message}
    </p>
  );
}

const inputClass =
  "h-12 w-full rounded-xl border border-zinc-300 bg-white px-4 text-sm outline-none transition focus:border-brand-500 focus:ring-4 focus:ring-brand-50";

function formatDate(value: string) {
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "long",
    timeZone: "UTC",
  }).format(new Date(`${value}T00:00:00Z`));
}
