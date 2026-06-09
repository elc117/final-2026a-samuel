import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowLeft, Camera, Check, ImagePlus, Send, X } from "lucide-react";
import { useEffect, useState } from "react";
import { useForm, useWatch } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { AuthenticatedHeader } from "../../auth/components/AuthenticatedHeader";
import { GroupNavigation } from "../../groups/components/GroupNavigation";
import { getApiErrorMessage } from "../../../services/apiClient";
import {
  createCheckInSchema,
  type CreateCheckInFormData,
} from "../schemas/checkInSchemas";
import { createCheckIn } from "../services/checkInService";

const CHECK_IN_IMAGE_OPTIONS = {
  maxWidth: 1600,
  maxHeight: 1600,
  maxInputBytes: 10 * 1024 * 1024,
  maxOutputBytes: 2 * 1024 * 1024,
  outputType: "image/webp",
} as const;

export function CreateCheckInPage() {
  const navigate = useNavigate();
  const [image, setImage] = useState<File>();
  const [previewUrl, setPreviewUrl] = useState("");
  const [imageError, setImageError] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [isCompressing, setIsCompressing] = useState(false);
  const {
    register,
    handleSubmit,
    control,
    formState: { errors, isSubmitting },
  } = useForm<CreateCheckInFormData>({
    resolver: zodResolver(createCheckInSchema),
    mode: "onBlur",
    defaultValues: {
      title: "",
      description: "",
    },
  });
  const title = useWatch({ control, name: "title" }) ?? "";
  const description = useWatch({ control, name: "description" }) ?? "";

  useEffect(() => {
    return () => {
      if (previewUrl) {
        URL.revokeObjectURL(previewUrl);
      }
    };
  }, [previewUrl]);

  async function selectImage(file?: File) {
    setImageError("");

    if (!file) {
      return;
    }

    setIsCompressing(true);

    try {
      const { ImageCompressor } = await import(
        "../../../shared/images/ImageCompressor"
      );
      const compressed = await ImageCompressor.compress(
        file,
        CHECK_IN_IMAGE_OPTIONS,
      );
      setImage(compressed);
      setPreviewUrl(URL.createObjectURL(compressed));
    } catch (error) {
      setImage(undefined);
      setPreviewUrl("");
      setImageError(
        error instanceof Error
          ? error.message
          : "Não foi possível processar a imagem.",
      );
    } finally {
      setIsCompressing(false);
    }
  }

  function removeImage() {
    setImage(undefined);
    setPreviewUrl("");
    setImageError("");
  }

  async function onSubmit(data: CreateCheckInFormData) {
    setSubmitError("");

    if (!image) {
      setImageError("Selecione uma imagem para o check-in.");
      return;
    }

    try {
      await createCheckIn({
        title: data.title,
        description: data.description || undefined,
        image,
      });
      navigate("/grupo", { replace: true });
    } catch (error) {
      setSubmitError(getApiErrorMessage(error));
    }
  }

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

        <div className="mt-6 overflow-hidden rounded-[2rem] border border-zinc-200 bg-white shadow-xl shadow-zinc-200/60">
          <div className="grid lg:grid-cols-[1.05fr_0.95fr]">
            <div className="relative min-h-80 bg-zinc-950">
              {previewUrl ? (
                <>
                  <img
                    src={previewUrl}
                    alt="Prévia do check-in"
                    className="absolute inset-0 size-full object-cover"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-black/20" />
                  <button
                    type="button"
                    onClick={removeImage}
                    className="absolute right-5 top-5 grid size-10 place-items-center rounded-xl bg-white/95 text-zinc-800 shadow-lg transition hover:bg-white"
                    aria-label="Remover imagem"
                  >
                    <X size={19} />
                  </button>
                  <div className="absolute inset-x-0 bottom-0 p-7 text-white">
                    <span className="inline-flex items-center gap-2 rounded-full bg-black/30 px-3 py-1.5 text-xs font-bold backdrop-blur">
                      <Check size={15} />
                      Imagem pronta
                    </span>
                  </div>
                </>
              ) : (
                <label
                  htmlFor="check-in-image"
                  className="auth-grid absolute inset-0 grid cursor-pointer place-items-center p-8 text-center text-white transition hover:bg-white/5"
                >
                  <span className="max-w-sm">
                    <span className="mx-auto grid size-16 place-items-center rounded-2xl bg-white/10">
                      <ImagePlus size={29} />
                    </span>
                    <strong className="mt-5 block text-xl">
                      Adicione uma foto do treino
                    </strong>
                    <span className="mt-2 block text-sm leading-6 text-zinc-400">
                      JPEG, PNG ou WebP. Até 10 MB antes da compressão.
                    </span>
                  </span>
                </label>
              )}

              <input
                id="check-in-image"
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

            <form
              className="space-y-5 p-6 sm:p-8"
              onSubmit={handleSubmit(onSubmit)}
              noValidate
            >
              <div>
                <span className="inline-flex items-center gap-2 rounded-full bg-brand-50 px-3 py-1.5 text-xs font-black uppercase tracking-[0.14em] text-brand-700">
                  <Camera size={15} />
                  Novo check-in
                </span>
                <h1 className="mt-4 text-3xl font-black tracking-[-0.04em] text-ink-950">
                  Compartilhe seu treino
                </h1>
                <p className="mt-2 text-sm leading-6 text-zinc-500">
                  A imagem e o título são obrigatórios. A descrição é opcional.
                </p>
              </div>

              <div>
                <div className="mb-2 flex items-center justify-between gap-3">
                  <label
                    htmlFor="check-in-title"
                    className="text-sm font-extrabold text-zinc-700"
                  >
                    Título
                  </label>
                  <span className="text-xs text-zinc-400">
                    {title.length}/100
                  </span>
                </div>
                <input
                  id="check-in-title"
                  maxLength={100}
                  placeholder="Ex.: Treino de pernas concluído"
                  className="h-12 w-full rounded-xl border border-zinc-300 px-4 text-sm text-zinc-900 outline-none transition placeholder:text-zinc-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-50"
                  aria-invalid={Boolean(errors.title)}
                  {...register("title")}
                />
                {errors.title && (
                  <p className="mt-2 text-sm font-medium text-red-600">
                    {errors.title.message}
                  </p>
                )}
              </div>

              <div>
                <div className="mb-2 flex items-center justify-between gap-3">
                  <label
                    htmlFor="check-in-description"
                    className="text-sm font-extrabold text-zinc-700"
                  >
                    Descrição <span className="font-normal">(opcional)</span>
                  </label>
                  <span className="text-xs text-zinc-400">
                    {description.length}/1000
                  </span>
                </div>
                <textarea
                  id="check-in-description"
                  rows={5}
                  maxLength={1000}
                  placeholder="Conte como foi o treino..."
                  className="w-full resize-none rounded-xl border border-zinc-300 px-4 py-3 text-sm leading-6 text-zinc-900 outline-none transition placeholder:text-zinc-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-50"
                  aria-invalid={Boolean(errors.description)}
                  {...register("description")}
                />
                {errors.description && (
                  <p className="mt-2 text-sm font-medium text-red-600">
                    {errors.description.message}
                  </p>
                )}
              </div>

              {imageError && (
                <p
                  role="alert"
                  className="rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700"
                >
                  {imageError}
                </p>
              )}

              {submitError && (
                <p
                  role="alert"
                  className="rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700"
                >
                  {submitError}
                </p>
              )}

              <button
                type="submit"
                disabled={isSubmitting || isCompressing}
                className="flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-brand-600 px-5 text-sm font-extrabold text-white shadow-lg shadow-red-200 transition hover:bg-brand-700 disabled:cursor-wait disabled:opacity-70"
              >
                <Send size={18} />
                {isCompressing
                  ? "Preparando imagem..."
                  : isSubmitting
                    ? "Publicando..."
                    : "Publicar check-in"}
              </button>
            </form>
          </div>
        </div>
      </section>

      <GroupNavigation />
    </main>
  );
}
