Описание проекта
Это RESTful API для управления банковскими картами с поддержкой:
Регистрации и аутентификации пользователей (JWT)
Создания, просмотра, редактирования и удаления карт
Переводов между картами
Фильтрации и пагинации
Административных операций
Шифрования номеров карт в базе данных (PostgreSQL pgcrypto)

Технологии
Версия/Назначение
Spring Boot 3.x — основной фреймворк
Spring Security Аутентификация и авторизация
JWT Токены для аутентификации
JPA/Hibernate ORM для работы с БД
PostgreSQL Реляционная база данных
pgcrypto Шифрование номеров карт
Validation Валидация входных данных
Swagger/OpenAPI Документация API
BCrypt Хеширование паролей

Установка и запуск 
Требования
Java 17+
PostgreSQL 14+
Maven
Настройка базы данных

Конфигурация application.yml
server:
port: 8081

spring:
datasource:
url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:bank_db}
username: user
password: pass
driver-class-name: org.postgresql.Driver
jpa:
hibernate:
ddl-auto: none
properties:
hibernate:
dialect: org.hibernate.dialect.PostgreSQLDialect
format_sql: true
show-sql: true
liquibase:
change-log: classpath:/db/changelog/changelog.xml

app:
jwt-secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt-expiration-milliseconds: 86400000 # 24h

encryption:
secret: my-encryption-secret-key

logging:
level:
liquibase: INFO

Аутентификация
API использует JWT (JSON Web Token) для аутентификации.
Получение токена
После успешной аутентификации сервер возвращает JWT токен, который нужно добавлять в заголовок всех последующих запросов:
http Authorization: Bearer <token>

API Документация
? Auth Controller
Базовый путь: /api/auth
1. Регистрация нового пользователя
   POST /api/auth/register
   Запрос: json
   {
   "username": "john_doe",
   "password": "secure123",
   "role": "ROLE_USER"
   }
   Ответ (201 Created): json
   {
   "id": 1,
   "username": "john_doe",
   "role": "ROLE_USER"
   }
2. Аутентификация и получение токена
   POST /api/auth/login
   Запрос: json
   {
   "username": "john_doe",
   "password": "secure123"
   }
   Ответ (200 OK): eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsInVzZXJJZCI6MSwiaWF0IjoxNzA1MDAwMDAwLCJleHAiOjE3MDUwODY0MDB9...
3. ? Card Controller
   Базовый путь: /api/cards
   Требуется аутентификация
3.1 Получение карт пользователя с пагинацией
   GET /api/cards?page=0&size=10&sort=id,asc
   Ответ (200 OK): json
   {
   "content": [
   {
   "id": 1,
   "maskedCardNumber": "**** **** **** 4242",
   "holderName": "John Doe",
   "expiryDate": "2025-12-31",
   "status": "ACTIVE",
   "balance": 1000.0
   },
   {
   "id": 2,
   "maskedCardNumber": "**** **** **** 5678",
   "holderName": "John Doe",
   "expiryDate": "2026-06-30",
   "status": "ACTIVE",
   "balance": 500.5
   }
   ],
   "pageable": {
   "pageNumber": 0,
   "pageSize": 10,
   "sort": {
   "empty": false,
   "sorted": true,
   "unsorted": false
   }
   },
   "totalElements": 2,
   "totalPages": 1,
   "last": true,
   "first": true,
   "numberOfElements": 2,
   "size": 10,
   "number": 0
   }
3.2 Получение всех карт пользователя (без пагинации)
   GET /api/cards/all
   Ответ (200 OK): json
   [
   {
   "id": 1,
   "maskedCardNumber": "**** **** **** 4242",
   "holderName": "John Doe",
   "expiryDate": "2025-12-31",
   "status": "ACTIVE",
   "balance": 1000.0
   },
   {
   "id": 2,
   "maskedCardNumber": "**** **** **** 5678",
   "holderName": "John Doe",
   "expiryDate": "2026-06-30",
   "status": "ACTIVE",
   "balance": 500.5
   }
   ]
