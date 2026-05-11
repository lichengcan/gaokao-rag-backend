package com.example.gaokao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.gaokao.entity.KnowledgeDoc;

import java.util.List;

public interface KnowledgeService extends IService<KnowledgeDoc> {

    KnowledgeDoc create(KnowledgeDoc knowledgeDoc);

    List<KnowledgeDoc> listEnabled();

    KnowledgeDoc updateDoc(Long id, KnowledgeDoc knowledgeDoc);

    void deleteDoc(Long id);
}
