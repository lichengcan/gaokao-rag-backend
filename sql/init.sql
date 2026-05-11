CREATE DATABASE IF NOT EXISTS gaokao_rag DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE gaokao_rag;

DROP TABLE IF EXISTS chat_message;
CREATE TABLE chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id VARCHAR(100) NOT NULL COMMENT '用户ID',
    conversation_id VARCHAR(100) DEFAULT NULL COMMENT 'Dify会话ID',
    question TEXT NOT NULL COMMENT '用户问题',
    answer TEXT COMMENT 'AI回答',
    message_source VARCHAR(50) DEFAULT 'DIFY' COMMENT '回答来源',
    status TINYINT DEFAULT 1 COMMENT '状态：1正常，0删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_create_time (create_time)
) COMMENT='问答记录表';

DROP TABLE IF EXISTS knowledge_doc;
CREATE TABLE knowledge_doc (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    doc_name VARCHAR(255) NOT NULL COMMENT '资料名称',
    doc_type VARCHAR(100) NOT NULL COMMENT '资料类型：高考政策、志愿填报、院校信息、专业信息、常见问题',
    doc_year VARCHAR(20) DEFAULT NULL COMMENT '资料年份',
    description TEXT COMMENT '资料说明',
    source VARCHAR(255) DEFAULT NULL COMMENT '资料来源',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用，0停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_doc_type (doc_type),
    INDEX idx_status (status)
) COMMENT='知识库资料说明表';

INSERT INTO knowledge_doc (doc_name, doc_type, doc_year, description, source, status) VALUES
('高考政策文件汇总', '高考政策', '2026', '各省高考政策、录取批次、投档规则等说明。', 'Dify 知识库', 1),
('志愿填报常见问题', '志愿填报', '2026', '平行志愿、服从调剂、冲稳保策略等问答资料。', 'Dify 知识库', 1),
('院校与专业基础信息', '院校信息', '2026', '院校层次、专业介绍、培养方向等资料说明。', 'Dify 知识库', 1);