3.3 Получение карты по ID
   GET /api/cards/{id}
   Пример: /api/cards/1
   Ответ (200 OK): json
   {
   "id": 1,
   "maskedCardNumber": "**** **** **** 4242",
   "holderName": "John Doe",
   "expiryDate": "2025-12-31",
   "status": "ACTIVE",
   "balance": 1000.0
   }
3.4 Создание новой карты
   POST /api/cards
   Запрос: json
   {
   "cardNumber": "4242424242424242",
   "holderName": "John Doe",
   "expiryDate": "2025-12-31",
   "balance": 1000.0
   }
   Ответ (200 OK): json
   {
   "id": 1,
   "maskedCardNumber": "**** **** **** 4242",
   "holderName": "John Doe",
   "expiryDate": "2025-12-31",
   "status": "ACTIVE",
   "balance": 1000.0
   }
3.5 Обновление статуса карты
    PATCH /api/cards/{id}/status?status=BLOCKED
    Параметры:
    status — ACTIVE, BLOCKED, EXPIRED
    Пример: /api/cards/1/status?status=BLOCKED
    Ответ (200 OK): json
    {
    "id": 1,
    "maskedCardNumber": "**** **** **** 4242",
    "holderName": "John Doe",
    "expiryDate": "2025-12-31",
    "status": "BLOCKED",
    "balance": 1000.0
    }
3.6 Удаление карты
   DELETE /api/cards/{id}
   Пример: /api/cards/1
   Ответ (204 No Content):
   Пустое тело ответа
   Curl: 
   curl -X DELETE http://localhost:8080/api/cards/1 \
   -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
3.7 Перевод между картами пользователя
   POST /api/cards/transfer
   Запрос: json
   {
   "sourceCardId": 1,
   "destinationCardId": 2,
   "amount": 100.5
   }
   Ответ (204 No Content):
   Пустое тело ответа
   Curl:
   curl -X POST http://localhost:8080/api/cards/transfer \
   -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
   -H "Content-Type: application/json" \
   -d '{
   "sourceCardId": 1,
   "destinationCardId": 2,
   "amount": 100.5
   }'
3.8 Фильтрация карт пользователя
   GET /api/cards/filter
   Параметры (все опциональные):
   status — ACTIVE, BLOCKED, EXPIRED
   expiryDateFrom — дата в формате YYYY-MM-DD
   expiryDateTo — дата в формате YYYY-MM-DD
   minBalance — минимальный баланс
   maxBalance — максимальный баланс
   page, size, sort — параметры пагинации
   Пример:
   /api/cards/filter?status=ACTIVE&minBalance=500&page=0&size=10
   Ответ (200 OK): json
   {
   "content": [
   {
   "id": 1,
   "maskedCardNumber": "**** **** **** 4242",
   "holderName": "John Doe",
   "expiryDate": "2025-12-31",
   "status": "ACTIVE",
   "balance": 1000.0
   }
   ],
   "totalElements": 1,
   "totalPages": 1,
   "last": true,
   "first": true,
   "numberOfElements": 1,
   "size": 10,
   "number": 0
   }
   Curl: 
   curl -X GET "http://localhost:8080/api/cards/filter?status=ACTIVE&minBalance=500&page=0&size=10" \
   -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
4. ??? Admin Controller
   Базовый путь: /api/admin
   Требуется роль: ROLE_ADMIN
4.1 Получение всех карт в системе
   GET /api/admin/cards
   Ответ (200 OK): json
   [
   {
   "id": 1,
   "maskedCardNumber": "**** **** **** 4242",
   "holderName": "John Doe",
   "expiryDate": "2025-12-31",
   "status": "ACTIVE",
   "balance": 1000.0
   },
   {
   "id": 2,
   "maskedCardNumber": "**** **** **** 5678",
   "holderName": "Jane Smith",
   "expiryDate": "2026-06-30",
   "status": "ACTIVE",
   "balance": 2000.0
   }
   ]
   Curl:
   curl -X GET http://localhost:8080/api/admin/cards \
   -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
