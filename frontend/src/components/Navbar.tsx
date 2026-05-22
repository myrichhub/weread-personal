"use client";

import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { clearToken } from "@/lib/auth";
import { BookOpen, LogOut, RefreshCw } from "lucide-react";
import { useState } from "react";

export default function Navbar() {
  const router = useRouter();
  const [syncing, setSyncing] = useState(false);

  const handleSync = async () => {
    setSyncing(true);
    try {
      await api.triggerSync();
      // poll until done
      const poll = setInterval(async () => {
        const s = await api.getSyncStatus();
        if (s.state === "DONE" || s.state === "FAILED") {
          clearInterval(poll);
          setSyncing(false);
          if (s.state === "DONE") window.location.reload();
        }
      }, 2000);
    } catch {
      setSyncing(false);
    }
  };

  const handleLogout = async () => {
    await api.logout().catch(() => {});
    clearToken();
    router.replace("/");
  };

  return (
    <header className="bg-white border-b border-gray-100 sticky top-0 z-10">
      <div className="max-w-4xl mx-auto px-4 h-14 flex items-center justify-between">
        <div className="flex items-center gap-2 text-brand font-bold text-lg">
          <BookOpen size={20} />
          微信读书笔记
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={handleSync}
            disabled={syncing}
            className="flex items-center gap-1.5 text-sm text-gray-600 hover:text-brand disabled:opacity-50"
          >
            <RefreshCw size={15} className={syncing ? "animate-spin" : ""} />
            {syncing ? "同步中…" : "同步数据"}
          </button>
          <button
            onClick={handleLogout}
            className="flex items-center gap-1.5 text-sm text-gray-600 hover:text-red-500"
          >
            <LogOut size={15} />
            退出
          </button>
        </div>
      </div>
    </header>
  );
}
