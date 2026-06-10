import { useEffect, useRef } from "react";

export function useInfiniteScroll({
  enabled,
  onLoad,
  rootMargin = "240px 0px",
}: {
  enabled: boolean;
  onLoad: () => void;
  rootMargin?: string;
}) {
  const elementRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const element = elementRef.current;
    if (!element || !enabled) {
      return;
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          onLoad();
        }
      },
      { rootMargin },
    );
    observer.observe(element);
    return () => observer.disconnect();
  }, [enabled, onLoad, rootMargin]);

  return elementRef;
}
