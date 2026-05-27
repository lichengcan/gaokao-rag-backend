package com.example.gaokao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_doc")
public class KnowledgeDoc {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String docName;
    private String docType;
    private String docYear;
    private String province;
    private String versionNo;
    private LocalDate effectiveDate;
    private String owner;
    private String description;
    private String source;
    private LocalDateTime lastSyncedAt;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
