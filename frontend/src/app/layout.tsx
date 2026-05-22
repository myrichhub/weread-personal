import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "微信读书笔记",
  description: "个人微信读书标注与想法展示",
  icons: { icon: "/favicon.svg" },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="zh-CN">
      <body>{children}</body>
    </html>
  );
}
