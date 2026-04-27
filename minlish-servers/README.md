# MinLish Servers (Backend)

Backend API cho ứng dụng học từ vựng MinLish, xây bằng Spring Boot.

## 1) Tổng quan

- Project: `com.minlish:minlish:0.0.1-SNAPSHOT`
- Java: `21`
- Spring Boot: `3.3.3`
- Build tool: Maven
- Database: MySQL
- Auth: JWT Bearer Token (stateless)
- Scheduling: Bật `@EnableScheduling` cho nhắc học định kỳ

Các nhóm chức năng chính:

- Xác thực: đăng ký/đăng nhập
- Quản lý hồ sơ user
- Quản lý bộ từ và từ vựng
- Import/Export danh sách từ vựng (CSV/XLSX)
- Ôn tập theo thuật toán spaced repetition (SM-2)
- Thống kê học tập
- Notification in-app + email reminder + cấu hình giờ nhắc

## 2) Cấu trúc source chính

```text
src/main/java/com/minlish
  config/       # JWT filter, security, CORS
  controller/   # REST API
  dto/          # Request/response DTO
  entity/       # JPA entities
  exception/    # Global exception handler
  repository/   # Spring Data JPA repositories
  service/      # Business logic
  util/         # Helpers (SecurityUtils, SM2Util...)

src/main/resources
  application.properties
  init.sql
```

## 3) Yêu cầu môi trường

- JDK 21+
- Maven 3.9+
- MySQL 8+

## 4) Cấu hình ứng dụng

File cấu hình chính: `src/main/resources/application.properties`

Các nhóm cấu hình cần kiểm tra:

- App/server: `server.port`, `spring.application.name`
- DB: `spring.datasource.*`
- JPA: `spring.jpa.*`
- JWT: `jwt.secret`, `jwt.expiration`, `jwt.refresh.expiration`
- CORS: trong `SecurityConfig` và `WebConfig`
- SMTP: `spring.mail.*`
- OAuth2: Google/GitHub registration

Lưu ý bảo mật:

- File hiện tại đang chứa thông tin nhạy cảm (DB password, mail password, OAuth secret, cloud key).
- Khuyến nghị chuyển sang biến môi trường ngay khi deploy.

Ví dụ chuyển sang env var:

```properties
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
spring.mail.password=${MAIL_PASSWORD}
```

## 5) Cảnh báo dữ liệu local (rất quan trọng)

Hiện tại app đang để:

```properties
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:init.sql
```

Và trong `init.sql` có:

- Drop/Truncate nhiều bảng
- Nạp sample data

Nghĩa là mỗi lần start app có thể reset dữ liệu local.

Nếu muốn giữ dữ liệu local khi dev:

```properties
spring.sql.init.mode=never
```

## 6) Chạy dự án

Chạy dev:

```bash
cd minlish-servers
mvn clean spring-boot:run
```

Mặc định API chạy tại:

- `http://localhost:8080`

Build jar:

```bash
mvn clean package
java -jar target/minlish-0.0.1-SNAPSHOT.jar
```

Test:

```bash
mvn test
```

## 7) Security và Authentication

- Public endpoint: `/api/auth/**` (và `/api/public/**` nếu có)
- Endpoint còn lại yêu cầu JWT
- Header bắt buộc cho API private:

```http
Authorization: Bearer <accessToken>
```

Response đăng nhập/đăng ký trả về `JwtResponse` có:

- `accessToken`
- `tokenType` (`Bearer`)
- `userId`
- `email`
- `fullName`
- `token` (alias của `accessToken`)

## 8) CORS

Hiện backend cho phép local origins:

- `http://localhost:3000`
- `http://localhost:5173`
- `http://localhost:8080`
- `http://localhost:8081`

Lưu ý: CORS được cấu hình ở cả `SecurityConfig` và `WebConfig`. Khi đổi domain frontend, nên cập nhật đồng bộ để tránh lỗi preflight.

## 9) API Reference (theo code hiện tại)

Base URL: `http://localhost:8080`

### 9.1 Auth - `/api/auth`

1. `POST /api/auth/register`
2. `POST /api/auth/login`

Register request:

```json
{
  "email": "user@example.com",
  "password": "123456",
  "fullName": "Test User",
  "learningGoal": "IELTS",
  "level": "B1"
}
```

Ràng buộc:

- `email`: bắt buộc, đúng định dạng email
- `password`: bắt buộc, 6-40 ký tự

Login request:

