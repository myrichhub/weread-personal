"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
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

  useEffect(() => {
    if (!isLoggedIn()) {
      router.replace("/");
      return;
    }
    setLoading(true);
    api
      .listBooks(page, 10)
      .then(setData)
      .catch(() => router.replace("/"))
      .finally(() => setLoading(false));
  }, [page, router]);

  return (
    <div className="min-h-screen">
      <Navbar />
      <main className="max-w-4xl mx-auto px-4 py-8">
        <h2 className="text-xl font-bold text-gray-800 mb-1">我的书架</h2>
        {data && (
          <p className="text-sm text-gray-400 mb-6">共 {data.total} 本书</p>
        )}

        {loading && (
          <div className="flex justify-center py-20">
            <span className="text-gray-400 animate-pulse">加载中…</span>
          </div>
        )}

        {!loading && data?.items.length === 0 && (
          <div className="text-center py-20 text-gray-400">
            暂无书籍数据，请点击右上角「同步数据」
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
                  <div className="flex flex-col justify-center gap-1 min-w-0">
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
