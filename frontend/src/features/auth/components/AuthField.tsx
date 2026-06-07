import { Eye, EyeOff, type LucideIcon } from "lucide-react";
import {
  forwardRef,
  useState,
  type InputHTMLAttributes,
} from "react";

type AuthFieldProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string;
  icon: LucideIcon;
  error?: string;
};

export const AuthField = forwardRef<HTMLInputElement, AuthFieldProps>(
  function AuthField(
    {
      label,
      icon: Icon,
      error,
      type = "text",
      id,
      ...props
    },
    ref,
  ) {
  const [showPassword, setShowPassword] = useState(false);
  const isPassword = type === "password";
  const inputType = isPassword && showPassword ? "text" : type;

  return (
    <div>
      <label htmlFor={id} className="mb-2 block text-sm font-bold text-zinc-800">
        {label}
      </label>
      <div className="relative">
        <Icon
          aria-hidden="true"
          size={19}
          className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-zinc-400"
        />
        <input
          {...props}
          ref={ref}
          id={id}
          type={inputType}
          aria-invalid={Boolean(error)}
          aria-describedby={error ? `${id}-error` : undefined}
          className={`h-12 w-full rounded-xl border bg-white pl-11 text-[15px] text-zinc-900 outline-none transition placeholder:text-zinc-400 focus:ring-4 ${
            isPassword ? "pr-12" : "pr-4"
          } ${
            error
              ? "border-red-400 focus:border-red-500 focus:ring-red-100"
              : "border-zinc-200 hover:border-zinc-300 focus:border-brand-500 focus:ring-brand-100"
          }`}
        />
        {isPassword && (
          <button
            type="button"
            onClick={() => setShowPassword((current) => !current)}
            className="absolute right-3 top-1/2 grid size-9 -translate-y-1/2 place-items-center rounded-lg text-zinc-400 transition hover:bg-zinc-100 hover:text-zinc-700 focus:outline-none focus:ring-2 focus:ring-brand-500"
            aria-label={showPassword ? "Ocultar senha" : "Mostrar senha"}
          >
            {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
          </button>
        )}
      </div>
      {error && (
        <p id={`${id}-error`} className="mt-1.5 text-sm font-medium text-red-600">
          {error}
        </p>
      )}
    </div>
  );
  },
);
