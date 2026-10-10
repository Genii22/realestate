-- ============================================================
--  Демо-данные. Каждый INSERT защищён условием
--  WHERE NOT EXISTS (SELECT 1 FROM <target_table>),
--  поэтому данные заливаются ТОЛЬКО когда таблица пуста.
--  При рестарте проекта пользовательские данные сохраняются.
-- ============================================================

-- ===== Справочник: типы недвижимости =====
INSERT INTO property_types (name, description)
SELECT * FROM (VALUES
    ('Квартира',  'Жилое помещение в многоквартирном доме'),
    ('Дом',       'Частный жилой дом'),
    ('Комната',   'Отдельная комната'),
    ('Офис',      'Коммерческое офисное помещение'),
    ('Участок',   'Земельный участок')
) AS v(name, description)
WHERE NOT EXISTS (SELECT 1 FROM property_types);

-- ===== Города =====
INSERT INTO cities (name, region)
SELECT * FROM (VALUES
    ('Бишкек', 'Чуйская область')
) AS v(name, region)
WHERE NOT EXISTS (SELECT 1 FROM cities);

-- ===== Микрорайоны / жилмассивы / ориентиры =====
INSERT INTO districts (name, city_id, postal_code)
SELECT v.name, v.city_id, v.postal_code FROM (VALUES
    -- Центральные (id 1–7)
    ('Центр',                 1, NULL),
    ('Золотой квадрат',       1, NULL),
    ('Политех',               1, NULL),
    ('Рабочий городок',       1, NULL),
    ('Южные ворота',          1, NULL),
    ('Пишпек',                1, NULL),
    ('Молодая Гвардия',       1, NULL),
    -- Микрорайоны (id 8–19)
    ('1-й микрорайон',        1, NULL),
    ('2-й микрорайон',        1, NULL),
    ('3-й микрорайон',        1, NULL),
    ('4-й микрорайон',        1, NULL),
    ('5-й микрорайон',        1, NULL),
    ('6-й микрорайон',        1, NULL),
    ('7-й микрорайон',        1, NULL),
    ('8-й микрорайон',        1, NULL),
    ('9-й микрорайон',        1, NULL),
    ('10-й микрорайон',       1, NULL),
    ('11-й микрорайон',       1, NULL),
    ('12-й микрорайон',       1, NULL),
    -- Восток / Джал / соседние (id 20–26)
    ('Восток-5',              1, NULL),
    ('Улан',                  1, NULL),
    ('Тунгуч',                1, NULL),
    ('Асанбай',               1, NULL),
    ('Джал',                  1, NULL),
    ('Джал-15',               1, NULL),
    ('Кок-Жар',               1, NULL),
    -- Аламедин (id 27–29)
    ('Аламедин-1',            1, NULL),
    ('Аламедин-2',            1, NULL),
    ('Аламедин-3',            1, NULL),
    -- Киргизия (id 30–31)
    ('Киргизия-1',            1, NULL),
    ('Киргизия-2',            1, NULL),
    -- Жилмассивы (id 32–61)
    ('Арча-Бешик',            1, NULL),
    ('Ак-Ордо',               1, NULL),
    ('Ак-Орго',               1, NULL),
    ('Ак-Босого',             1, NULL),
    ('Бакай-Ата',             1, NULL),
    ('Калыс-Ордо',            1, NULL),
    ('Ынтымак',               1, NULL),
    ('Ынтымак-2',             1, NULL),
    ('Мурас',                 1, NULL),
    ('Мурас-Ордо',            1, NULL),
    ('Рухий-Мурас',           1, NULL),
    ('Рухий-Мурас-2',         1, NULL),
    ('Учкун',                 1, NULL),
    ('Биримдик',              1, NULL),
    ('Биримдик-Кут',          1, NULL),
    ('Кара-Жыгач',            1, NULL),
    ('Алтын-Казык',           1, NULL),
    ('Алтын-Ордо',            1, NULL),
    ('Энесай',                1, NULL),
    ('Дордой',                1, NULL),
    ('Дордой-2',              1, NULL),
    ('Келечек',               1, NULL),
    ('Ак-Тилек',              1, NULL),
    ('Чон-Арык',              1, NULL),
    ('Орто-Сай',              1, NULL),
    ('Ала-Тоо',               1, NULL),
    ('Ак-Жар',                1, NULL),
    ('Көк-Жар',               1, NULL),
    ('Достук',                1, NULL),
    ('Селекция',              1, NULL),
    -- Ориентиры (id 62–72)
    ('рядом с ЦУМ',           1, NULL),
    ('рядом с Вефой',         1, NULL),
    ('рядом с Филармонией',   1, NULL),
    ('рядом с Ошским рынком', 1, NULL),
    ('рядом с АУЦА',          1, NULL),
    ('рядом с Медакадемией',  1, NULL),
    ('рядом с Азия Молл',     1, NULL),
    ('рядом с БГУ',           1, NULL),
    ('рядом с ТЭЦ',           1, NULL),
    ('рядом с Дордой Плаза',  1, NULL),
    ('рядом с Асанбай Центр', 1, NULL)
) AS v(name, city_id, postal_code)
WHERE NOT EXISTS (SELECT 1 FROM districts);

