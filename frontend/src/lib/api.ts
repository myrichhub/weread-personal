import axios from "axios";

const http = axios.create({ baseURL: "/api" });

http.interceptors.request.use((config) => {
  const token =
    typeof window !== "undefined" ? localStorage.getItem("token") : null;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err.response?.status === 401 && typeof window !== "undefined") {
      localStorage.removeItem("token");
      window.location.href = "/";
    }
    return Promise.reject(err);
  },
);

export interface QrCodeResponse {
  uuid: string;
  qrUrl: string;
}

export interface PollResponse {
  status: "WAITING" | "SCANNED" | "SUCCESS" | "EXPIRED";
  token: string | null;
}

export interface Book {
  bookId: string;
  title: string;
  author: string;
  cover: string;
  intro: string;
  lastReadTime: number;
  readStatus: number;
  annotationCount: number;
  thoughtCount: number;
}

export interface NoteItem {
  type: "ANNOTATION" | "THOUGHT";
  id: string;
  bookId: string;
  chapterTitle: string;
  markedText: string;
  content: string | null;
  createdTime: number;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  total: number;
  totalPages: number;
}

export const api = {
  getQrCode: () => http.get<QrCodeResponse>("/auth/qrcode").then((r) => r.data),
  pollLogin: (uuid: string) =>
    http.get<PollResponse>(`/auth/poll?uuid=${uuid}`).then((r) => r.data),
  logout: () => http.post("/auth/logout"),

  triggerSync: () =>
    http.post<{ message: string }>("/sync").then((r) => r.data),
  getSyncStatus: () =>
    http
      .get<{ state: string; message: string }>("/sync/status")
      .then((r) => r.data),

  listBooks: (page = 0, size = 10, q?: string, hasNotes = false) => {
    const params = new URLSearchParams({
      page: String(page),
      size: String(size),
    });
    if (q) params.set("q", q);
    if (hasNotes) params.set("hasNotes", "true");
    return http.get<PageResponse<Book>>(`/books?${params}`).then((r) => r.data);
  },
  getBook: (bookId: string) =>
    http.get<Book>(`/books/${bookId}`).then((r) => r.data),
  getNotes: (bookId: string, page = 0, size = 20) =>
    http
      .get<
        PageResponse<NoteItem>
      >(`/books/${bookId}/notes?page=${page}&size=${size}`)
      .then((r) => r.data),
  exportUrl: (bookId: string) => `/api/books/${bookId}/export`,
};
