package com.example.gaokao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.gaokao.common.exception.BusinessException;
import com.example.gaokao.entity.KnowledgeDoc;
import com.example.gaokao.mapper.KnowledgeDocMapper;
import com.example.gaokao.service.KnowledgeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class KnowledgeServiceImpl extends ServiceImpl<KnowledgeDocMapper, KnowledgeDoc> implements KnowledgeService {

    @Override
    public KnowledgeDoc create(KnowledgeDoc knowledgeDoc) {
        validate(knowledgeDoc);
        if (knowledgeDoc.getStatus() == null) {
            knowledgeDoc.setStatus(1);
        }
        save(knowledgeDoc);
        return knowledgeDoc;
    }

    @Override
    public List<KnowledgeDoc> listEnabled() {
        return list(new LambdaQueryWrapper<KnowledgeDoc>()
                .eq(KnowledgeDoc::getStatus, 1)
                .orderByDesc(KnowledgeDoc::getCreateTime));
    }

    @Override
    public KnowledgeDoc updateDoc(Long id, KnowledgeDoc knowledgeDoc) {
        validate(knowledgeDoc);
        knowledgeDoc.setId(id);
        updateById(knowledgeDoc);
        return getById(id);
    }

    @Override
    public void deleteDoc(Long id) {
        KnowledgeDoc doc = getById(id);
        if (doc != null) {
            doc.setStatus(0);
            updateById(doc);
        }
    }

    private void validate(KnowledgeDoc knowledgeDoc) {
        if (knowledgeDoc == null || !StringUtils.hasText(knowledgeDoc.getDocName())) {
            throw new BusinessException("请输入资料名称。");
        }
        if (!StringUtils.hasText(knowledgeDoc.getDocType())) {
            throw new BusinessException("请选择资料类型。");
        }
    }
}
