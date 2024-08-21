# No-Code - PRODвижение

## Рабочая версия доступна [здесь](https://ноу-код.рф/)

### Реализованные фичи

- [x] Авторизация и регистрация
- [x] Планирование постов
- [x] Просмотр, редактирование и удаление постов (CRUD)
- [x] Просмотр статистики постов
- [x] Интеграция с Telegram
- [x] Превью постов
- [x] Поддержка медиа-контента (включая кружочки)
- [x] Поддержка тегов

### Как запустить (backend)

1. Склонировать репозиторий

```bash
git clone https://github.com/Central-University-IT-prod/PROD-No-Code-backend.git
```

2. Укажите переменные окружения в файле docker-compose.yml и установите сессию в микросервисе статистики

3. Запуск

```bash
docker compose up -d
```

4. Готово!

### Структура базы данных

![NoCodeBackend](https://github.com/Central-University-IT-prod/PROD-No-Code-backend/assets/74096901/13a732ce-e5cc-4e42-bcc0-ab8df9f99c09)
