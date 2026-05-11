package com.example.gaokao.controller;

import com.example.gaokao.common.Result;
import com.example.gaokao.service.AdminService;
import com.example.gaokao.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/statistics")
    public Result<StatisticsVO> statistics() {
        return Result.success(adminService.statistics());
    }
}
