package com.example.gaokao.controller;

import com.example.gaokao.common.Result;
import com.example.gaokao.service.HealthService;
import com.example.gaokao.vo.DifyHealthVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/dify")
    public Result<DifyHealthVO> dify() {
        return Result.success(healthService.dify());
    }
}
