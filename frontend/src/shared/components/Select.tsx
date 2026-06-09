import { Check, ChevronDown } from "lucide-react";
import {
  forwardRef,
  useEffect,
  useId,
  useRef,
  useState,
  type KeyboardEvent,
} from "react";

export type SelectOption = {
  value: string;
  label: string;
  disabled?: boolean;
};

type SelectProps = {
  options: SelectOption[];
  value: string;
  onChange: (value: string) => void;
  onBlur?: () => void;
  name?: string;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
  ariaLabel?: string;
};

export const Select = forwardRef<HTMLButtonElement, SelectProps>(
  function Select(
    {
      options,
      value,
      onChange,
      onBlur,
      name,
      placeholder = "Selecione uma opção",
      disabled = false,
      className = "",
      ariaLabel,
    },
    forwardedRef,
  ) {
    const [isOpen, setIsOpen] = useState(false);
    const [highlightedIndex, setHighlightedIndex] = useState(-1);
    const containerRef = useRef<HTMLDivElement>(null);
    const buttonRef = useRef<HTMLButtonElement | null>(null);
    const listboxId = useId();
    const selectedIndex = options.findIndex((option) => option.value === value);
    const selectedOption = options[selectedIndex];

    useEffect(() => {
      if (!isOpen) return;

      function handlePointerDown(event: PointerEvent) {
        if (!containerRef.current?.contains(event.target as Node)) {
          setIsOpen(false);
          onBlur?.();
        }
      }

      document.addEventListener("pointerdown", handlePointerDown);
      return () => document.removeEventListener("pointerdown", handlePointerDown);
    }, [isOpen, onBlur]);

    function firstEnabledIndex() {
      return options.findIndex((option) => !option.disabled);
    }

    function open() {
      if (disabled) return;
      setHighlightedIndex(selectedIndex >= 0 ? selectedIndex : firstEnabledIndex());
      setIsOpen(true);
    }

    function moveHighlight(direction: 1 | -1) {
      if (options.length === 0) return;

      let nextIndex = highlightedIndex;
      for (let attempt = 0; attempt < options.length; attempt += 1) {
        nextIndex = (nextIndex + direction + options.length) % options.length;
        if (!options[nextIndex].disabled) {
          setHighlightedIndex(nextIndex);
          return;
        }
      }
    }

    function selectOption(index: number) {
      const option = options[index];
      if (!option || option.disabled) return;
      onChange(option.value);
      setHighlightedIndex(index);
      setIsOpen(false);
      buttonRef.current?.focus();
    }

    function handleKeyDown(event: KeyboardEvent<HTMLButtonElement>) {
      switch (event.key) {
        case "ArrowDown":
          event.preventDefault();
          if (!isOpen) open();
          else moveHighlight(1);
          break;
        case "ArrowUp":
          event.preventDefault();
          if (!isOpen) open();
          else moveHighlight(-1);
          break;
        case "Enter":
        case " ":
          event.preventDefault();
          if (!isOpen) open();
          else if (highlightedIndex >= 0) selectOption(highlightedIndex);
          break;
        case "Escape":
          if (isOpen) {
            event.preventDefault();
            setIsOpen(false);
          }
          break;
      }
    }

    return (
      <div ref={containerRef} className={`relative ${className}`}>
        <button
          ref={(element) => {
            buttonRef.current = element;
            if (typeof forwardedRef === "function") forwardedRef(element);
            else if (forwardedRef) forwardedRef.current = element;
          }}
          type="button"
          name={name}
          role="combobox"
          aria-label={ariaLabel}
          aria-controls={listboxId}
          aria-expanded={isOpen}
          aria-haspopup="listbox"
          disabled={disabled}
          onBlur={(event) => {
            if (!containerRef.current?.contains(event.relatedTarget)) onBlur?.();
          }}
          onClick={() => (isOpen ? setIsOpen(false) : open())}
          onKeyDown={handleKeyDown}
          className="flex h-12 w-full items-center justify-between rounded-xl border border-zinc-300 bg-white pl-4 pr-4 text-left text-sm outline-none transition focus:border-brand-500 focus:ring-4 focus:ring-brand-50 disabled:cursor-not-allowed disabled:bg-zinc-100 disabled:text-zinc-400"
        >
          <span className={selectedOption ? "text-ink-950" : "text-zinc-400"}>
            {selectedOption?.label ?? placeholder}
          </span>
          <ChevronDown
            size={17}
            aria-hidden="true"
            className={`ml-4 shrink-0 text-zinc-500 transition-transform ${
              isOpen ? "rotate-180" : ""
            }`}
          />
        </button>

        {isOpen && (
          <div
            id={listboxId}
            role="listbox"
            aria-label={ariaLabel}
            className="absolute left-0 right-0 top-[calc(100%+0.5rem)] z-30 overflow-hidden rounded-xl border border-zinc-200 bg-white p-1.5 shadow-xl shadow-zinc-200/70"
          >
            {options.map((option, index) => {
              const isSelected = option.value === value;
              const isHighlighted = index === highlightedIndex;

              return (
                <button
                  key={option.value}
                  type="button"
                  role="option"
                  aria-selected={isSelected}
                  disabled={option.disabled}
                  onPointerMove={() => setHighlightedIndex(index)}
                  onClick={() => selectOption(index)}
                  className={`flex w-full items-center justify-between rounded-lg px-3 py-2.5 text-left text-sm transition ${
                    isHighlighted
                      ? "bg-brand-50 text-brand-700"
                      : "text-zinc-700 hover:bg-zinc-50"
                  } disabled:cursor-not-allowed disabled:opacity-40`}
                >
                  <span>{option.label}</span>
                  {isSelected && <Check size={16} aria-hidden="true" />}
                </button>
              );
            })}
          </div>
        )}
      </div>
    );
  },
);