-- ===== Пользователи. Пароль для всех демо-учёток: admin123 =====
INSERT INTO users (first_name, last_name, email, password_hash, phone, role, specialization)
SELECT * FROM (VALUES
    ('Администратор', 'Системный',  'admin@nedvizhimost.kg',
        '$2b$10$rzHK7oizXmfEms1hIlVl6.dWrY0hSqp0NJL7wFNvmv1ee0CbmCVJ.', '+996700100001', 'ADMIN',  CAST(NULL AS VARCHAR)),
    ('Азамат',  'Жумабеков',  'azamat@nedvizhimost.kg',
        '$2b$10$rzHK7oizXmfEms1hIlVl6.dWrY0hSqp0NJL7wFNvmv1ee0CbmCVJ.', '+996700100002', 'SELLER', 'SALE'),
    ('Айгуль',  'Сейталиева', 'aigul@nedvizhimost.kg',
        '$2b$10$rzHK7oizXmfEms1hIlVl6.dWrY0hSqp0NJL7wFNvmv1ee0CbmCVJ.', '+996700100003', 'AGENT',  'RENT'),
    ('Нурбек',  'Асанов',     'nurbek@nedvizhimost.kg',
        '$2b$10$rzHK7oizXmfEms1hIlVl6.dWrY0hSqp0NJL7wFNvmv1ee0CbmCVJ.', '+996700100004', 'SELLER', 'SALE'),
    ('Бегимай', 'Кадыркулова','begimai@nedvizhimost.kg',
        '$2b$10$rzHK7oizXmfEms1hIlVl6.dWrY0hSqp0NJL7wFNvmv1ee0CbmCVJ.', '+996700100005', 'AGENT',  'RENT')
) AS v(first_name, last_name, email, password_hash, phone, role, specialization)
WHERE NOT EXISTS (SELECT 1 FROM users);

