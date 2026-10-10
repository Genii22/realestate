# Real Estate

Веб-приложение для объявлений о недвижимости: Spring Boot 3.2, Thymeleaf, Spring Security, PostgreSQL.

## Требования

- Java 17
- Maven
- PostgreSQL 16

## Запуск на macOS

### 1. Установить зависимости

Через [Homebrew](https://brew.sh):

```bash
brew install openjdk@17 maven postgresql@16
brew services start postgresql@16
```

Чтобы система видела Java 17:

```bash
sudo ln -sfn "$(brew --prefix openjdk@17)/libexec/openjdk.jdk" /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

Если Postgres не находит команды `createdb` и `psql`, добавьте его в `PATH`:

```bash
echo 'export PATH="/opt/homebrew/opt/postgresql@16/bin:$PATH"' >> ~/.zshrc && source ~/.zshrc
```

### 2. Склонировать проект

```bash
git clone https://github.com/Genii22/realestate.git
cd realestate
```

### 3. Создать базу данных

Приложение подключается к `realestate_db` на `localhost:5432` под пользователем `postgres` с паролем `password` (см. `src/main/resources/application.properties`).

Homebrew создаёт суперпользователя с именем вашей учётной записи macOS, а не `postgres`, поэтому создайте роль `postgres` и базу:

```bash
psql -d postgres -c "CREATE ROLE postgres WITH SUPERUSER LOGIN PASSWORD 'password';"
createdb -U postgres -h localhost realestate_db
```

Если у вас уже есть пользователь `postgres` с другим паролем, измените `spring.datasource.password` в `application.properties`.

Таблицы и демо-данные создаются автоматически при первом запуске из `src/main/resources/db/schema.sql` и `data.sql`.

### 4. Запустить

```bash
mvn spring-boot:run
```

Откройте http://localhost:8080.

## Демо-учётные записи

Пароль у всех демо-пользователей: `admin123`.

| Роль | Email |
|------|-------|
| Администратор | `admin@nedvizhimost.kg` |

Остальные пользователи перечислены в `src/main/resources/db/data.sql`.

## Загруженные фото

Фото объявлений сохраняются в папку `uploads/` в корне проекта. Она в `.gitignore`, поэтому в репозиторий не попадает. Чтобы перенести фото на другой компьютер, скопируйте эту папку вручную.

## Перенос данных между компьютерами

На старом компьютере:

```bash
pg_dump -U postgres -h localhost realestate_db > dump.sql
```

На новом, после создания пустой базы:

```bash
psql -U postgres -h localhost realestate_db < dump.sql
```

## Частые проблемы

- **`password authentication failed for user "postgres"`** — пароль роли `postgres` не совпадает с `application.properties`. Задайте его: `psql -d postgres -c "ALTER ROLE postgres PASSWORD 'password';"`.
- **`Connection refused` на порту 5432** — Postgres не запущен: `brew services start postgresql@16`.
- **`database "realestate_db" does not exist`** — выполните `createdb -U postgres -h localhost realestate_db`.
- **`Port 8080 was already in use`** — освободите порт или задайте другой: `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`.
- **`release version 17 not supported`** — Maven использует старую Java. Проверьте `java -version` и `mvn -version`.
