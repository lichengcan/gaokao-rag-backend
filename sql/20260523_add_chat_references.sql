USE gaokao_rag;

ALTER TABLE chat_message
    ADD COLUMN references_json LONGTEXT DEFAULT NULL COMMENT 'references json' AFTER answer;
