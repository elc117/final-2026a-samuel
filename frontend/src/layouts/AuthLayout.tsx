import { Activity, Trophy, Users } from "lucide-react";
import type { ReactNode } from "react";
import { BrandMark } from "../features/auth/components/BrandMark";

const benefits = [
  { icon: Users, text: "Treine junto com quem te motiva" },
  { icon: Trophy, text: "Participe de desafios em grupo" },
  { icon: Activity, text: "Registre cada conquista" },
];

type AuthLayoutProps = {
  children: ReactNode;
  eyebrow: string;
  title: string;
  description: string;
};

export function AuthLayout({
  children,
  eyebrow,
  title,
  description,
}: AuthLayoutProps) {
  return (
    <main className="min-h-screen bg-zinc-50 lg:grid lg:grid-cols-[minmax(380px,0.9fr)_minmax(560px,1.1fr)]">
      <aside className="auth-grid relative hidden min-h-screen overflow-hidden bg-brand-600 px-12 py-10 text-white lg:flex lg:flex-col xl:px-20 xl:py-14">
        <div className="absolute -left-28 bottom-20 size-72 rounded-full border-[42px] border-white/5" />
        <div className="absolute -right-28 top-24 size-96 rounded-full bg-white/5 blur-2xl" />

        <div className="relative z-10">
          <BrandMark inverted />
        </div>

        <div className="relative z-10 my-auto max-w-xl py-14">
          <span className="mb-5 inline-flex rounded-full border border-white/20 bg-white/10 px-4 py-2 text-xs font-extrabold uppercase tracking-[0.18em]">
            Sua evolução, em equipe
          </span>
          <h1 className="max-w-lg text-5xl font-black leading-[1.05] tracking-[-0.04em] xl:text-6xl">
            Consistência fica mais leve quando é compartilhada.
          </h1>
          <p className="mt-6 max-w-lg text-lg leading-8 text-white/78">
            Um espaço para celebrar treinos, acompanhar seus amigos e transformar
            metas em conquistas reais.
          </p>

          <ul className="mt-10 grid gap-4">
            {benefits.map(({ icon: Icon, text }) => (
              <li key={text} className="flex items-center gap-4 text-sm font-bold">
                <span className="grid size-10 place-items-center rounded-xl bg-white/12">
                  <Icon size={19} />
                </span>
                {text}
              </li>
            ))}
          </ul>
        </div>

        <p className="relative z-10 text-xs font-semibold text-white/55">
          © 2026 GymSocial. Feito para quem não treina sozinho.
        </p>
      </aside>

      <section className="flex min-h-screen flex-col bg-white">
        <header className="flex items-center justify-between px-5 py-5 sm:px-8 lg:hidden">
          <BrandMark />
        </header>

        <div className="flex flex-1 items-center justify-center px-5 py-8 sm:px-8 lg:px-12 xl:px-20">
          <div className="w-full max-w-[470px]">
            <p className="mb-3 text-xs font-black uppercase tracking-[0.18em] text-brand-600">
              {eyebrow}
            </p>
            <h2 className="text-3xl font-black tracking-[-0.035em] text-ink-950 sm:text-4xl">
              {title}
            </h2>
            <p className="mt-3 text-[15px] leading-6 text-zinc-500">{description}</p>
            <div className="mt-8">{children}</div>
          </div>
        </div>
      </section>
    </main>
  );
}
