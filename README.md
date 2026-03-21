# Защита Информации

Минимальный backend-проект на Spring Boot с фокусом на базовые требования безопасности:
- JWT-аутентификация (`access` + `refresh`)
- Ролевая авторизация (RBAC)
- HTTPS (TLS)
- PostgreSQL + Flyway
- Лицензирование устройств
- ЭЦП тикетов лицензии (RFC 8785 + `SHA256withRSA`)

## Эндпоинты
- `POST /api/auth/login` - получить `accessToken` и `refreshToken`
- `POST /api/auth/refresh` - обновить пару токенов
- `GET /api/public/ping` - публичный endpoint
- `GET /api/public/signature/certificate` - получить публичный сертификат для проверки подписи тикета
- `GET /api/user/me` - доступ `USER` или `ADMIN`
- `GET /api/admin/ping` - доступ `ADMIN`
- `GET /api/admin/licenses` - список всех лицензий и активированных устройств (только `ADMIN`)

## Роли
- `ROLE_USER`
- `ROLE_ADMIN`

## База данных
- Отдельная PostgreSQL БД для этого проекта: `information_protection_db`
- Порт контейнера на хосте: `5433` (маппинг `5433:5432`, чтобы не конфликтовать с локальным PostgreSQL на `5432`)
- Flyway миграции: `V1`, `V2`, `V5`, `V6`

## HTTPS
- `server.ssl.key-store=classpath:ssl/information-protection-keystore.p12`
- `server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}`

## Подпись тикетов лицензии
Подпись формируется сервером по схеме:
1. Payload тикета канонизируется по RFC 8785 (JCS).
2. Канонический JSON кодируется в UTF-8.
3. Данные подписываются алгоритмом `SHA256withRSA`.
4. Подпись возвращается в Base64.

Публичный сертификат для проверки подписи:
- `GET /api/public/signature/certificate`

Конфигурация подписи:
- `SIGNATURE_KEY_STORE_PATH` (например `classpath:ssl/server.p12` или `file:/path/to/signing-keystore.p12`)
- `SIGNATURE_KEY_STORE_TYPE` (`PKCS12` или `JKS`)
- `SIGNATURE_KEY_STORE_PASSWORD`
- `SIGNATURE_KEY_ALIAS` (опционально; если пусто, берётся первая key-entry из keystore)
- `SIGNATURE_KEY_PASSWORD` (опционально; по умолчанию = `SIGNATURE_KEY_STORE_PASSWORD`)
- `SIGNATURE_ALGORITHM` (по умолчанию `SHA256withRSA`)

## Генерация signing keystore
Скрипт:
- `init-scripts/generate-signature-keystore.ps1`

Пример запуска:
```powershell
powershell -ExecutionPolicy Bypass -File init-scripts/generate-signature-keystore.ps1 -StorePassword "<strong-pass>" -KeyPassword "<strong-pass>"
```

Скрипт создаёт:
- `Certificates/signature/signing-keystore.p12`
- `Certificates/signature/signing-public.pem`
- `Certificates/signature/signing-keystore.base64.txt` (для GitHub Secret `SIGNATURE_KEYSTORE_BASE64`)

## GitHub Secrets для CI/CD
Для сборки с секретным signing-keystore добавьте:
- `SIGNATURE_KEYSTORE_BASE64`
- `SIGNATURE_KEY_STORE_PASSWORD`
- `SIGNATURE_KEY_PASSWORD`
- `SIGNATURE_KEY_ALIAS`

`ci-cd.yml` уже содержит шаг восстановления `.secrets/signing-keystore.p12` из `SIGNATURE_KEYSTORE_BASE64`.

## Запуск
```bash
docker compose up -d
./mvnw spring-boot:run
```
