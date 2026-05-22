import Link from "next/link";

export default function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center flex-col gap-4">
      <h1 className="text-4xl font-bold text-gray-300">404</h1>
      <Link href="/books" className="text-brand text-sm hover:underline">
        返回书架
      </Link>
    </div>
  );
}
