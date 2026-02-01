# BankAPI — RESTful API для управления банковскими картами <br>
<br>
## Описание проекта <br>
<br>
Это **RESTful API** для управления банковскими картами с поддержкой: <br>
<br>
- Регистрации и аутентификации пользователей (JWT) <br>
- Создания, просмотра, редактирования и удаления карт <br>
- Переводов между картами <br>
- Фильтрации и пагинации <br>
- Административных операций <br>
- Шифрования номеров карт в базе данных (PostgreSQL pgcrypto) <br>
<br>
## Технологии <br>
<br>
| Технология      | Версия/Назначение            | <br>
| Spring Boot     | 3.x — основной фреймворк     | <br>
| Spring Security | Аутентификация и авторизация | <br>
| JWT             | Токены для аутентификации    | <br>
| JPA/Hibernate   | ORM для работы с БД          | <br>
| PostgreSQL      | Реляционная база данных      | <br>
| pgcrypto        | Шифрование номеров карт      | <br>
| Validation      | Валидация входных данных     | <br>
| Swagger/OpenAPI | Документация API             | <br>
| BCrypt          | Хеширование паролей          | <br>
| Liquibase       | Миграции базы данных         | <br>
<br>
## Установка и запуск <br>
<br>
### Требования <br>
<br>
- Java 17+ <br>
- PostgreSQL 14+ <br>
- Maven <br>
<br>
### Настройка базы данных <br>
<br>
```sql <br>
-- Создание базы данных <br>
CREATE DATABASE bank_db; <br>
<br>
-- Включение расширения pgcrypto для шифрования <br>
CREATE EXTENSION IF NOT EXISTS pgcrypto; <br>
``` <br>
<br>
### Конфигурация `application.yml` <br>
<br>
```yaml <br>
server: <br>
  port: 8081 <br>
<br>
spring: <br>
  datasource: <br>
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:bank_db} <br>
    username: user <br>
    password: pass <br>
    driver-class-name: org.postgresql.Driver <br>
  jpa: <br>
    hibernate: <br>
      ddl-auto: none <br>
    properties: <br>
      hibernate: <br>
        dialect: org.hibernate.dialect.PostgreSQLDialect <br>
        format_sql: true <br>
        show-sql: true <br>
  liquibase: <br>
    change-log: classpath:/db/changelog/changelog.xml <br>
<br>
app: <br>
  jwt-secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970 <br>
  jwt-expiration-milliseconds: 86400000 # 24h <br>
<br>
encryption: <br>
  secret: my-encryption-secret-key <br>
<br>
logging: <br>
  level: <br>
    liquibase: INFO <br>
``` <br>
<br>
### Запуск приложения <br>
<br>
```bash <br>
# Сборка проекта <br>
mvn clean package <br>
<br>
# Запуск приложения <br>
java -jar target/bankcards-0.0.1-SNAPSHOT.jar <br>
``` <br>
<br>
Приложение будет доступно по адресу: `http://localhost:8081` <br>
<br>
## Аутентификация <br>
<br>
API использует **JWT (JSON Web Token)** для аутентификации. <br>
<br>
После успешной аутентификации сервер возвращает JWT токен, который нужно добавлять в заголовок всех последующих запросов: <br>
<br>
```
Authorization: Bearer <token>
``` <br>
<br>
## API Документация <br>
<br>
### 🔐 Auth Controller <br>
<br>
**Базовый путь:** `/api/auth` <br>
<br>
#### 1. Регистрация нового пользователя <br>
<br>
**POST** `/api/auth/register` <br>
<br>
**Запрос:** <br>
```json <br>
{ <br>
  "username": "john_doe", <br>
  "password": "secure123", <br>
  "role": "ROLE_USER" <br>
} <br>
``` <br>
<br>
**Ответ (201 Created):** <br>
```json <br>
{ <br>
  "id": 1, <br>
  "username": "john_doe", <br>
  "role": "ROLE_USER" <br>
} <br>
``` <br>
<br>
#### 2. Аутентификация и получение токена <br>
<br>
**POST** `/api/auth/login` <br>
<br>
**Запрос:** <br>
```json <br>
{ <br>
  "username": "john_doe", <br>
  "password": "secure123" <br>
} <br>
``` <br>
<br>
**Ответ (200 OK):** <br>
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsInVzZXJJZCI6MSwiaWF0IjoxNzA1MDAwMDAwLCJleHAiOjE3MDUwODY0MDB9...
``` <br>
<br>
### 💳 Card Controller <br>
<br>
**Базовый путь:** `/api/cards` <br>
**Требуется аутентификация** <br>
<br>
#### 3.1 Получение карт пользователя с пагинацией <br>
<br>
**GET** `/api/cards?page=0&size=10&sort=id,asc` <br>
<br>
**Ответ (200 OK):** <br>
```json <br>
{ <br>
  "content": [ <br>
    { <br>
      "id": 1, <br>
      "maskedCardNumber": "**** **** **** 4242", <br>
      "holderName": "John Doe", <br>
      "expiryDate": "2025-12-31", <br>
      "status": "ACTIVE", <br>
      "balance": 1000.0 <br>
    }, <br>
    { <br>
      "id": 2, <br>
      "maskedCardNumber": "**** **** **** 5678", <br>
      "holderName": "John Doe", <br>
      "expiryDate": "2026-06-30", <br>
      "status": "ACTIVE", <br>
      "balance": 500.5 <br>
    } <br>
  ], <br>
  "pageable": { <br>
    "pageNumber": 0, <br>
    "pageSize": 10, <br>
    "sort": { <br>
      "empty": false, <br>
      "sorted": true, <br>
      "unsorted": false <br>
    } <br>
  }, <br>
  "totalElements": 2, <br>
  "totalPages": 1, <br>
  "last": true, <br>
  "first": true, <br>
  "numberOfElements": 2, <br>
  "size": 10, <br>
  "number": 0 <br>
} <br>
``` <br>
<br>
#### 3.2 Получение всех карт пользователя (без пагинации) <br>
<br>
**GET** `/api/cards/all` <br>
<br>
**Ответ (200 OK):** <br>
```json <br>
[ <br>
  { <br>
    "id": 1, <br>
    "maskedCardNumber": "**** **** **** 4242", <br>
    "holderName": "John Doe", <br>
    "expiryDate": "2025-12-31", <br>
    "status": "ACTIVE", <br>
    "balance": 1000.0 <br>
  }, <br>
  { <br>
    "id": 2, <br>
    "maskedCardNumber": "**** **** **** 5678", <br>
    "holderName": "John Doe", <br>
    "expiryDate": "2026-06-30", <br>
    "status": "ACTIVE", <br>
    "balance": 500.5 <br>
  } <br>
] <br>
``` <br>
<br>
#### 3.3 Получение карты по ID <br>
<br>
**GET** `/api/cards/1` <br>
<br>
**Ответ (200 OK):** <br>
```json <br>
{ <br>
  "id": 1, <br>
  "maskedCardNumber": "**** **** **** 4242", <br>
  "holderName": "John Doe", <br>
  "expiryDate": "2025-12-31", <br>
  "status": "ACTIVE", <br>
  "balance": 1000.0 <br>
} <br>
``` <br>
<br>
#### 3.4 Создание новой карты <br>
<br>
**POST** `/api/cards` <br>
<br>
**Запрос:** <br>
```json <br>
{ <br>
  "cardNumber": "4242424242424242", <br>
  "holderName": "John Doe", <br>
  "expiryDate": "2025-12-31", <br>
  "balance": 1000.0 <br>
} <br>
``` <br>
<br>
**Ответ (200 OK):** <br>
```json <br>
{ <br>
  "id": 1, <br>
  "maskedCardNumber": "**** **** **** 4242", <br>
  "holderName": "John Doe", <br>
  "expiryDate": "2025-12-31", <br>
  "status": "ACTIVE", <br>
  "balance": 1000.0 <br>
} <br>
``` <br>
<br>
#### 3.5 Обновление статуса карты <br>
<br>
**PATCH** `/api/cards/1/status?status=BLOCKED` <br>
<br>
**Параметры:** <br>
- `status` — `ACTIVE`, `BLOCKED`, `EXPIRED` <br>
<br>
**Ответ (200 OK):** <br>
```json <br>
{ <br>
  "id": 1, <br>
  "maskedCardNumber": "**** **** **** 4242", <br>
  "holderName": "John Doe", <br>
  "expiryDate": "2025-12-31", <br>
  "status": "BLOCKED", <br>
  "balance": 1000.0 <br>
} <br>
``` <br>
<br>
#### 3.6 Удаление карты <br>
<br>
**DELETE** `/api/cards/1` <br>
<br>
**Ответ (204 No Content):** <br>
Пустое тело ответа <br>
<br>
#### 3.7 Перевод между картами пользователя <br>
<br>
**POST** `/api/cards/transfer` <br>
<br>
**Запрос:** <br>
```json <br>
{ <br>
  "sourceCardId": 1, <br>
  "destinationCardId": 2, <br>
  "amount": 100.5 <br>
} <br>
``` <br>
<br>
**Ответ (204 No Content):** <br>
Пустое тело ответа <br>
<br>
#### 3.8 Фильтрация карт пользователя <br>
<br>
**GET** `/api/cards/filter?status=ACTIVE&minBalance=500&page=0&size=10` <br>
<br>
**Параметры (все опциональные):** <br>
- `status` — `ACTIVE`, `BLOCKED`, `EXPIRED` <br>
- `expiryDateFrom` — дата в формате `YYYY-MM-DD` <br>
- `expiryDateTo` — дата в формате `YYYY-MM-DD` <br>
- `minBalance` — минимальный баланс <br>
- `maxBalance` — максимальный баланс <br>
- `page`, `size`, `sort` — параметры пагинации <br>
<br>
**Ответ (200 OK):** <br>
```json <br>
{ <br>
  "content": [ <br>
    { <br>
      "id": 1, <br>
      "maskedCardNumber": "**** **** **** 4242", <br>
      "holderName": "John Doe", <br>
      "expiryDate": "2025-12-31", <br>
      "status": "ACTIVE", <br>
      "balance": 1000.0 <br>
    } <br>
  ], <br>
  "totalElements": 1, <br>
  "totalPages": 1, <br>
  "last": true, <br>
  "first": true, <br>
  "numberOfElements": 1, <br>
  "size": 10, <br>
  "number": 0 <br>
} <br>
``` <br>
<br>
### 👨‍💼 Admin Controller <br>
<br>
**Базовый путь:** `/api/admin` <br>
**Требуется роль:** `ROLE_ADMIN` <br>
<br>
#### 4.1 Получение всех карт в системе <br>
<br>
**GET** `/api/admin/cards` <br>
<br>
**Ответ (200 OK):** <br>
```json <br>
[ <br>
  { <br>
    "id": 1, <br>
    "maskedCardNumber": "**** **** **** 4242", <br>
    "holderName": "John Doe", <br>
    "expiryDate": "2025-12-31", <br>
    "status": "ACTIVE", <br>
    "balance": 1000.0 <br>
  }, <br>
  { <br>
    "id": 2, <br>
    "maskedCardNumber": "**** **** **** 5678", <br>
    "holderName": "Jane Smith", <br>
    "expiryDate": "2026-06-30", <br>
    "status": "ACTIVE", <br>
    "balance": 2000.0 <br>
  } <br>
] <br>
``` <br>
<br>
#### 4.2 Создание карты для любого пользователя <br>
<br>
**POST** `/api/admin/cards?username=john_doe` <br>
<br>
**Запрос:** <br>
```json <br>
{ <br>
  "cardNumber": "4242424242424242", <br>
  "holderName": "John Doe", <br>
  "expiryDate": "2025-12-31", <br>
  "balance": 1000.0 <br>
} <br>
``` <br>
<br>
**Ответ (201 Created):** <br>
```json <br>
{ <br>
  "id": 1, <br>
  "maskedCardNumber": "**** **** **** 4242", <br>
  "holderName": "John Doe", <br>
  "expiryDate": "2025-12-31", <br>
  "status": "ACTIVE", <br>
  "balance": 1000.0 <br>
} <br>
``` <br>
<br>
#### 4.3 Обновление статуса любой карты <br>
<br>
**PATCH** `/api/admin/cards/1/status?status=BLOCKED` <br>
<br>
**Ответ (200 OK):** <br>
```json <br>
{ <br>
  "id": 1, <br>
  "maskedCardNumber": "**** **** **** 4242", <br>
  "holderName": "John Doe", <br>
  "expiryDate": "2025-12-31", <br>
  "status": "BLOCKED", <br>
  "balance": 1000.0 <br>
} <br>
``` <br>
<br>
#### 4.4 Удаление любой карты <br>
<br>
**DELETE** `/api/admin/cards/1` <br>
<br>
**Ответ (204 No Content):** <br>
Пустое тело ответа <br>
<br>
#### 4.5 Создание нового пользователя (админ) <br>
<br>
**POST** `/api/admin/users` <br>
<br>
**Запрос:** <br>
```json <br>
{ <br>
  "username": "new_user", <br>
  "password": "password123", <br>
  "role": "ROLE_USER" <br>
} <br>
``` <br>
<br>
**Ответ (201 Created):** <br>
```json <br>
{ <br>
  "id": 3, <br>
  "username": "new_user", <br>
  "role": "ROLE_USER" <br>
} <br>
``` <br>
<br>
#### 4.6 Удаление пользователя <br>
<br>
**DELETE** `/api/admin/users/3` <br>
<br>
**Ответ (204 No Content):** <br>
Пустое тело ответа <br>
<br>
#### 4.7 Фильтрация всех карт (админ) <br>
<br>
**GET** `/api/admin/cards/filter?status=ACTIVE&minBalance=1000&page=0&size=10` <br>
<br>
**Ответ (200 OK):** <br>
```json <br>
{ <br>
  "content": [ <br>
    { <br>
      "id": 1, <br>
      "maskedCardNumber": "**** **** **** 4242", <br>
      "holderName": "John Doe", <br>
      "expiryDate": "2025-12-31", <br>
      "status": "ACTIVE", <br>
      "balance": 1000.0 <br>
    }, <br>
    { <br>
      "id": 2, <br>
      "maskedCardNumber": "**** **** **** 5678", <br>
      "holderName": "Jane Smith", <br>
      "expiryDate": "2026-06-30", <br>
      "status": "ACTIVE", <br>
      "balance": 2000.0 <br>
    } <br>
  ], <br>
  "totalElements": 2, <br>
  "totalPages": 1, <br>
  "last": true, <br>
  "first": true, <br>
  "numberOfElements": 2, <br>
  "size": 10, <br>
  "number": 0 <br>
} <br>
``` <br>
<br>
## Обработка ошибок <br>
<br>
### Формат ошибок валидации <br>
<br>
**400 Bad Request:** <br>
```json <br>
{ <br>
  "cardNumber": "Номер карты должен состоять из 16 цифр", <br>
  "holderName": "Имя владельца не может быть пустым" <br>
} <br>
``` <br>
<br>
### Общие ошибки <br>
<br>
| Сообщение | HTTP Код | Ошибка | <br>
|-----------|----------|--------| <br>
| Недостаточно средств для перевода | 400 | `InsufficientFundsException` | <br>
| Обе карты должны быть активны для перевода | 400 | `InvalidCardOperationException` | <br>
| User with this username already exists | 400 | `UserAlreadyExistsException` | <br>
| Номер карты уже существует | 400 | `CardNumberExistsException` | <br>
| Invalid username or password | 401 | `BadCredentialsException` | <br>
| Access denied | 403 | `AccessDeniedException` | <br>
| Карта с идентификатором не найдена: 1 | 404 | `CardNotFoundException` | <br>
| User with username john_doe not found | 404 | `UserNotFoundException` | <br>
| Internal server error | 500 | `Exception` | <br>
<br>
## Swagger UI <br>
<br>
Документация API доступна через Swagger UI: <br>
<br>
**URL:** `http://localhost:8081/swagger-ui.html` <br>
