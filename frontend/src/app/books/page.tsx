"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { Search, StickyNote } from "lucide-react";
import { api, Book, PageResponse } from "@/lib/api";
import { isLoggedIn } from "@/lib/auth";
import Navbar from "@/components/Navbar";
import Pagination from "@/components/Pagination";

function formatDate(ts: number) {
  if (!ts) return "";
  return new Date(ts * 1000).toLocaleDateString("zh-CN", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

export default function BooksPage() {
  const router = useRouter();
  const [data, setData] = useState<PageResponse<Book> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [q, setQ] = useState("");
  const [hasNotes, setHasNotes] = useState(false);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.replace("/");
      return;
    }
    setLoading(true);
    api
      .listBooks(page, 10, q || undefined, hasNotes)
      .then(setData)
      .catch(() => router.replace("/"))
      .finally(() => setLoading(false));
  }, [page, q, hasNotes, router]);

  const handleSearch = (value: string) => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      setPage(0);
      setQ(value);
    }, 300);
  };

  const toggleHasNotes = () => {
    setPage(0);
    setHasNotes((v) => !v);
  };

  return (
    <div className="min-h-screen">
      <Navbar />
      <main className="max-w-4xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-gray-800">我的书架</h2>
          {data && (
            <p className="text-sm text-gray-400">共 {data.total} 本书</p>
          )}
        </div>

        {/* Search + filter bar */}
        <div className="flex gap-2 mb-6">
          <div className="relative flex-1">
            <Search
              size={15}
              className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
            />
            <input
              type="text"
              placeholder="搜索书名或作者…"
              onChange={(e) => handleSearch(e.target.value)}
              className="w-full pl-9 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-300"
            />
          </div>
          <button
            onClick={toggleHasNotes}
            className={`flex items-center gap-1.5 px-3 py-2 text-sm rounded-lg border transition-colors whitespace-nowrap ${
              hasNotes
                ? "bg-brand text-white border-brand"
                : "bg-white text-gray-600 border-gray-200 hover:border-brand hover:text-brand"
            }`}
          >
            <StickyNote size={14} />
            仅看有笔记
          </button>
        </div>

        {loading && (
          <div className="flex justify-center py-20">
            <span className="text-gray-400 animate-pulse">加载中…</span>
          </div>
        )}

        {!loading && data?.items.length === 0 && (
          <div className="text-center py-20 text-gray-400">
            {q || hasNotes
              ? "没有符合条件的书籍"
              : "暂无书籍数据，请点击右上角「同步数据」"}
          </div>
        )}

        {!loading && data && data.items.length > 0 && (
          <>
            <div className="grid gap-4">
              {data.items.map((book) => (
                <button
                  key={book.bookId}
                  onClick={() => router.push(`/books/${book.bookId}`)}
                  className="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow p-4 flex gap-4 text-left w-full"
                >
                  {book.cover ? (
                    <Image
                      src={book.cover}
                      alt={book.title}
                      width={56}
                      height={72}
                      className="rounded object-cover flex-shrink-0"
                      unoptimized
                    />
                  ) : (
                    <div className="w-14 h-[72px] bg-gray-100 rounded flex-shrink-0" />
                  )}
                  <div className="flex flex-col justify-center gap-1 min-w-0 flex-1">
                    <p className="font-semibold text-gray-900 truncate">
                      {book.title}
                    </p>
                    <p className="text-sm text-gray-500">{book.author}</p>
                    {book.lastReadTime > 0 && (
                      <p className="text-xs text-gray-400">
                        最后阅读 {formatDate(book.lastReadTime)}
                      </p>
                    )}
                  </div>
                  {(book.annotationCount > 0 || book.thoughtCount > 0) && (
                    <div className="flex flex-col items-end justify-center gap-1 flex-shrink-0 text-xs text-gray-400">
                      {book.annotationCount > 0 && (
                        <span className="bg-yellow-50 text-yellow-700 px-2 py-0.5 rounded-full">
                          划线 {book.annotationCount}
                        </span>
                      )}
                      {book.thoughtCount > 0 && (
                        <span className="bg-green-50 text-green-700 px-2 py-0.5 rounded-full">
                          想法 {book.thoughtCount}
                        </span>
                      )}
                    </div>
                  )}
                </button>
              ))}
            </div>

            <Pagination
              page={page}
              totalPages={data.totalPages}
              onChange={setPage}
            />
          </>
        )}
      </main>
    </div>
  );
}
