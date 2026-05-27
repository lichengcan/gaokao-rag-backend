SET @feedback_status_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'chat_message'
      AND COLUMN_NAME = 'feedback_status'
);
SET @ddl = IF(
    @feedback_status_exists = 0,
    'ALTER TABLE chat_message ADD COLUMN feedback_status TINYINT DEFAULT NULL COMMENT ''1 like, -1 dislike'' AFTER references_json',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @feedback_comment_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'chat_message'
      AND COLUMN_NAME = 'feedback_comment'
);
SET @ddl = IF(
    @feedback_comment_exists = 0,
    'ALTER TABLE chat_message ADD COLUMN feedback_comment VARCHAR(500) DEFAULT NULL COMMENT ''feedback comment'' AFTER feedback_status',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @province_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'knowledge_doc'
      AND COLUMN_NAME = 'province'
);
SET @ddl = IF(
    @province_exists = 0,
    'ALTER TABLE knowledge_doc ADD COLUMN province VARCHAR(50) DEFAULT NULL COMMENT ''province'' AFTER doc_year',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @version_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'knowledge_doc'
      AND COLUMN_NAME = 'version_no'
);
SET @ddl = IF(
    @version_exists = 0,
    'ALTER TABLE knowledge_doc ADD COLUMN version_no VARCHAR(50) DEFAULT NULL COMMENT ''version'' AFTER province',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @effective_date_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'knowledge_doc'
      AND COLUMN_NAME = 'effective_date'
);
SET @ddl = IF(
    @effective_date_exists = 0,
    'ALTER TABLE knowledge_doc ADD COLUMN effective_date DATE DEFAULT NULL COMMENT ''effective date'' AFTER version_no',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @owner_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'knowledge_doc'
      AND COLUMN_NAME = 'owner'
);
SET @ddl = IF(
    @owner_exists = 0,
    'ALTER TABLE knowledge_doc ADD COLUMN owner VARCHAR(100) DEFAULT NULL COMMENT ''owner'' AFTER effective_date',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @last_synced_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'knowledge_doc'
      AND COLUMN_NAME = 'last_synced_at'
);
SET @ddl = IF(
    @last_synced_exists = 0,
    'ALTER TABLE knowledge_doc ADD COLUMN last_synced_at DATETIME DEFAULT NULL COMMENT ''last synced time'' AFTER source',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
