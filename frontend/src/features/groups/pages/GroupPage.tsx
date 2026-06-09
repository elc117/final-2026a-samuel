import { zodResolver } from "@hookform/resolvers/zod";
import {
  ArrowRight,
  Camera,
  Check,
  ChevronDown,
  Clock,
  Copy,
  Dumbbell,
  Image,
  ShieldCheck,
  Sparkles,
  Trophy,
  Upload,
  UserPlus,
  Users,
} from "lucide-react";
import React, { useEffect, useState } from "react";
import { useForm, useWatch } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { AuthenticatedHeader } from "../../auth/components/AuthenticatedHeader";
import { getCurrentUser } from "../../auth/services/authService";
import { GroupNavigation } from "../components/GroupNavigation";
import {
  createGroupSchema,
  type CreateGroupFormData,
} from "../schemas/groupSchemas";
import {
  createGroup,
  getCurrentGroup,
  type Group,
} from "../services/groupService";
import {
  ApiError,
  getApiErrorMessage,
} from "../../../services/apiClient";
import {
  ImageCompressionError,
  ImageCompressor,
} from "../../../shared/images/ImageCompressor";
import { Dropdown } from "../../../shared/components/Dropdown";
import { getGroupInviteLink } from "../services/groupInvitationService";
import {
  getCheckIns,
  type CheckIn,
} from "../../checkins/services/checkInService";

const GROUP_IMAGE_OPTIONS = {
  maxWidth: 1200,
  maxHeight: 1200,
  maxInputBytes: 8 * 1024 * 1024,
  maxOutputBytes: 1024 * 1024,
  outputType: "image/webp",
} as const;

