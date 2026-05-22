"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { isLoggedIn, saveToken } from "@/lib/auth";

export default function LoginPage() {
  const router = useRouter();
  const [cookie, setCookie] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (isLoggedIn()) router.replace("/books");
  }, [router]);

  const handleLogin = async () => {
    if (!cookie.trim()) {
      setError("请先粘贴 Cookie");
      return;
    }
    setLoading(true);
    setError("");
    try {
      const res = await fetch("/api/auth/cookie", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ cookie: cookie.trim() }),
      });
      const data = await res.json();
      if (!res.ok) {
        setError(data.error || "登录失败");
        return;
      }
      saveToken(data.token);
      router.replace("/books");
    } catch {
      setError("网络错误，请检查后端是否已启动");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-50 to-white px-4">
      <div className="bg-white rounded-2xl shadow-lg p-8 w-full max-w-lg flex flex-col gap-5">
        <h1 className="text-2xl font-bold text-gray-800 text-center">
          微信读书笔记
        </h1>

        <div className="bg-blue-50 rounded-xl p-4 text-sm text-gray-700 space-y-2.5">
          <p className="font-semibold text-gray-800">如何获取完整 Cookie：</p>
          <ol className="list-decimal list-inside space-y-2">
            <li>
              <a
                href="https://weread.qq.com"
                target="_blank"
                rel="noopener noreferrer"
                className="text-blue-600 underline"
              >
                打开微信读书网页版
              </a>
              ，用微信扫码登录
            </li>
            <li>
              按{" "}
              <kbd className="bg-gray-200 px-1.5 py-0.5 rounded text-xs">
                F12
              </kbd>{" "}
              打开开发者工具，切换到 <strong>Network</strong> 标签
            </li>
            <li>
              刷新页面，点击任意一个请求（如{" "}
              <code className="bg-gray-200 px-1 rounded text-xs">
                syncshelf
              </code>{" "}
              或 <code className="bg-gray-200 px-1 rounded text-xs">shelf</code>
              ）
            </li>
            <li>
              在右侧 <strong>Request Headers</strong> 里找到{" "}
              <strong>cookie</strong> 行， 点击右键 → <strong>复制值</strong>
              （整行内容，包含 wr_skey、wr_vid 等所有字段）
            </li>
          </ol>
          <p className="text-xs text-gray-500 mt-1">
            💡 也可以在 Application → Cookies → weread.qq.com 中，把所有 cookie
            拼成{" "}
            <code className="bg-gray-200 px-1 rounded">
              name=value; name2=value2
            </code>{" "}
            格式
          </p>
        </div>

        <div className="flex flex-col gap-2">
          <label className="text-sm font-medium text-gray-700">
            粘贴完整 Cookie
          </label>
          <textarea
            value={cookie}
            onChange={(e) => setCookie(e.target.value)}
            placeholder="wr_skey=xxx; wr_vid=xxx; wr_name=xxx; ..."
            rows={3}
            className="border border-gray-200 rounded-lg px-3 py-2 text-xs font-mono focus:outline-none focus:ring-2 focus:ring-green-300 resize-none"
          />
        </div>

        {error && <p className="text-red-500 text-sm">{error}</p>}

        <button
          onClick={handleLogin}
          disabled={loading}
          className="w-full py-2.5 bg-brand text-white rounded-lg font-semibold text-sm hover:opacity-90 disabled:opacity-50"
        >
          {loading ? "验证中…" : "登录"}
        </button>

        <p className="text-xs text-gray-400 text-center">
          Cookie 仅存在服务端内存中，不会写入数据库
        </p>
      </div>
    </main>
  );
}
