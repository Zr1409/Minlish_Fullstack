# Minlish Fullstack

> **Minlish** là hệ thống học ngoại ngữ toàn diện, gồm backend (server) và frontend (website) phát triển độc lập, tích hợp trong một workspace duy nhất.

---

## Cấu trúc thư mục

- `minlish-servers/` — Backend (Spring Boot Java). Xem hướng dẫn chi tiết tại [minlish-servers/README.md](minlish-servers/README.md)
- `minlish-websites/` — Frontend (React + Vite + Tailwind). Xem hướng dẫn chi tiết tại [minlish-websites/README.md](minlish-websites/README.md)

## Hướng dẫn chạy nhanh

### 1. Chuẩn bị môi trường
- Java 17+ (cho backend)
- Node.js 18+ và npm/bun (cho frontend)

### 2. Chạy backend
```bash
cd minlish-servers
./mvnw spring-boot:run
```

### 3. Chạy frontend
```bash
cd minlish-websites
bun install # hoặc npm install
bun dev     # hoặc npm run dev
```

---

> Tham khảo chi tiết:
- Backend: [minlish-servers/README.md](minlish-servers/README.md)
- Frontend: [minlish-websites/README.md](minlish-websites/README.md)

© 2026 Minlish Team

