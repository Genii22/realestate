-- ============================================================
--  Схема БД: Информационная система рынка недвижимости (Бишкек)
--  Скрипт идемпотентный: безопасен при каждом рестарте,
--  существующие данные НЕ удаляются.
-- ============================================================

-- ===== ТАБЛИЦА: users =====
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(50)  NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone         VARCHAR(20),
    role          VARCHAR(10)  NOT NULL DEFAULT 'BUYER',
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);
-- Колонки, добавленные позже — подхватываем ALTER'ом для старых БД.
ALTER TABLE users ADD COLUMN IF NOT EXISTS specialization VARCHAR(10);

-- ===== ТАБЛИЦА: property_types =====
CREATE TABLE IF NOT EXISTS property_types (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);

-- ===== ТАБЛИЦА: cities =====
CREATE TABLE IF NOT EXISTS cities (
    id     BIGSERIAL PRIMARY KEY,
    name   VARCHAR(100) NOT NULL,
    region VARCHAR(100)
);

-- ===== ТАБЛИЦА: districts (микрорайоны/жилмассивы/ориентиры) =====
CREATE TABLE IF NOT EXISTS districts (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    city_id     BIGINT       NOT NULL REFERENCES cities(id) ON DELETE CASCADE,
    postal_code VARCHAR(10)
);

-- ===== ТАБЛИЦА: properties =====
CREATE TABLE IF NOT EXISTS properties (
    id               BIGSERIAL PRIMARY KEY,
    type_id          BIGINT         NOT NULL REFERENCES property_types(id),
    district_id      BIGINT         NOT NULL REFERENCES districts(id),
    street           VARCHAR(200)   NOT NULL,
    building_number  VARCHAR(10),
    apartment_number VARCHAR(10),
    area             DECIMAL(10,2)  NOT NULL CHECK (area > 0),
    rooms            INT            CHECK (rooms > 0),
    floor            INT,
    total_floors     INT,
    year_built       INT            CHECK (year_built >= 1800 AND year_built <= 2100),
    description      TEXT,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);
ALTER TABLE properties ADD COLUMN IF NOT EXISTS plot_area DECIMAL(10,2);

-- ===== ТАБЛИЦА: listings =====
CREATE TABLE IF NOT EXISTS listings (
    id           BIGSERIAL PRIMARY KEY,
    property_id  BIGINT          NOT NULL REFERENCES properties(id),
    seller_id    BIGINT          NOT NULL REFERENCES users(id),
    title        VARCHAR(255)    NOT NULL,
    deal_type    VARCHAR(10)     NOT NULL,
    price        DECIMAL(15,2)   NOT NULL CHECK (price > 0),
    status       VARCHAR(15)     NOT NULL DEFAULT 'ACTIVE',
    views_count  INT             NOT NULL DEFAULT 0,
    created_at   TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP       NOT NULL DEFAULT NOW(),
    expires_at   DATE
);

-- ===== ТАБЛИЦА: listing_images =====
CREATE TABLE IF NOT EXISTS listing_images (
    id          BIGSERIAL PRIMARY KEY,
    listing_id  BIGINT       NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    image_url   VARCHAR(500) NOT NULL,
    is_primary  BOOLEAN      NOT NULL DEFAULT FALSE,
    sort_order  INT          NOT NULL DEFAULT 0
);

-- ===== ТАБЛИЦА: applications =====
-- Анонимные заявки: applicant_id NULL + applicant_name/phone.
CREATE TABLE IF NOT EXISTS applications (
    id              BIGSERIAL PRIMARY KEY,
    listing_id      BIGINT      NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    applicant_id    BIGINT      REFERENCES users(id),
    applicant_name  VARCHAR(100),
    applicant_phone VARCHAR(20),
    message         TEXT,
    status          VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    visit_date      TIMESTAMP,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    CHECK (applicant_id IS NOT NULL OR (applicant_name IS NOT NULL AND applicant_phone IS NOT NULL))
);

-- ===== ТАБЛИЦА: deals =====
CREATE TABLE IF NOT EXISTS deals (
    id          BIGSERIAL PRIMARY KEY,
    listing_id  BIGINT        NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    buyer_id    BIGINT        NOT NULL REFERENCES users(id),
    final_price DECIMAL(15,2) NOT NULL CHECK (final_price > 0),
    deal_date   DATE,
    status      VARCHAR(15)   NOT NULL DEFAULT 'PENDING',
    notes       TEXT,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ===== ТАБЛИЦА: favorites =====
CREATE TABLE IF NOT EXISTS favorites (
    user_id    BIGINT    NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    listing_id BIGINT    NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, listing_id)
);

-- ===== ИНДЕКСЫ =====
CREATE INDEX IF NOT EXISTS idx_listings_status     ON listings(status);
CREATE INDEX IF NOT EXISTS idx_listings_deal_type  ON listings(deal_type);
CREATE INDEX IF NOT EXISTS idx_listings_price      ON listings(price);
CREATE INDEX IF NOT EXISTS idx_listings_seller     ON listings(seller_id);
CREATE INDEX IF NOT EXISTS idx_applications_applicant ON applications(applicant_id);
CREATE INDEX IF NOT EXISTS idx_applications_listing   ON applications(listing_id);
CREATE INDEX IF NOT EXISTS idx_properties_district ON properties(district_id);
CREATE INDEX IF NOT EXISTS idx_districts_city      ON districts(city_id);
