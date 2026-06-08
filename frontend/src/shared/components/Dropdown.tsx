import {
  useEffect,
  useId,
  useRef,
  useState,
  type ReactNode,
} from "react";

type DropdownControls = {
  close: () => void;
  isOpen: boolean;
};

type DropdownProps = {
  trigger: (controls: DropdownControls) => ReactNode;
  children: ReactNode | ((controls: DropdownControls) => ReactNode);
  className?: string;
  menuClassName?: string;
  ariaLabel: string;
};

export function Dropdown({
  trigger,
  children,
  className = "",
  menuClassName = "",
  ariaLabel,
}: DropdownProps) {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const menuId = useId();

  function close() {
    setIsOpen(false);
  }

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    function handlePointerDown(event: PointerEvent) {
      if (!containerRef.current?.contains(event.target as Node)) {
        close();
      }
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        close();
        triggerRef.current?.focus();
      }
    }

    document.addEventListener("pointerdown", handlePointerDown);
    document.addEventListener("keydown", handleKeyDown);

    return () => {
      document.removeEventListener("pointerdown", handlePointerDown);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen]);

  const controls = { close, isOpen };

  return (
    <div ref={containerRef} className={`relative ${className}`}>
      <button
        ref={triggerRef}
        type="button"
        aria-label={ariaLabel}
        aria-haspopup="menu"
        aria-expanded={isOpen}
        aria-controls={isOpen ? menuId : undefined}
        onClick={() => setIsOpen((current) => !current)}
      >
        {trigger(controls)}
      </button>

      {isOpen && (
        <div
          id={menuId}
          role="menu"
          className={`absolute z-20 ${menuClassName}`}
        >
          {typeof children === "function" ? children(controls) : children}
        </div>
      )}
    </div>
  );
}
