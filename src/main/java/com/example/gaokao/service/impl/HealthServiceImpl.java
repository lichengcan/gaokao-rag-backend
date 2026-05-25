package com.example.gaokao.service.impl;

import com.example.gaokao.client.DifyClient;
import com.example.gaokao.service.HealthService;
import com.example.gaokao.vo.DifyHealthVO;
import org.springframework.stereotype.Service;

@Service
public class HealthServiceImpl implements HealthService {

    private final DifyClient difyClient;

    public HealthServiceImpl(DifyClient difyClient) {
        this.difyClient = difyClient;
    }

    @Override
    public DifyHealthVO dify() {
        long start = System.currentTimeMillis();
        try {
            difyClient.checkHealth();
            return DifyHealthVO.builder()
                    .available(true)
                    .status("UP")
                    .message("Dify 连接正常")
                    .latencyMs(System.currentTimeMillis() - start)
                    .build();
        } catch (Exception e) {
            return DifyHealthVO.builder()
                    .available(false)
                    .status("DOWN")
                    .message(e.getMessage())
                    .latencyMs(System.currentTimeMillis() - start)
                    .build();
        }
    }
}
