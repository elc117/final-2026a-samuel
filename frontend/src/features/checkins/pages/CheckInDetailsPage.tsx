import { ArrowLeft, Clock, Send, UserRound } from "lucide-react";
import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { AuthenticatedHeader } from "../../auth/components/AuthenticatedHeader";
import { ApiError, getApiErrorMessage } from "../../../services/apiClient";
import {
  createCheckInComment,
  getCheckIn,
  getCheckInComments,
  type CheckIn,
  type CheckInComment,
} from "../services/checkInService";

export function CheckInDetailsPage() {
  const navigate = useNavigate();
  const { checkInId = "" } = useParams();
  const [checkIn, setCheckIn] = useState<CheckIn>();
  const [comments, setComments] = useState<CheckInComment[]>([]);
  const [comment, setComment] = useState("");
  const [loadError, setLoadError] = useState("");
  const [commentError, setCommentError] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    let active = true;

    getCheckIn(checkInId)
      .then((response) => active && setCheckIn(response))
      .catch((error: unknown) => {
        if (error instanceof ApiError && error.status === 401) {
          navigate("/login", { replace: true });
          return;
        }
        if (active) setLoadError(getApiErrorMessage(error));
      })
      .finally(() => active && setIsLoading(false));

    getCheckInComments(checkInId)
      .then((response) => active && setComments(response))
      .catch(() => undefined);

    return () => {
      active = false;
    };
  }, [checkInId, navigate]);

  async function submitComment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const content = comment.trim();

    if (!content) {
      setCommentError("Escreva um comentário.");
      return;
    }

    setCommentError("");
    setIsSubmitting(true);

    try {
      const created = await createCheckInComment(checkInId, content);
      setComments((current) => [...current, created]);
      setComment("");
    } catch (error) {
      setCommentError(getApiErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="min-h-screen bg-zinc-50">
      <AuthenticatedHeader page="group" />
      <section className="mx-auto max-w-5xl px-5 py-10 sm:px-8 sm:py-16">
        <Link
          to="/grupo"
          className="inline-flex items-center gap-2 text-sm font-extrabold text-zinc-500 transition hover:text-brand-700"
        >
          <ArrowLeft size={18} />
          Voltar aos check-ins
        </Link>

        {isLoading ? (
          <p className="mt-8 text-sm font-bold text-zinc-500">
            Carregando check-in...
          </p>
        ) : loadError || !checkIn ? (
          <p className="mt-6 rounded-2xl bg-red-50 px-5 py-4 text-sm font-semibold text-red-700">
            {loadError || "Check-in não encontrado."}
          </p>
        ) : (
          <div className="mt-6">
            <article className="flex flex-col overflow-hidden rounded-[2rem] border border-zinc-200 bg-white shadow-xl shadow-zinc-200/50">
              <div className="h-[24rem] overflow-hidden border-b border-zinc-200 bg-zinc-100 sm:h-[30rem]">
                <img
                  src={checkIn.imageUrl}
                  alt={`Check-in: ${checkIn.title}`}
                  className="size-full bg-white object-cover"
                />
              </div>

              <div className="flex flex-col p-6 sm:p-8">
                <div className="flex items-center gap-3">
                  <Avatar
                    name={checkIn.authorName}
                    imageUrl={checkIn.authorImageUrl}
                    large
                  />
                  <div>
                    <p className="text-sm font-extrabold text-ink-950">
                      {checkIn.authorName ?? "Membro do grupo"}
                    </p>
                    <p className="mt-1 flex items-center gap-1.5 text-xs text-zinc-500">
                      <Clock size={13} />
                      {formatDate(checkIn.createdAt)}
                    </p>
                  </div>
                </div>

                <p className="mt-7 text-xs font-black uppercase tracking-[0.16em] text-brand-600">
                  Check-in
                </p>
                <h1 className="mt-2 text-3xl font-black tracking-[-0.04em] text-ink-950">
                  {checkIn.title}
                </h1>
                {checkIn.description && (
                  <p className="mt-3 whitespace-pre-wrap text-sm leading-7 text-zinc-600">
                    {checkIn.description}
                  </p>
                )}
              </div>

              <section className="border-t border-zinc-100 px-6 py-5 sm:px-8">
              <div className="divide-y divide-zinc-100">
                {comments.length === 0 ? (
                  <p className="py-6 text-center text-sm text-zinc-400">
                    Nenhum comentário ainda.
                  </p>
                ) : (
                  comments.map((current) => (
                    <article key={current.id} className="flex gap-3 py-4 first:pt-0">
                      <Avatar
                        name={current.authorName}
                        imageUrl={current.authorImageUrl}
                      />
                      <div className="min-w-0 flex-1">
                        <div className="flex justify-between gap-3">
                          <p className="text-sm font-extrabold text-ink-950">
                            {current.authorName}
                          </p>
                          <time className="shrink-0 text-xs text-zinc-400">
                            {formatDate(current.createdAt)}
                          </time>
                        </div>
                        <p className="mt-1 whitespace-pre-wrap break-words [overflow-wrap:anywhere] text-sm leading-6 text-zinc-600">
                          {current.content}
                        </p>
                      </div>
                    </article>
                  ))
                )}
              </div>

              <form
                className="mt-4 border-t border-zinc-100 pt-4"
                onSubmit={(event) => void submitComment(event)}
              >
                <div className="flex items-center border-b border-zinc-300 transition focus-within:border-brand-500">
                  <input
                    value={comment}
                    maxLength={1000}
                    placeholder="Escreva um comentário..."
                    onChange={(event) => {
                      setComment(event.target.value);
                      setCommentError("");
                    }}
                    className="h-11 min-w-0 flex-1 border-0 bg-transparent px-0 text-sm outline-none placeholder:text-zinc-400"
                  />
                  <button
                    type="submit"
                    disabled={isSubmitting || !comment.trim()}
                    className="grid size-10 shrink-0 place-items-center text-brand-600 transition hover:text-brand-700 disabled:cursor-not-allowed disabled:text-zinc-300"
                    aria-label="Publicar comentário"
                  >
                    <Send size={17} />
                  </button>
                </div>
                {commentError && (
                  <p className="mt-2 text-sm font-medium text-red-600">
                    {commentError}
                  </p>
                )}
              </form>
              </section>
            </article>
          </div>
        )}
      </section>
    </main>
  );
}

function Avatar({
  name,
  imageUrl,
  large = false,
}: {
  name: string | null;
  imageUrl: string | null;
  large?: boolean;
}) {
  return (
    <span
      className={`grid shrink-0 overflow-hidden bg-brand-50 font-black text-brand-700 ${
        large ? "size-11 rounded-xl text-sm" : "size-9 rounded-lg text-xs"
      }`}
    >
      {imageUrl ? (
        <img src={imageUrl} alt="" className="size-full object-cover" />
      ) : name ? (
        <span className="m-auto">{name.charAt(0).toUpperCase()}</span>
      ) : (
        <UserRound className="m-auto" size={17} />
      )}
    </span>
  );
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(value));
}
