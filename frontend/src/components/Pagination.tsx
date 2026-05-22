"use client";

import { ChevronLeft, ChevronRight } from "lucide-react";

interface Props {
  page: number;
  totalPages: number;
  onChange: (p: number) => void;
}

function buildPageNums(current: number, total: number): (number | "…")[] {
  if (total <= 7) return Array.from({ length: total }, (_, i) => i);
  const pages: (number | "…")[] = [0];
  if (current > 2) pages.push("…");
  for (
    let i = Math.max(1, current - 1);
    i <= Math.min(total - 2, current + 1);
    i++
  )
    pages.push(i);
  if (current < total - 3) pages.push("…");
  pages.push(total - 1);
  return pages;
}

export default function Pagination({ page, totalPages, onChange }: Props) {
  if (totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-center gap-2 mt-6">
      <button
        onClick={() => onChange(page - 1)}
        disabled={page === 0}
        className="p-1.5 rounded hover:bg-gray-100 disabled:opacity-30"
      >
        <ChevronLeft size={18} />
      </button>

      {buildPageNums(page, totalPages).map((p, idx) =>
        p === "…" ? (
          <span
            key={`ellipsis-${idx}`}
            className="w-8 text-center text-gray-400 text-sm"
          >
            …
          </span>
        ) : (
          <button
            key={p}
            onClick={() => onChange(p as number)}
            className={`w-8 h-8 rounded text-sm font-medium ${
              p === page
                ? "bg-brand text-white"
                : "text-gray-600 hover:bg-gray-100"
            }`}
          >
            {(p as number) + 1}
          </button>
        ),
      )}

      <button
        onClick={() => onChange(page + 1)}
        disabled={page >= totalPages - 1}
        className="p-1.5 rounded hover:bg-gray-100 disabled:opacity-30"
      >
        <ChevronRight size={18} />
      </button>
    </div>
  );
}