export function GroupPage() {
  const navigate = useNavigate();
  const [group, setGroup] = useState<Group | null>(null);
  const [checkIns, setCheckIns] = useState<CheckIn[]>([]);
  const [currentUserCode, setCurrentUserCode] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [imageError, setImageError] = useState("");
  const [groupImage, setGroupImage] = useState<File>();
  const [imagePreviewUrl, setImagePreviewUrl] = useState("");
  const [isCompressing, setIsCompressing] = useState(false);
  const {
    register,
    handleSubmit,
    control,
    formState: { errors, isSubmitting },
  } = useForm<CreateGroupFormData>({
    resolver: zodResolver(createGroupSchema),
    mode: "onBlur",
    defaultValues: {
      name: "",
    },
  });

  const groupName = useWatch({ control, name: "name" });

  useEffect(() => {
    return () => {
      if (imagePreviewUrl) {
        URL.revokeObjectURL(imagePreviewUrl);
      }
    };
  }, [imagePreviewUrl]);

  useEffect(() => {
    let active = true;

    Promise.all([getCurrentGroup(), getCurrentUser(), getCheckIns()])
      .then(([currentGroup, currentUser, currentCheckIns]) => {
        if (active) {
          setGroup(currentGroup);
          setCurrentUserCode(currentUser.code);
          setCheckIns(currentCheckIns);
        }
      })
      .catch((error: unknown) => {
        if (!active) {
          return;
        }

        if (error instanceof ApiError && error.status === 401) {
          navigate("/login", { replace: true });
          return;
        }

        setLoadError(getApiErrorMessage(error));
      })
      .finally(() => {
        if (active) {
          setIsLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [navigate]);

  async function onSubmit(data: CreateGroupFormData) {
    setSubmitError("");

    try {
      const createdGroup = await createGroup({
        name: data.name,
        image: groupImage,
      });
      setGroup(createdGroup);
    }
    catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      setSubmitError(getApiErrorMessage(error));
    }
  }

  async function selectGroupImage(file?: File) {
    setImageError("");

    if (!file) {
      return;
    }

    setIsCompressing(true);

    try {
      const compressedImage = await ImageCompressor.compress(
        file,
        GROUP_IMAGE_OPTIONS,
      );
      setGroupImage(compressedImage);
      setImagePreviewUrl(URL.createObjectURL(compressedImage));
    }
    catch (error) {
      setGroupImage(undefined);
      setImagePreviewUrl("");
      setImageError(
        error instanceof ImageCompressionError
          ? error.message
          : "Não foi possível processar a imagem.",
      );
    }
    finally {
      setIsCompressing(false);
    }
  }

  if (isLoading) {
    return <GroupPageLoading />;
  }

  return (
    <main className="min-h-screen bg-zinc-50">
      <AuthenticatedHeader page="group" />

      {loadError ? (
        <section className="mx-auto max-w-3xl px-5 py-20 text-center sm:px-8">
          <p className="rounded-2xl bg-red-50 px-5 py-4 text-sm font-semibold text-red-700">
            {loadError}
          </p>
        </section>
      ) : group ? (
        <CurrentGroup
          group={group}
          isAdministrator={group.adminUserCode === currentUserCode}
          checkIns={checkIns}
        />
      ) : (
        <CreateGroup
          groupName={groupName}
          imagePreviewUrl={imagePreviewUrl}
          imageName={groupImage?.name}
          imageError={imageError}
          isCompressing={isCompressing}
          register={register}
          errors={errors}
          isSubmitting={isSubmitting}
          submitError={submitError}
          onImageSelected={selectGroupImage}
          onSubmit={handleSubmit(onSubmit)}
        />
      )}
    </main>
  );
}

type CreateGroupProps = {
  groupName: string;
  imagePreviewUrl: string;
  imageName?: string;
  imageError: string;
  isCompressing: boolean;
  register: ReturnType<typeof useForm<CreateGroupFormData>>["register"];
  errors: ReturnType<typeof useForm<CreateGroupFormData>>["formState"]["errors"];
  isSubmitting: boolean;
  submitError: string;
  onImageSelected: (file?: File) => Promise<void>;
  onSubmit: React.SubmitEventHandler<HTMLFormElement>;
};

function CreateGroup({
  groupName,
  imagePreviewUrl,
  imageName,
  imageError,
  isCompressing,
  register,
  errors,
  isSubmitting,
  submitError,
  onImageSelected,
  onSubmit,
}: CreateGroupProps) {
  return (
    <section className="mx-auto grid max-w-7xl gap-10 px-5 py-10 sm:px-8 sm:py-16 lg:grid-cols-[0.9fr_1.1fr] lg:items-center lg:py-24">
      <div className="max-w-xl">
        <span className="inline-flex items-center gap-2 rounded-full bg-brand-50 px-4 py-2 text-xs font-black uppercase tracking-[0.16em] text-brand-700">
          <Sparkles size={15} />
          Comece sua equipe
        </span>
        <h1 className="mt-5 text-4xl font-black tracking-[-0.045em] text-ink-950 sm:text-5xl">
          Crie o grupo que vai manter todo mundo em movimento.
        </h1>
        <p className="mt-5 max-w-lg text-base leading-7 text-zinc-500 sm:text-lg">
          Você será o administrador e poderá convidar até nove amigos para
          compartilhar treinos e participar dos desafios.
        </p>

        <div className="mt-8 grid gap-3 sm:grid-cols-2 lg:grid-cols-1 xl:grid-cols-2">
          <InfoCard
            icon={Users}
            title="Até 10 pessoas"
            description="Um grupo pequeno, próximo e motivador."
          />
          <InfoCard
            icon={ShieldCheck}
            title="Você administra"
            description="Organize membros, desafios e convites."
          />
        </div>
      </div>

      <div className="overflow-hidden rounded-[2rem] border border-zinc-200 bg-white shadow-2xl shadow-zinc-200/70">
        <div className="grid sm:grid-cols-[0.78fr_1.22fr]">
          <div className="relative min-h-60 overflow-hidden bg-brand-600 p-7 text-white sm:min-h-full">
            {imagePreviewUrl ? (
              <img
                src={imagePreviewUrl}
                alt=""
                className="absolute inset-0 size-full object-cover"
              />
            ) : (
              <div className="auth-grid absolute inset-0 opacity-40" />
            )}
            <div className="absolute inset-0 bg-gradient-to-t from-black/65 via-brand-700/20 to-transparent" />
            <div className="relative flex h-full min-h-48 flex-col justify-end">
              <span className="mb-auto grid size-11 place-items-center rounded-2xl bg-white/15 backdrop-blur">
                <Dumbbell size={22} />
              </span>
              <p className="text-xs font-extrabold uppercase tracking-[0.16em] text-white/70">
                Prévia do grupo
              </p>
              <h2 className="mt-2 break-words text-2xl font-black">
                {groupName.trim() || "Nome do seu grupo"}
              </h2>
            </div>
          </div>

          <form className="space-y-5 p-6 sm:p-8" onSubmit={onSubmit} noValidate>
            <div>
              <h2 className="text-2xl font-black tracking-[-0.03em] text-ink-950">
                Informações do grupo
              </h2>
              <p className="mt-1 text-sm text-zinc-500">
                Você poderá editar esses dados depois.
              </p>
            </div>

            <GroupField
              id="group-name"
              label="Nome do grupo"
              placeholder="Ex.: Treino das 6"
              error={errors.name?.message}
              {...register("name")}
            />
            <div>
              <span className="mb-2 block text-sm font-extrabold text-zinc-700">
                Imagem do grupo
              </span>
              <label
                htmlFor="group-image-file"
                className="flex min-h-28 cursor-pointer items-center gap-4 rounded-xl border border-dashed border-zinc-300 bg-zinc-50 p-4 transition hover:border-brand-400 hover:bg-brand-50/40"
              >
                <span className="grid size-11 shrink-0 place-items-center rounded-xl bg-white text-brand-600 shadow-sm">
                  <Upload size={20} />
                </span>
                <span className="min-w-0">
                  <strong className="block truncate text-sm text-zinc-800">
                    {isCompressing
                      ? "Comprimindo imagem..."
                      : imageName ?? "Escolher imagem do computador"}
                  </strong>
                  <span className="mt-1 block text-xs leading-5 text-zinc-500">
                    JPEG, PNG ou WebP. Até 8 MB antes da compressão.
                  </span>
                </span>
              </label>
              <input
                id="group-image-file"
                name="image"
                type="file"
                accept="image/jpeg,image/png,image/webp"
                className="sr-only"
                disabled={isCompressing || isSubmitting}
                onChange={(event) => {
                  void onImageSelected(event.target.files?.[0]);
                  event.target.value = "";
                }}
              />
              {imageError && (
                <p className="mt-2 text-sm font-medium text-red-600" role="alert">
                  {imageError}
                </p>
              )}
            </div>

            <div className="flex items-start gap-3 rounded-2xl bg-zinc-50 p-4 text-xs leading-5 text-zinc-500">
              <Camera className="mt-0.5 shrink-0 text-brand-600" size={17} />
              A imagem é opcional e será comprimida para WebP antes do envio.
              O arquivo armazenado terá no máximo 1 MB.
            </div>

            {submitError && (
              <p role="alert" className="rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                {submitError}
              </p>
            )}

            <button
              type="submit"
              disabled={isSubmitting || isCompressing}
              className="flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-brand-600 px-5 text-sm font-extrabold text-white shadow-lg shadow-red-200 transition hover:bg-brand-700 focus:outline-none focus:ring-4 focus:ring-brand-100 disabled:cursor-wait disabled:opacity-70"
            >
              {isSubmitting
                ? "Criando grupo..."
                : isCompressing
                  ? "Preparando imagem..."
                  : "Criar meu grupo"}
              {!isSubmitting && !isCompressing && <ArrowRight size={18} />}
            </button>
          </form>
        </div>
      </div>
    </section>
  );
}

function CurrentGroup({
  group,
  isAdministrator,
  checkIns,
}: {
  group: Group;
  isAdministrator: boolean;
  checkIns: CheckIn[];
}) {
  const [inviteLink, setInviteLink] = useState("");
  const [inviteError, setInviteError] = useState("");
  const [isLoadingLink, setIsLoadingLink] = useState(false);
  const [copied, setCopied] = useState(false);

  async function loadInviteLink() {
    setInviteError("");
    setCopied(false);
    setIsLoadingLink(true);

    try {
      const link = await getGroupInviteLink(group.id);
      setInviteLink(link);

      try {
        await navigator.clipboard.writeText(link);
        setCopied(true);
      } catch {
        setInviteError("Link gerado. Copie-o pelo botão ao lado.");
      }
    } catch (error) {
      setInviteError(getApiErrorMessage(error));
    } finally {
      setIsLoadingLink(false);
    }
  }

  async function copyInviteLink() {
    try {
      await navigator.clipboard.writeText(inviteLink);
      setCopied(true);
    } catch {
      setInviteError("Não foi possível copiar o link automaticamente.");
    }
  }

  return (
    <>
      <section className="mx-auto max-w-7xl px-5 pb-32 pt-10 sm:px-8 sm:pb-36 sm:pt-16">
        <div className="overflow-hidden rounded-[2rem] border border-zinc-200 bg-white shadow-xl shadow-zinc-200/60">
          <div className="relative min-h-80 bg-brand-600">
          {group.imageUrl ? (
            <img
              src={group.imageUrl}
              alt={`Imagem do grupo ${group.name}`}
              className="absolute inset-0 size-full object-cover"
            />
          ) : (
            <div className="auth-grid absolute inset-0 opacity-40" />
          )}
          <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/15 to-transparent" />
          <div className="absolute right-5 top-5 z-10 sm:right-7 sm:top-7">
            <Dropdown
              ariaLabel="Abrir ações do grupo"
              menuClassName="right-0 mt-2 w-72 overflow-hidden rounded-2xl border border-zinc-200 bg-white p-2 text-zinc-800 shadow-2xl"
              trigger={({ isOpen }) => (
                <span className="flex items-center gap-2 rounded-xl bg-white/95 px-4 py-2.5 text-sm font-extrabold text-zinc-800 shadow-lg backdrop-blur transition hover:bg-white">
                  Ações
                  <ChevronDown
                    size={17}
                    className={`transition ${isOpen ? "rotate-180" : ""}`}
                  />
                </span>
              )}
            >
              <div>
              <button
                type="button"
                role="menuitem"
                disabled={isLoadingLink}
                onClick={() => void loadInviteLink()}
                className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-left transition hover:bg-zinc-50 disabled:cursor-wait disabled:opacity-60"
              >
                <span className="grid size-9 shrink-0 place-items-center rounded-xl bg-brand-50 text-brand-600">
                  {copied ? <Check size={18} /> : <UserPlus size={18} />}
                </span>
                <span>
                  <strong className="block text-sm">
                    {isLoadingLink
                      ? "Carregando convite..."
                      : copied
                        ? "Link copiado"
                        : "Convidar pessoas"}
                  </strong>
                  <span className="mt-0.5 block text-xs text-zinc-500">
                    Copiar link de convite
                  </span>
                </span>
              </button>

              {inviteLink && (
                <div className="m-1 mt-2 flex items-center gap-2 rounded-xl bg-zinc-50 p-2 pl-3">
                  <span className="min-w-0 flex-1 truncate text-xs text-zinc-500">
                    {inviteLink}
                  </span>
                  <button
                    type="button"
                    role="menuitem"
                    onClick={() => void copyInviteLink()}
                    className="grid size-8 shrink-0 place-items-center rounded-lg bg-white text-zinc-700 shadow-sm transition hover:text-brand-600"
                    aria-label="Copiar link de convite"
                  >
                    {copied ? <Check size={16} /> : <Copy size={16} />}
                  </button>
                </div>
              )}

              {inviteError && (
                <p className="m-1 mt-2 rounded-xl bg-red-50 px-3 py-2 text-xs font-medium text-red-700">
                  {inviteError}
                </p>
              )}

              {isAdministrator && (
                <>
                  <div className="my-2 border-t border-zinc-100" />
                  <Link
                    to="/grupo/ranking"
                    role="menuitem"
                    className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-left transition hover:bg-zinc-50"
                  >
                    <span className="grid size-9 shrink-0 place-items-center rounded-xl bg-brand-50 text-brand-600">
                      <Trophy size={18} />
                    </span>
                    <span>
                      <strong className="block text-sm">Criar desafio</strong>
                      <span className="mt-0.5 block text-xs text-zinc-500">
                        Definir novo desafio
                      </span>
                    </span>
                  </Link>
                </>
              )}
              </div>
            </Dropdown>
          </div>
          <div className="absolute inset-x-0 bottom-0 p-7 text-white sm:p-10">
            <p className="text-xs font-black uppercase tracking-[0.18em] text-white/70">
              Seu grupo
            </p>
            <h1 className="mt-2 text-4xl font-black tracking-[-0.04em] sm:text-5xl">
              {group.name}
            </h1>
            <div className="mt-5 flex flex-wrap gap-3">
              <span className="inline-flex items-center gap-2 rounded-full bg-white/15 px-4 py-2 text-sm font-bold backdrop-blur">
                <Users size={17} />
                {group.memberCount} de 10 participantes
              </span>
              {isAdministrator && (
                <span className="inline-flex items-center gap-2 rounded-full bg-white/15 px-4 py-2 text-sm font-bold backdrop-blur">
                  <ShieldCheck size={17} />
                  Administrador
                </span>
              )}
            </div>
          </div>
          </div>

          <div className="p-7 sm:p-10">
            <div className="flex items-end justify-between gap-4">
              <div>
                <p className="text-xs font-black uppercase tracking-[0.16em] text-brand-600">
                  Atividade do grupo
                </p>
                <h2 className="mt-2 text-2xl font-black tracking-[-0.03em] text-ink-950">
                  Check-ins dos membros
                </h2>
              </div>
            </div>

            {checkIns.length === 0 ? (
              <div className="mt-6 grid min-h-56 place-items-center rounded-3xl border border-dashed border-zinc-300 bg-zinc-50 px-6 py-12 text-center">
                <div className="max-w-sm">
                  <span className="mx-auto grid size-12 place-items-center rounded-2xl bg-brand-50 text-brand-600">
                    <Dumbbell size={22} />
                  </span>
                  <h3 className="mt-4 font-extrabold text-ink-950">
                    Nenhum check-in por enquanto
                  </h3>
                  <p className="mt-2 text-sm leading-6 text-zinc-500">
                    As fotos e os exercícios publicados pelos membros aparecerão
                    aqui.
                  </p>
                </div>
              </div>
            ) : (
              <div className="mt-6 grid w-full gap-4">
                {checkIns.map((checkIn) => (
                  <CheckInCard key={checkIn.id} checkIn={checkIn} />
                ))}
              </div>
            )}
          </div>
        </div>
      </section>
      <GroupNavigation />
    </>
  );
}

type IconComponent = typeof Users;

function CheckInCard({ checkIn }: { checkIn: CheckIn }) {
  return (
    <article className="flex w-full overflow-hidden rounded-xl border border-zinc-200 bg-white p-2 shadow-sm transition hover:border-brand-200 hover:shadow-md">
      <Link
        to={`/grupo/check-ins/${checkIn.id}`}
        className="size-14 shrink-0 rounded-lg focus:outline-none focus:ring-4 focus:ring-brand-50"
      >
        <img
          src={checkIn.imageUrl}
          alt={`Check-in: ${checkIn.title}`}
          className="size-full rounded-lg border border-zinc-300 bg-zinc-100 object-cover"
        />
      </Link>
      <div className="flex min-w-0 flex-1 flex-col px-3 py-1 sm:px-4">
        <Link
          to={`/grupo/check-ins/${checkIn.id}`}
          className="truncate text-sm font-black text-ink-950 transition hover:text-brand-700 sm:text-base"
        >
          {checkIn.title}
        </Link>

        {checkIn.description && (
          <Link
            to={`/grupo/check-ins/${checkIn.id}`}
            className="mt-0.5 line-clamp-1 whitespace-pre-wrap text-xs leading-5 text-zinc-500 sm:text-sm"
          >
            {checkIn.description}
          </Link>
        )}

        <div className="mt-auto flex min-w-0 items-center justify-between gap-3 pt-1">
          <div className="flex min-w-0 items-center gap-2">
            <Link
              to={`/perfil/${checkIn.authorCode}`}
              className="grid size-6 shrink-0 overflow-hidden rounded-md bg-brand-50 text-[10px] font-black text-brand-700"
            >
              {checkIn.authorImageUrl ? (
                <img
                  src={checkIn.authorImageUrl}
                  alt=""
                  className="size-full object-cover"
                />
              ) : (
                <span className="m-auto">
                  {(checkIn.authorName?.trim().charAt(0) || "?").toUpperCase()}
                </span>
              )}
            </Link>
            <Link
              to={`/perfil/${checkIn.authorCode}`}
              className="truncate text-xs font-extrabold text-ink-950 transition hover:text-brand-700 sm:text-sm"
            >
              {checkIn.authorName ?? "Membro do grupo"}
            </Link>
          </div>

          <p className="flex shrink-0 items-center gap-1 text-[10px] text-zinc-500 sm:text-xs">
            <Clock size={12} />
            {new Intl.DateTimeFormat("pt-BR", {
              dateStyle: "short",
              timeStyle: "short",
            }).format(new Date(checkIn.createdAt))}
          </p>
        </div>
      </div>
    </article>
  );
}

function InfoCard({
  icon: Icon,
  title,
  description,
}: {
  icon: IconComponent;
  title: string;
  description: string;
}) {
  return (
    <div className="flex gap-3 rounded-2xl border border-zinc-200 bg-white p-4">
      <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-brand-50 text-brand-600">
        <Icon size={19} />
      </span>
      <div>
        <h2 className="text-sm font-extrabold text-ink-950">{title}</h2>
        <p className="mt-1 text-xs leading-5 text-zinc-500">{description}</p>
      </div>
    </div>
  );
}

type GroupFieldProps = React.InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  error?: string;
};

function GroupField({
  id,
  label,
  error,
  ...inputProps
}: GroupFieldProps) {
  return (
    <div>
      <label htmlFor={id} className="mb-2 block text-sm font-extrabold text-zinc-700">
        {label}
      </label>
      <div className="relative">
        <Image
          className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-zinc-400"
          size={18}
        />
        <input
          id={id}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${id}-error` : undefined}
          className="h-12 w-full rounded-xl border border-zinc-200 bg-white pl-11 pr-4 text-sm outline-none transition placeholder:text-zinc-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-50"
          {...inputProps}
        />
      </div>
      {error && (
        <p id={`${id}-error`} className="mt-2 text-sm font-medium text-red-600">
          {error}
        </p>
      )}
    </div>
  );
}

function GroupPageLoading() {
  return (
    <main className="grid min-h-screen place-items-center bg-zinc-50">
      <div className="text-center">
        <span className="mx-auto grid size-14 animate-pulse place-items-center rounded-2xl bg-brand-600 text-white">
          <Dumbbell size={26} />
        </span>
        <p className="mt-4 text-sm font-bold text-zinc-500">
          Carregando seu grupo...
        </p>
      </div>
    </main>
  );
}
