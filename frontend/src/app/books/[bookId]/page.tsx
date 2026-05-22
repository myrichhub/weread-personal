"use client";

import { useEffect, useState } from "react";
import { useRouter, useParams } from "next/navigation";
import Image from "next/image";
import { Download, ChevronLeft } from "lucide-react";
import { api, Book, NoteItem, PageResponse } from "@/lib/api";
import { isLoggedIn, getToken } from "@/lib/auth";
import Navbar from "@/components/Navbar";
import Pagination from "@/components/Pagination";

export default function BookDetailPage() {
  const router = useRouter();
  const { bookId } = useParams<{ bookId: string }>();

  const [book, setBook] = useState<Book | null>(null);
  const [notes, setNotes] = useState<PageResponse<NoteItem> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.replace("/");
      return;
    }
    api
      .getBook(bookId)
      .then(setBook)
      .catch(() => router.replace("/books"));
  }, [bookId, router]);

  useEffect(() => {
    if (!isLoggedIn()) return;
    setLoading(true);
    api
      .getNotes(bookId, page, 20)
      .then(setNotes)
      .finally(() => setLoading(false));
  }, [bookId, page]);

  const handleExport = () => {
    const token = getToken();
    const url = api.exportUrl(bookId);
    // Trigger download via a temporary anchor with auth header injected via fetch
    fetch(url, { headers: { Authorization: `Bearer ${token}` } })
      .then((r) => r.blob())
      .then((blob) => {
        const a = document.createElement("a");
        a.href = URL.createObjectURL(blob);
        a.download = `weread-notes-${bookId}.md`;
        a.click();
        URL.revokeObjectURL(a.href);
      });
  };

  return (
    <div className="min-h-screen">
      <Navbar />
      <main className="max-w-3xl mx-auto px-4 py-8">
        <button
          onClick={() => router.back()}
          className="flex items-center gap-1 text-sm text-gray-500 hover:text-brand mb-6"
        >
          <ChevronLeft size={16} /> 返回书架
        </button>

        {book && (
          <div className="bg-white rounded-xl shadow-sm p-6 mb-8">
            <div className="flex gap-5">
              {book.cover ? (
                <Image
                  src={book.cover}
                  alt={book.title}
                  width={80}
                  height={104}
                  className="rounded object-cover flex-shrink-0"
                  unoptimized
                />
              ) : (
                <div className="w-20 h-[104px] bg-gray-100 rounded flex-shrink-0" />
              )}
              <div className="flex flex-col gap-1.5 min-w-0">
                <h1 className="text-xl font-bold text-gray-900">
                  {book.title}
                </h1>
                <p className="text-gray-500 text-sm">{book.author}</p>
                {book.intro && (
                  <p className="text-gray-600 text-sm mt-2 line-clamp-4">
                    {book.intro}
                  </p>
                )}
              </div>
            </div>
            <div className="mt-5 flex justify-end">
              <button
                onClick={handleExport}
                className="flex items-center gap-2 px-4 py-2 bg-brand text-white text-sm rounded-lg hover:opacity-90"
              >
                <Download size={15} />
                导出 Markdown
              </button>
            </div>
          </div>
        )}

        <h2 className="text-lg font-semibold text-gray-800 mb-4">
          标注与想法
          {notes && (
            <span className="text-sm font-normal text-gray-400 ml-2">
              共 {notes.total} 条
            </span>
          )}
        </h2>

        {loading && (
          <div className="flex justify-center py-16">
            <span className="text-gray-400 animate-pulse">加载中…</span>
          </div>
        )}

        {!loading && notes?.items.length === 0 && (
          <p className="text-center text-gray-400 py-16">暂无标注或想法</p>
        )}

        {!loading && notes && notes.items.length > 0 && (
          <>
            <NoteList items={notes.items} />
            <Pagination
              page={page}
              totalPages={notes.totalPages}
              onChange={setPage}
            />
          </>
        )}
      </main>
    </div>
  );
}

function NoteList({ items }: { items: NoteItem[] }) {
  let lastChapter = "";
  return (
    <div className="space-y-4">
      {items.map((note) => {
        const showChapter =
          note.chapterTitle && note.chapterTitle !== lastChapter;
        if (showChapter) lastChapter = note.chapterTitle;
        return (
          <div key={note.id}>
            {showChapter && (
              <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide mt-6 mb-2">
                {note.chapterTitle}
              </p>
            )}
            <div className="bg-white rounded-xl shadow-sm p-4 space-y-2">
              {note.markedText && (
                <blockquote className="border-l-4 border-brand pl-3 text-gray-700 text-sm leading-relaxed">
                  {note.markedText}
                </blockquote>
              )}
              {note.type === "THOUGHT" && note.content && (
                <p className="text-sm text-gray-600 bg-yellow-50 rounded-lg px-3 py-2">
                  💭 {note.content}
                </p>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}
