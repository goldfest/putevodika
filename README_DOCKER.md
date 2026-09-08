# Путеводика — единый Docker Compose

Теперь одной командой запускаются:

- backend
- PostgreSQL/PostGIS
- Redis
- Mailpit
- OSRM

## 1. Создать `.env`

В PowerShell из корня проекта:

```powershell
Copy-Item .env.example .env
notepad .env
```

Вставьте JWT secret в `JWT_SECRET`.

`.env` уже исключен из Git.

## 2. Полный запуск

```powershell
docker compose down
docker compose up -d --build
```

Посмотреть состояние:

```powershell
docker compose ps
```

Логи backend:

```powershell
docker compose logs -f backend
```

## 3. Адреса

Backend / Swagger:

- http://localhost:8080/swagger-ui.html

Mailpit:

- http://localhost:8025

OSRM:

- http://localhost:5000

PostgreSQL с Windows:

- localhost:5433

Redis и SMTP Mailpit наружу не публикуются.
Backend обращается к ним внутри compose-сети:

- redis:6379
- mailpit:1025

## 4. После изменения Java-кода

Пересобрать только backend:

```powershell
docker compose up -d --build backend
```

Логи:

```powershell
docker compose logs -f backend
```

## 5. Полная остановка

```powershell
docker compose down
```

Данные PostgreSQL при этом сохраняются в named volume.

Не используйте `docker compose down -v`, если не хотите удалить локальную БД.
