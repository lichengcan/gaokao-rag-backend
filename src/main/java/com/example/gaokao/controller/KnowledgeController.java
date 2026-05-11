package com.example.gaokao.controller;

import com.example.gaokao.common.Result;
import com.example.gaokao.entity.KnowledgeDoc;
import com.example.gaokao.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping
    public Result<KnowledgeDoc> create(@RequestBody KnowledgeDoc knowledgeDoc) {
        return Result.success(knowledgeService.create(knowledgeDoc));
    }

    @GetMapping("/list")
    public Result<List<KnowledgeDoc>> list() {
        return Result.success(knowledgeService.listEnabled());
    }

    @PutMapping("/{id}")
    public Result<KnowledgeDoc> update(@PathVariable Long id, @RequestBody KnowledgeDoc knowledgeDoc) {
        return Result.success(knowledgeService.updateDoc(id, knowledgeDoc));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.deleteDoc(id);
        return Result.success();
    }
}
