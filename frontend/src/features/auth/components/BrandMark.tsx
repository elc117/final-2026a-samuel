import { Dumbbell } from "lucide-react";
import { Link } from "react-router-dom";

export function BrandMark({ inverted = false }: { inverted?: boolean }) {
  return (
    <Link
      to="/login"
      className={`inline-flex items-center gap-3 font-black tracking-tight ${
        inverted ? "text-white" : "text-ink-950"
      }`}
      aria-label="Gym Social"
    >
      <span
        className={`grid size-10 place-items-center rounded-xl ${
          inverted ? "bg-white text-brand-600" : "bg-brand-600 text-white"
        }`}
      >
        <Dumbbell size={22} strokeWidth={2.6} />
      </span>
      <span className="text-xl">
        Gym<span className={inverted ? "text-white/75" : "text-brand-600"}>Social</span>
      </span>
    </Link>
  );
}
