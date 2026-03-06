# Защита Информации

Минимальный backend-проект на Spring Boot с фокусом на базовые требования безопасности:
- JWT-аутентификация (`access` + `refresh`)
- Ролевая авторизация (RBAC)
- HTTPS (TLS)
- PostgreSQL + Flyway

## Эндпоинты
- `POST /api/auth/login` - получить `accessToken` и `refreshToken`
- `POST /api/auth/refresh` - обновить пару токенов
- `GET /api/public/ping` - публичный endpoint
- `GET /api/user/me` - доступ `USER` или `ADMIN`
- `GET /api/admin/ping` - доступ `ADMIN`

## Роли
- `ROLE_USER`
- `ROLE_ADMIN`

## База данных
- Отдельная PostgreSQL БД для этого проекта: `information_protection_db`
- Порт контейнера на хосте: `5433` (маппинг `5433:5432`), чтобы не конфликтовать с локальным PostgreSQL на `5432`
- Flyway миграции: `V1`, `V2`, `V5`

## HTTPS
- `server.ssl.key-store=classpath:ssl/information-protection-keystore.p12`
- `server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}`

## Запуск
```bash
docker compose up -d
./mvnw spring-boot:run
```
