import { zodResolver } from "@hookform/resolvers/zod";
import {
  ArrowLeft,
  Camera,
  Check,
  Dumbbell,
  Save,
  UserRound,
  Users,
} from "lucide-react";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { BrandMark } from "../../auth/components/BrandMark";
import {
  ImageCompressionError,
  ImageCompressor,
} from "../../../shared/images/ImageCompressor";
import {
  ApiError,
  getApiErrorMessage,
} from "../../../services/apiClient";
import {
  updateProfileSchema,
  type UpdateProfileFormData,
} from "../schemas/profileSchemas";
import {
  getProfile,
  updateProfile,
  type UserProfile,
} from "../services/profileService";

const PROFILE_IMAGE_OPTIONS = {
  maxWidth: 600,
  maxHeight: 600,
  maxInputBytes: 5 * 1024 * 1024,
  maxOutputBytes: 512 * 1024,
  outputType: "image/webp",
} as const;

export function ProfilePage() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState<UserProfile>();
  const [profileImage, setProfileImage] = useState<File>();
  const [previewUrl, setPreviewUrl] = useState("");
  const [loadError, setLoadError] = useState("");
  const [imageError, setImageError] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [saved, setSaved] = useState(false);
  const [isCompressing, setIsCompressing] = useState(false);
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<UpdateProfileFormData>({
    resolver: zodResolver(updateProfileSchema),
    mode: "onBlur",
    defaultValues: { name: "" },
  });

  useEffect(() => {
    let active = true;

    getProfile()
      .then((response) => {
        if (active) {
          setProfile(response);
          reset({ name: response.name });
        }
      })
      .catch((error: unknown) => {
        if (error instanceof ApiError && error.status === 401) {
          navigate("/login", { replace: true });
          return;
        }

        if (active) {
          setLoadError(getApiErrorMessage(error));
        }
      });

    return () => {
      active = false;
    };
  }, [navigate, reset]);

  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  async function selectImage(file?: File) {
    setImageError("");
    setSaved(false);

    if (!file) {
      return;
    }

    setIsCompressing(true);

    try {
      const compressed = await ImageCompressor.compress(
        file,
        PROFILE_IMAGE_OPTIONS,
      );
      setProfileImage(compressed);
      setPreviewUrl(URL.createObjectURL(compressed));
    } catch (error) {
      setProfileImage(undefined);
      setPreviewUrl("");
      setImageError(
        error instanceof ImageCompressionError
          ? error.message
          : "Não foi possível processar a imagem.",
      );
    } finally {
      setIsCompressing(false);
    }
  }

  async function onSubmit(data: UpdateProfileFormData) {
    setSubmitError("");
    setSaved(false);

    try {
      const updated = await updateProfile({
        name: data.name,
        image: profileImage,
      });
      setProfile(updated);
      setProfileImage(undefined);
      setPreviewUrl("");
      reset({ name: updated.name });
      setSaved(true);
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        navigate("/login", { replace: true });
        return;
      }

      setSubmitError(getApiErrorMessage(error));
    }
  }

  if (!profile && !loadError) {
    return <ProfileLoading />;
  }

  return (
    <main className="min-h-screen bg-zinc-50">
      <header className="border-b border-zinc-200 bg-white">
        <div className="mx-auto flex h-20 max-w-6xl items-center justify-between px-5 sm:px-8">
          <BrandMark />
          <Link
            to="/grupo"
            className="inline-flex items-center gap-2 rounded-xl px-3 py-2 text-sm font-extrabold text-zinc-600 transition hover:bg-zinc-100 hover:text-zinc-950"
          >
            <ArrowLeft size={18} />
            Voltar ao grupo
          </Link>
        </div>
      </header>

      {loadError || !profile ? (
        <section className="mx-auto max-w-3xl px-5 py-20 text-center sm:px-8">
          <p className="rounded-2xl bg-red-50 px-5 py-4 text-sm font-semibold text-red-700">
            {loadError}
          </p>
        </section>
      ) : (
        <section className="mx-auto max-w-6xl px-5 py-10 sm:px-8 sm:py-16">
          <div className="overflow-hidden rounded-[2rem] border border-zinc-200 bg-white shadow-xl shadow-zinc-200/60">
            <div className="auth-grid relative h-44 bg-brand-600 sm:h-56">
              <div className="absolute inset-0 bg-gradient-to-br from-brand-700/20 to-black/20" />
            </div>

            <div className="px-6 pb-8 sm:px-10 sm:pb-10">
              <div className="-mt-20 flex flex-col gap-5 sm:-mt-16 sm:flex-row sm:items-end sm:justify-between">
                <div className="flex flex-col items-start gap-4 sm:flex-row sm:items-end">
                  <div className="relative">
                    <div className="grid size-36 overflow-hidden rounded-[2rem] border-4 border-white bg-brand-50 text-brand-600 shadow-xl">
                      {previewUrl || profile.profileImageUrl ? (
                        <img
                          src={previewUrl || profile.profileImageUrl || ""}
                          alt={`Foto de ${profile.name}`}
                          className="size-full object-cover"
                        />
                      ) : (
                        <UserRound className="m-auto" size={58} />
                      )}
                    </div>
                    <label
                      htmlFor="profile-image"
                      className="absolute -bottom-2 -right-2 grid size-11 cursor-pointer place-items-center rounded-2xl border-4 border-white bg-brand-600 text-white shadow-lg transition hover:bg-brand-700"
                      title="Alterar foto"
                    >
                      <Camera size={18} />
                    </label>
                    <input
                      id="profile-image"
                      type="file"
                      accept="image/jpeg,image/png,image/webp"
                      className="sr-only"
                      disabled={isCompressing || isSubmitting}
                      onChange={(event) => {
                        void selectImage(event.target.files?.[0]);
                        event.target.value = "";
                      }}
                    />
                  </div>

                  <div className="pb-1">
                    <p className="text-sm font-bold text-brand-600">
                      @{profile.username}
                    </p>
                    <h1 className="mt-1 text-3xl font-black tracking-[-0.04em] text-ink-950 sm:text-4xl">
                      {profile.name}
                    </h1>
                  </div>
                </div>

                <div className="flex min-w-40 items-center gap-3 rounded-2xl bg-brand-50 px-5 py-4 text-brand-700">
                  <Users size={24} />
                  <div>
                    <strong className="block text-2xl font-black leading-none">
                      {profile.friendCount}
                    </strong>
                    <span className="text-xs font-extrabold uppercase tracking-wider">
                      {profile.friendCount === 1 ? "amigo" : "amigos"}
                    </span>
                  </div>
                </div>
              </div>

              <div className="mt-10 grid gap-8 lg:grid-cols-[1fr_0.72fr]">
                <form
                  className="rounded-3xl border border-zinc-200 p-5 sm:p-7"
                  onSubmit={handleSubmit(onSubmit)}
                  noValidate
                >
                  <h2 className="text-xl font-black text-ink-950">
                    Informações do perfil
                  </h2>
                  <p className="mt-1 text-sm text-zinc-500">
                    Atualize como seu nome aparece para seus amigos.
                  </p>

                  <div className="mt-6">
                    <label
                      htmlFor="profile-name"
                      className="mb-2 block text-sm font-extrabold text-zinc-700"
                    >
                      Nome
                    </label>
                    <input
                      id="profile-name"
                      className="h-12 w-full rounded-xl border border-zinc-200 px-4 text-sm outline-none transition focus:border-brand-500 focus:ring-4 focus:ring-brand-50"
                      {...register("name", {
                        onChange: () => setSaved(false),
                      })}
                    />
                    {errors.name && (
                      <p className="mt-2 text-sm font-medium text-red-600">
                        {errors.name.message}
                      </p>
                    )}
                  </div>

                  {imageError && (
                    <p className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                      {imageError}
                    </p>
                  )}
                  {submitError && (
                    <p className="mt-4 rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                      {submitError}
                    </p>
                  )}
                  {saved && (
                    <p className="mt-4 flex items-center gap-2 rounded-xl bg-emerald-50 px-4 py-3 text-sm font-bold text-emerald-700">
                      <Check size={17} />
                      Perfil atualizado.
                    </p>
                  )}

                  <button
                    type="submit"
                    disabled={isSubmitting || isCompressing}
                    className="mt-6 inline-flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-brand-600 px-5 text-sm font-extrabold text-white shadow-lg shadow-red-200 transition hover:bg-brand-700 disabled:cursor-wait disabled:opacity-70 sm:w-auto"
                  >
                    <Save size={18} />
                    {isSubmitting
                      ? "Salvando..."
                      : isCompressing
                        ? "Preparando foto..."
                        : "Salvar alterações"}
                  </button>
                </form>

                <aside className="rounded-3xl bg-zinc-950 p-6 text-white sm:p-7">
                  <span className="grid size-12 place-items-center rounded-2xl bg-white/10">
                    <Dumbbell size={23} />
                  </span>
                  <h2 className="mt-6 text-2xl font-black">
                    Seu perfil acompanha seus treinos.
                  </h2>
                  <p className="mt-3 text-sm leading-6 text-zinc-400">
                    Sua foto e seu nome aparecerão nas publicações,
                    comentários e desafios do grupo.
                  </p>
                  <p className="mt-6 text-xs leading-5 text-zinc-500">
                    A foto é comprimida para WebP e armazenada de forma
                    privada, com limite de 512 KB.
                  </p>
                </aside>
              </div>
            </div>
          </div>
        </section>
      )}
    </main>
  );
}

function ProfileLoading() {
  return (
    <main className="grid min-h-screen place-items-center bg-zinc-50">
      <div className="text-center">
        <span className="mx-auto grid size-14 animate-pulse place-items-center rounded-2xl bg-brand-600 text-white">
          <UserRound size={26} />
        </span>
        <p className="mt-4 text-sm font-bold text-zinc-500">
          Carregando seu perfil...
        </p>
      </div>
    </main>
  );
}
