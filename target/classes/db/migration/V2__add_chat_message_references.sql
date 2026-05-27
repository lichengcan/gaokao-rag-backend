SET @column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'chat_message'
      AND COLUMN_NAME = 'references_json'
);

SET @ddl = IF(
    @column_exists = 0,
    'ALTER TABLE chat_message ADD COLUMN references_json LONGTEXT DEFAULT NULL COMMENT ''references json'' AFTER answer',
    'SELECT 1'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