4.2 Создание карты для любого пользователя
   POST /api/admin/cards?username=john_doe
   Запрос: json
   {
   "cardNumber": "4242424242424242",
   "holderName": "John Doe",
   "expiryDate": "2025-12-31",
   "balance": 1000.0
   }
   Ответ (201 Created): json
   {
   "id": 1,
   "maskedCardNumber": "**** **** **** 4242",
   "holderName": "John Doe",
   "expiryDate": "2025-12-31",
   "status": "ACTIVE",
   "balance": 1000.0
   }
4.3 Обновление статуса любой карты
   PATCH /api/admin/cards/{id}/status?status=BLOCKED
   Пример: /api/admin/cards/1/status?status=BLOCKED
   Ответ (200 OK): json
   {
   "id": 1,
   "maskedCardNumber": "**** **** **** 4242",
   "holderName": "John Doe",
   "expiryDate": "2025-12-31",
   "status": "BLOCKED",
   "balance": 1000.0
   }
4.4 Удаление любой карты
   DELETE /api/admin/cards/{id}
   Пример: /api/admin/cards/1
   Ответ (204 No Content):
   Пустое тело ответа
   Curl:
   curl -X DELETE http://localhost:8080/api/admin/cards/1 \
   -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
4.5 Создание нового пользователя (админ)
   POST /api/admin/users
   Запрос: json
   {
   "username": "new_user",
   "password": "password123",
   "role": "ROLE_USER"
   }
   Ответ (201 Created): json
   {
   "id": 3,
   "username": "new_user",
   "password": "$2a$10$...",
   "role": "ROLE_USER"
   }
4.6 Удаление пользователя
   DELETE /api/admin/users/{id}
   Пример: /api/admin/users/3
   Ответ (204 No Content):
   Пустое тело ответа
   Curl:
   curl -X DELETE http://localhost:8080/api/admin/users/3 \
   -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
4.7 Фильтрация всех карт (админ)
   GET /api/admin/cards/filter
   Параметры (все опциональные):
   status — ACTIVE, BLOCKED, EXPIRED
   expiryDateFrom — дата в формате YYYY-MM-DD
   expiryDateTo — дата в формате YYYY-MM-DD
   minBalance — минимальный баланс
   maxBalance — максимальный баланс
   page, size, sort — параметры пагинации
   Пример:
   /api/admin/cards/filter?status=ACTIVE&minBalance=1000&page=0&size=10
   Ответ (200 OK): json
   {
   "content": [
   {
   "id": 1,
   "maskedCardNumber": "**** **** **** 4242",
   "holderName": "John Doe",
   "expiryDate": "2025-12-31",
   "status": "ACTIVE",
   "balance": 1000.0
   },
   {
   "id": 2,
   "maskedCardNumber": "**** **** **** 5678",
   "holderName": "Jane Smith",
   "expiryDate": "2026-06-30",
   "status": "ACTIVE",
   "balance": 2000.0
   }
   ],
   "totalElements": 2,
   "totalPages": 1,
   "last": true,
   "first": true,
   "numberOfElements": 2,
   "size": 10,
   "number": 0
   }
5. Обработка ошибок
   Формат ошибок валидации
   400 Bad Request: json
   {
   "cardNumber": "Номер карты должен состоять из 16 цифр",
   "holderName": "Имя владельца не может быть пустым"
   }
   Общие ошибки   
   Сообщение                                        | HTTP Код | Ошибка
   Недостаточно средств для перевода                     400     InsufficientFundsException
   Обе карты должны быть активны для перевода            400     InvalidCardOperationException
   User with this username already exists                400     UserAlreadyExistsException
   Номер карты уже существует                            400     CardNumberAlreadyExistsException
   Invalid username or password                          401     BadCredentialsException
   Access denied                                         403     AccessDeniedException
   Карта с идентификатором не найдена: 1                 404     CardNotFoundException
   User with username john_doe not found                 404     UserNotFoundException
   Internal server error: ...                            500     Exception
6. Swagger UI
   Документация API доступна через Swagger UI:
   URL: http://localhost:8080/swagger-ui.html
   