```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

### 9.2 Users - `/api/users` (JWT)

1. `GET /api/users/profile`
2. `PUT /api/users/profile`

Update profile request:

```json
{
  "fullName": "Nguyen Van A",
  "learningGoal": "TOEIC",
  "level": "B2"
}
```

### 9.3 Vocabulary Sets - `/api/sets` (JWT)

1. `POST /api/sets`
2. `GET /api/sets`
3. `GET /api/sets/{setId}`
4. `PUT /api/sets/{setId}`
5. `DELETE /api/sets/{setId}`

VocabularySet request body:

```json
{
  "name": "IELTS Academic",
  "description": "Từ vựng học thuật",
  "tags": "IELTS,Academic"
}
```

Ràng buộc:

- `name`: bắt buộc

### 9.4 Vocabularies - `/api/vocabularies` (JWT)

1. `POST /api/vocabularies/set/{setId}`
2. `GET /api/vocabularies/set/{setId}`
3. `PUT /api/vocabularies/{vocabId}`
4. `DELETE /api/vocabularies/{vocabId}`
5. `POST /api/vocabularies/import/{setId}` (multipart)
6. `GET /api/vocabularies/export/{setId}` (download CSV)

Vocabulary request body:

```json
{
  "word": "ubiquitous",
  "pronunciation": "/juːˈbɪk.wɪ.təs/",
  "meaning": "co mat o khap noi",
  "description": "present everywhere",
  "exampleSentence": "Mobile phones are ubiquitous.",
  "fixedPhrase": "ubiquitous technology",
  "relatedWords": "omnipresent, pervasive",
  "notes": "IELTS Band 7+"
}
```

Ràng buộc:

- `word`: bắt buộc
- `meaning`: bắt buộc

Import file hỗ trợ:

- `.csv`, `.xls`, `.xlsx`
- Cột bắt buộc theo thứ tự: `word` (cột 0), `meaning` (cột 2)
- Header được kỳ vọng: `word,pronunciation,meaning,description,example_sentence,fixed_phrase,related_words,notes`

### 9.5 Study - `/api/study` (JWT)

1. `POST /api/study/rate`
2. `GET /api/study/today`

Rate request:

```json
{
  "vocabularyId": 123,
  "rating": "good"
}
```

`rating` hỗ trợ:

- `repeat`
- `hard`
- `good`
- `easy`
- `again` (được map nội bộ thành `repeat`)

### 9.6 Stats - `/api/stats` (JWT)

1. `GET /api/stats/daily?start=YYYY-MM-DD&end=YYYY-MM-DD`
2. `GET /api/stats/summary`

Nếu thiếu `start`/`end`, backend mặc định lấy 30 ngày gần nhất.

### 9.7 Notifications - `/api/notifications` (JWT)

1. `GET /api/notifications` (unread)
2. `GET /api/notifications/recent` (tối đa 100)
3. `PUT /api/notifications/{notificationId}/read`
4. `POST /api/notifications/session-summary`
5. `GET /api/notifications/preferences`
6. `PUT /api/notifications/preferences`

Session summary request:

```json
{
  "correct": 8,
  "total": 10
}
```

Update preferences request:

```json
{
  "enableDailyReminder": true,
  "enableReviewReminder": true,
  "enableEmailNotification": true,
  "reminderTime": "08:30"
}
```

`reminderTime` format: `HH:mm`.

## 10) Scheduler và notification

Backend có 2 job chạy mỗi 5 phút:

1. Daily reminder: gửi nhắc học theo `reminderTime` của user (window ±5 phút)
2. Review-due reminder: gửi nhắc ôn từ đến hạn tại `reminderTime + 1 giờ`

Có cơ chế tránh gửi trùng thông báo trong cùng ngày theo `notification_type`.

## 11) Mẫu cURL nhanh

Đăng ký:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "123456",
    "fullName": "Test User",
    "learningGoal": "IELTS",
    "level": "B1"
  }'
```

Đăng nhập:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "123456"
  }'
```

Lấy profile (JWT):

```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer <TOKEN>"
```

Import vocab bằng CSV/XLSX:

```bash
curl -X POST http://localhost:8080/api/vocabularies/import/1 \
  -H "Authorization: Bearer <TOKEN>" \
  -F "file=@vocab.xlsx"
```

## 12) Format lỗi API

Global exception handler trả JSON dạng:

```json
{
  "timestamp": "2026-03-31T16:30:00",
  "message": "Noi dung loi",
  "status": 400
}
```

Một số status thường gặp:

- `400`: RuntimeException, validate fail, payload sai
- `401`: chưa đăng nhập / token không hợp lệ
- `403`: truy cập tài nguyên không thuộc user
- `404`: tài nguyên không tồn tại
- `500`: lỗi hệ thống

## 13) Tích hợp frontend

Frontend hiện tại ở workspace `minlish-websites` (Vite, thường chạy `http://localhost:5173`).
Backend đã mở CORS cho môi trường local dev.

## 14) Checklist trước khi deploy

1. Tắt reset dữ liệu tự động (`spring.sql.init.mode=never`) nếu dùng DB thật.
2. Chuyển toàn bộ secrets sang env vars/secret manager.
3. Giảm log debug (`logging.level.com.minlish=INFO` hoặc thấp hơn).
4. Rà lại allowed origins theo domain thực tế.
5. Kiểm tra SMTP account dùng cho production.
