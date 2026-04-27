-- Tạo database nếu chưa tồn tại
CREATE DATABASE IF NOT EXISTS minlish;
USE minlish;

-- Tạo lại bảng để đồng bộ schema cho môi trường local
SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS learning_plans;
DROP TABLE IF EXISTS daily_stats;
DROP TABLE IF EXISTS study_history;
DROP TABLE IF EXISTS vocabularies;
DROP TABLE IF EXISTS vocabulary_sets;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS=1;

-- Bảng users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    learning_goal VARCHAR(50),
    level VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng learning_plans
CREATE TABLE IF NOT EXISTS learning_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    new_words_per_day INT DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng vocabulary_sets
CREATE TABLE IF NOT EXISTS vocabulary_sets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    tags VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng vocabularies
CREATE TABLE IF NOT EXISTS vocabularies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vocabulary_set_id BIGINT NOT NULL,
    word VARCHAR(255) NOT NULL,
    pronunciation VARCHAR(255),
    meaning VARCHAR(500) NOT NULL,
    description TEXT,
    example_sentence TEXT,
    fixed_phrase VARCHAR(500),
    related_words VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (vocabulary_set_id) REFERENCES vocabulary_sets(id) ON DELETE CASCADE,
    INDEX idx_vocabulary_set_id (vocabulary_set_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng study_history
CREATE TABLE IF NOT EXISTS study_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    vocabulary_id BIGINT NOT NULL,
    rating VARCHAR(20),
    ease_factor DECIMAL(3,2) DEFAULT 2.50,
    interval_days INT DEFAULT 1,
    next_review_date DATE,
    last_review_date DATE,
    repetitions INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (vocabulary_id) REFERENCES vocabularies(id) ON DELETE CASCADE,
    UNIQUE KEY uk_study_history_user_vocab (user_id, vocabulary_id),
    INDEX idx_user_id (user_id),
    INDEX idx_vocabulary_id (vocabulary_id),
    INDEX idx_next_review_date (next_review_date),
    INDEX idx_last_review_date (last_review_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng daily_stats
CREATE TABLE IF NOT EXISTS daily_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    study_date DATE NOT NULL,
    words_learned INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    incorrect_count INT DEFAULT 0,
    time_spent_seconds INT DEFAULT 0 COMMENT 'Tổng thời gian học (giây)',
    time_spent_hms TIME GENERATED ALWAYS AS (SEC_TO_TIME(time_spent_seconds)) STORED COMMENT 'Tổng thời gian học (HH:MM:SS)',
    new_words_learned INT DEFAULT 0 COMMENT 'Số từ mới học trong ngày',
    review_success_count INT DEFAULT 0 COMMENT 'Số từ ôn đúng trong ngày',
    review_total_count INT DEFAULT 0 COMMENT 'Tổng số từ ôn trong ngày',
    retention_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT 'Tỷ lệ ghi nhớ (%)',
    study_sessions INT DEFAULT 0 COMMENT 'Số phiên học trong ngày',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_date (user_id, study_date),
    INDEX idx_user_id (user_id),
    INDEX idx_study_date (study_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
        message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    notification_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng notification_preferences
CREATE TABLE IF NOT EXISTS notification_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    enable_daily_reminder BOOLEAN DEFAULT TRUE COMMENT 'Nhắc học mỗi ngày',
    enable_review_reminder BOOLEAN DEFAULT TRUE COMMENT 'Nhắc từ đến hạn ôn',
    enable_email_notification BOOLEAN DEFAULT TRUE COMMENT 'Gửi thông báo qua email',
    reminder_time TIME DEFAULT '08:00:00' COMMENT 'Giờ nhắc học (HH:MM)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Làm sạch dữ liệu mẫu
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE notification_preferences;
TRUNCATE TABLE notifications;
TRUNCATE TABLE learning_plans;
TRUNCATE TABLE daily_stats;
TRUNCATE TABLE study_history;
TRUNCATE TABLE vocabularies;
TRUNCATE TABLE vocabulary_sets;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS=1;

-- ============================================
-- DỮ LIỆU MẪU
-- ============================================

-- Thêm users (5 bản ghi)
INSERT INTO users (email, password, full_name, learning_goal, level, created_at, updated_at) VALUES
('tranvandu1409@gmail.com', '$2a$12$Cc8rf0fJqd07Ki4UaUwOV.WqkOyZm5/I70Nii7wFVz8AwuSCsqdtW', 'Zr1409', 'IELTS', 'B1', NOW(), NOW()),
('user2@gmail.com', '$2a$12$Cc8rf0fJqd07Ki4UaUwOV.WqkOyZm5/I70Nii7wFVz8AwuSCsqdtW', 'Trần Thị B', 'TOEIC', 'B2', NOW(), NOW()),
('user3@gmail.com', '$2a$12$Cc8rf0fJqd07Ki4UaUwOV.WqkOyZm5/I70Nii7wFVz8AwuSCsqdtW', 'Lê Minh C', 'Communication', 'A2', NOW(), NOW()),
('user4@gmail.com', '$2a$12$Cc8rf0fJqd07Ki4UaUwOV.WqkOyZm5/I70Nii7wFVz8AwuSCsqdtW', 'Phạm Anh D', 'IELTS', 'C1', NOW(), NOW()),
('user5@gmail.com', '$2a$12$Cc8rf0fJqd07Ki4UaUwOV.WqkOyZm5/I70Nii7wFVz8AwuSCsqdtW', 'Hoàng Thúy E', 'TOEIC', 'B1', NOW(), NOW());
--Tao 10.000 user test từ
--python e:/minlish-servers/src/main/resources/gen_bulk_users.py
--mysql -u root -p minlish < e:/minlish-servers/src/main/resources/bulk_users.sql
-- Thêm learning_plans (mỗi user 1 cấu hình)
INSERT INTO learning_plans (user_id, new_words_per_day, created_at, updated_at) VALUES
(1, 10, NOW(), NOW()),
(2, 12, NOW(), NOW()),
(3, 8, NOW(), NOW()),
(4, 15, NOW(), NOW()),
(5, 10, NOW(), NOW());

-- Thêm vocabulary_sets (5 bản ghi)
INSERT INTO vocabulary_sets (user_id, name, description, tags, created_at, updated_at) VALUES
(1, 'IELTS Academic Vocabulary', 'Từ vựng học thuật thường gặp trong IELTS', 'IELTS,Academic', NOW(), NOW()),
(1, 'Business English', 'Từ vựng dành cho môi trường công ty', 'Business,Professional', NOW(), NOW()),
(2, 'Daily Conversation', 'Các từ dùng trong giao tiếp hàng ngày', 'Daily,Conversation', NOW(), NOW()),
(3, 'Travel English', 'Từ vựng du lịch và khám phá', 'Travel,Adventure', NOW(), NOW()),
(4, 'Technical Terms', 'Kỹ thuật và công nghệ', 'Technical,IT', NOW(), NOW());

-- Thêm vocabularies (mỗi bộ 5 từ = 25 bản ghi)

INSERT INTO vocabularies (vocabulary_set_id, word, pronunciation, meaning, description, example_sentence, fixed_phrase, related_words, notes, created_at, updated_at) VALUES
(1, 'Ubiquitous', '/juːˈbɪk.wɪ.təs/', 'Có mặt ở khắp nơi', 'Present, appearing, or found everywhere', 'Mobile phones are ubiquitous in modern society.', 'ubiquitous presence, ubiquitous technology', 'omnipresent, pervasive, widespread', 'IELTS Band 7+', NOW(), NOW()),
(1, 'Resilient', '/rɪˈzɪl.i.ənt/', 'Kiên cường, có sức chống chịu', 'Able to recover quickly from difficult conditions', 'Children are often more resilient than adults.', 'resilient economy, resilient spirit', 'tough, adaptable, strong', 'Speaking Band 8', NOW(), NOW()),
(1, 'Mitigate', '/ˈmɪt.ɪ.ɡeɪt/', 'Giảm nhẹ, làm dịu bớt', 'Make less severe, serious, or painful', 'Measures to mitigate climate change are essential.', 'mitigate risk, mitigate impact', 'alleviate, reduce, ease', 'Academic vocabulary', NOW(), NOW()),
(1, 'Pragmatic', '/præɡˈmæt.ɪk/', 'Thực dụng, thực tế', 'Dealing with things sensibly and realistically', 'We need a pragmatic approach to solve this.', 'pragmatic approach, pragmatic solution', 'practical, realistic, sensible', 'Business English', NOW(), NOW()),
(1, 'Eloquent', '/ˈel.ə.kwənt/', 'Hùng biện, lưu loát', 'Fluent or persuasive in speaking or writing', 'She gave an eloquent speech about human rights.', 'eloquent speaker, eloquent words', 'articulate, expressive, fluent', 'Speaking Band 8+', NOW(), NOW()),

(2, 'Leverage', '/ˈlev.ər.ɪdʒ/', 'Tận dụng, sử dụng hiệu quả', 'Use something to maximum advantage', 'Companies leverage technology to improve efficiency.', 'leverage resources, leverage position', 'utilize, exploit, harness', 'Business term', NOW(), NOW()),
(2, 'Revenue', '/ˈrev.ə.njuː/', 'Doanh thu, thu nhập', 'Income generated by a business', 'The company increased revenue by 25% this year.', 'revenue stream, revenue growth', 'income, earnings, profit', 'Business term', NOW(), NOW()),
(2, 'Stakeholder', '/ˈsteɪk.hoʊl.dər/', 'Người có quyền lợi', 'Person with an interest or concern in something', 'We must consider all stakeholder perspectives.', 'stakeholder engagement, key stakeholder', 'investor, shareholder, participant', 'Business term', NOW(), NOW()),
(2, 'Synergy', '/ˈsɪn.ər.dʒi/', 'Hiệu ứng tương tác', 'Interaction producing enhanced effect', 'The merger created significant synergy benefits.', 'synergy effect, create synergy', 'cooperation, collaboration, combination', 'Business term', NOW(), NOW()),
(2, 'Compliance', '/kəmˈplaɪ.əns/', 'Tuân thủ, tuân theo', 'Action of complying with a rule or request', 'Full compliance with regulations is mandatory.', 'compliance with law, compliance standard', 'adherence, conformity, obedience', 'Business term', NOW(), NOW()),

(3, 'Cheers', '/tʃɪrz/', 'Tạm biệt, cảm ơn', 'Used as a friendly way to say goodbye', 'Cheers, see you tomorrow!', 'cheers, mate, goodbye', 'goodbye, bye, farewell', 'Casual speech', NOW(), NOW()),
(3, 'Grab', '/ɡræb/', 'Lấy nhanh, bắt', 'Take or seize something quickly', 'Let me grab a coffee before the meeting.', 'grab a bite, grab your bag', 'take, seize, snatch', 'Daily phrase', NOW(), NOW()),
(3, 'Chill', '/tʃɪl/', 'Thư giãn', 'Relax, spend time in a relaxed way', 'Let''s just chill this weekend.', 'chill out, chill with friends', 'relax, unwind, rest', 'Slang', NOW(), NOW()),
(3, 'Vibe', '/vaɪb/', 'Cảm giác, không khí', 'A feeling or atmosphere', 'I''m getting good vibes from this place.', 'good vibe, bad vibe', 'feeling, atmosphere, energy', 'Slang', NOW(), NOW()),
(3, 'Awesome', '/ˈɔ.səm/', 'Tuyệt vời', 'Extremely impressive or daunting', 'That''s an awesome idea!', 'awesome job, awesome experience', 'great, fantastic, wonderful', 'Casual', NOW(), NOW()),

(4, 'Itinerary', '/aɪˈtɪn.ə.rer.i/', 'Lịch trình', 'A planned route or journey', 'Here''s my travel itinerary for next month.', 'travel itinerary, detailed itinerary', 'route, plan, schedule', 'Travel term', NOW(), NOW()),
(4, 'Accommodation', '/ə.kɒm.ə.ˈdeɪ.ʃən/', 'Chỗ ở, hầu hế', 'A place where someone can live', 'We booked a beautiful accommodation in Paris.', 'accommodation options, budget accommodation', 'lodging, housing, hostel', 'Travel term', NOW(), NOW()),
(4, 'Souvenir', '/ˌsuː.vəˈnɪr/', 'Quà lưu niệm', 'A thing that is kept as a reminder', 'I bought some souvenirs from the local market.', 'souvenir shop, travel souvenir', 'memento, keepsake, gift', 'Travel term', NOW(), NOW()),
(4, 'Scenic', '/ˈsen.ɪk/', 'Có cảnh đẹp', 'Displaying scenery or beautiful views', 'We took the scenic route through the mountains.', 'scenic view, scenic route, scenic beauty', 'picturesque, beautiful, pretty', 'Travel term', NOW(), NOW()),
(4, 'Amenity', '/əˈmen.ə.ti/', 'Tiện nghi', 'A useful or desirable feature', 'The hotel offers many amenities for guests.', 'amenity services, hotel amenities', 'facility, convenience, service', 'Travel term', NOW(), NOW()),

(5, 'Algorithm', '/ˈæl.ɡə.rɪ.ðəm/', 'Thuật toán', 'Step-by-step procedure for solving a problem', 'The search algorithm works very efficiently.', 'machine learning algorithm, sorting algorithm', 'procedure, method, formula', 'Tech term', NOW(), NOW()),
(5, 'Interface', '/ˈɪn.tər.feɪs/', 'Giao diện', 'Device or program enabling communication', 'The user interface is very intuitive.', 'user interface, user-friendly interface', 'UI, display, dashboard', 'Tech term', NOW(), NOW()),
(5, 'Database', '/ˈdeɪ.tə.beɪs/', 'Cơ sở dữ liệu', 'Organized collection of data', 'We store all customer data in a database.', 'database management, database query', 'data storage, repository', 'Tech term', NOW(), NOW()),
(5, 'API', '/ˌeɪ.piː.ˈaɪ/', 'Giao diện lập trình ứng dụng', 'Interface for software communication', 'The REST API provides access to our services.', 'API endpoint, API integration', 'interface, integration, connection', 'Tech term', NOW(), NOW()),
(5, 'Encryption', '/ɪnˈkrip.ʃən/', 'Mã hoá', 'Process of converting data into code', 'End-to-end encryption protects user privacy.', 'data encryption, encryption key', 'security, encoding, cipher', 'Tech term', NOW(), NOW());

-- Từ test: quá hạn ôn để kích hoạt REVIEW_DUE_REMINDER
INSERT INTO vocabularies (vocabulary_set_id, word, pronunciation, meaning, description, example_sentence, fixed_phrase, related_words, notes, created_at, updated_at) VALUES
(1, 'Recall', '/rɪˈkɔːl/', 'Ôn lại, nhớ lại', 'Seed từ quá hạn để test nhắc ôn', 'Please recall this word today.', 'recall knowledge', 'remember, review', 'Test seed', NOW(), NOW());

-- Thêm study_history (5 bản ghi)
INSERT INTO study_history (user_id, vocabulary_id, rating, ease_factor, interval_days, next_review_date, last_review_date, repetitions, created_at) VALUES
(1, 1, 'good', 2.50, 3, CURDATE(), CURDATE() - INTERVAL 1 DAY, 1, NOW()),
(1, 6, 'easy', 2.50, 5, CURDATE(), CURDATE() - INTERVAL 2 DAY, 2, NOW() - INTERVAL 1 DAY),
(2, 11, 'hard', 2.35, 2, CURDATE() + INTERVAL 2 DAY, CURDATE(), 1, NOW()),
(3, 11, 'good', 2.40, 4, CURDATE() + INTERVAL 4 DAY, CURDATE() - INTERVAL 2 DAY, 2, NOW() - INTERVAL 2 DAY),
(4, 16, 'again', 2.20, 1, CURDATE(), CURDATE() - INTERVAL 3 DAY, 1, NOW() - INTERVAL 3 DAY);

-- Thêm daily_stats (7 bản ghi)
INSERT INTO daily_stats (user_id, study_date, words_learned, correct_count, incorrect_count, time_spent_seconds, new_words_learned, review_success_count, review_total_count, retention_rate, study_sessions) VALUES
(3, CURDATE() - INTERVAL 1 DAY, 7, 6, 1, 2100, 2, 3, 5, 60, 2),
(4, CURDATE() - INTERVAL 2 DAY, 12, 10, 2, 3000, 3, 5, 5, 100, 3),
(5, CURDATE() - INTERVAL 3 DAY, 6, 5, 1, 1500, 1, 4, 5, 80.0, 1),
(1, '2026-04-07', 1, 1, 1, 2400, 1, 1, 1, 80, 1),
(1, '2026-04-08', 1, 1, 1, 1920, 1, 1, 1, 60, 1);


-- Thêm notifications (8 bản ghi)
INSERT INTO notifications (user_id, message, is_read, notification_type, created_at) VALUES
(3, 'Chúc mừng! Bạn đã đạt 7 ngày học liên tiếp', TRUE, 'milestone', NOW() - INTERVAL 1 DAY),
(4, 'Từ mới: Business English được thêm vào bộ từ của bạn', TRUE, 'info', NOW() - INTERVAL 2 DAY),
(5, 'Bạn có 15 từ cần ôn hôm nay', FALSE, 'reminder', NOW() - INTERVAL 3 DAY),
(1, 'Hoàn thành phiên học\nBạn vừa học xong 5 từ, đúng 5 từ (100%).', FALSE, 'SESSION_SUMMARY', '2026-04-07'),
(1, 'Hoàn thành phiên học\nBạn vừa học xong 5 từ, đúng 5 từ (100%).', FALSE, 'SESSION_SUMMARY', '2026-04-08');

-- Thêm notification_preferences (mỗi user 1 bản ghi)
INSERT INTO notification_preferences (user_id, enable_daily_reminder, enable_review_reminder, enable_email_notification, reminder_time, created_at, updated_at) VALUES
(1, TRUE, TRUE, TRUE, '08:00:00', NOW(), NOW()),
(2, TRUE, TRUE, TRUE, '08:00:00', NOW(), NOW()),
(3, TRUE, FALSE, FALSE, '09:00:00', NOW(), NOW()),
(4, FALSE, TRUE, TRUE, '08:30:00', NOW(), NOW()),
(5, TRUE, TRUE, FALSE, '08:00:00', NOW(), NOW());