-- ===== Демо-объекты =====
INSERT INTO properties (type_id, district_id, street, building_number, apartment_number, area, plot_area, rooms, floor, total_floors, year_built, description)
SELECT * FROM (VALUES
    (1, 1,  'Чуй',         '120', '45',    CAST(75.5  AS DECIMAL(10,2)), CAST(NULL  AS DECIMAL(10,2)), 3,    5,    9,  2005, 'Просторная 3-комнатная в Центре, отличное состояние, рядом ЦУМ'),
    (1, 24, 'Манаса',      '88',  '102',   CAST(92.0  AS DECIMAL(10,2)), CAST(NULL  AS DECIMAL(10,2)), 4,    10,   16, 2015, 'Евроремонт, панорамный вид на горы Ала-Тоо, мкр. Джал'),
    (2, 20, 'Боконбаева',  '34',  CAST(NULL AS VARCHAR), CAST(200.0 AS DECIMAL(10,2)), CAST(800.0 AS DECIMAL(10,2)), 5,    CAST(NULL AS INT), 2,  2010, 'Двухэтажный коттедж с гаражом, мкр. Восток-5'),
    (1, 27, 'Киевская',    '55',  '12',    CAST(48.0  AS DECIMAL(10,2)), CAST(NULL  AS DECIMAL(10,2)), 2,    3,    5,  1998, 'Уютная двухкомнатная, Аламедин-1, рядом парк Панфилова'),
    (1, 24, 'Юнусалиева',  '3',   '201',   CAST(65.0  AS DECIMAL(10,2)), CAST(NULL  AS DECIMAL(10,2)), 3,    20,   24, 2020, 'Новостройка в Джале, чистовая отделка'),
    (4, 1,  'Манаса',      '40',  CAST(NULL AS VARCHAR), CAST(120.0 AS DECIMAL(10,2)), CAST(NULL AS DECIMAL(10,2)), CAST(NULL AS INT), 3, 9, 2018, 'Офис в БЦ в Центре Бишкека, опенспейс')
) AS v(type_id, district_id, street, building_number, apartment_number, area, plot_area, rooms, floor, total_floors, year_built, description)
WHERE NOT EXISTS (SELECT 1 FROM properties);

-- ===== Демо-объявления =====
INSERT INTO listings (property_id, seller_id, title, deal_type, price, status, views_count)
SELECT * FROM (VALUES
    (1, 2, 'Продаётся 3-комнатная квартира в Центре Бишкека',     'SALE', CAST(4500000 AS DECIMAL(15,2)), 'ACTIVE', 142),
    (2, 2, 'Аренда 4-комнатной с видом на Ала-Тоо, мкр. Джал',    'RENT', CAST(45000   AS DECIMAL(15,2)), 'ACTIVE',  87),
    (3, 4, 'Продаётся двухэтажный коттедж в мкр. Восток-5',       'SALE', CAST(8500000 AS DECIMAL(15,2)), 'ACTIVE', 234),
    (4, 2, 'Сдаётся 2-комнатная в Аламедин-1, у парка Панфилова', 'RENT', CAST(22000   AS DECIMAL(15,2)), 'ACTIVE',  53),
    (5, 4, 'Продажа квартиры в новостройке мкр. Джал',            'SALE', CAST(3500000 AS DECIMAL(15,2)), 'ACTIVE', 196),
    (6, 4, 'Аренда офиса в БЦ в Центре Бишкека',                  'RENT', CAST(60000   AS DECIMAL(15,2)), 'ACTIVE',  45)
) AS v(property_id, seller_id, title, deal_type, price, status, views_count)
WHERE NOT EXISTS (SELECT 1 FROM listings);

-- ===== Демо-заявки (анонимные) =====
INSERT INTO applications (listing_id, applicant_id, applicant_name, applicant_phone, message, status)
SELECT v.listing_id, v.applicant_id, v.applicant_name, v.applicant_phone, v.message, v.status FROM (VALUES
    (1, CAST(NULL AS BIGINT), 'Эркин', '+996555010101', 'Хочу посмотреть квартиру в выходные', 'PENDING'),
    (2, CAST(NULL AS BIGINT), 'Айдай', '+996555020202', 'Интересует аренда с 1 июля',          'APPROVED'),
    (3, CAST(NULL AS BIGINT), 'Тилек', '+996555030303', 'Можно ли посмотреть на этой неделе?', 'PENDING'),
    (5, CAST(NULL AS BIGINT), 'Жибек', '+996555040404', 'Есть ли рассрочка?',                  'REJECTED')
) AS v(listing_id, applicant_id, applicant_name, applicant_phone, message, status)
WHERE NOT EXISTS (SELECT 1 FROM applications);

-- ===== Избранное (заметки риелторов) =====
INSERT INTO favorites (user_id, listing_id)
SELECT * FROM (VALUES (3, 1), (3, 3), (5, 2), (5, 5)) AS v(user_id, listing_id)
WHERE NOT EXISTS (SELECT 1 FROM favorites);
