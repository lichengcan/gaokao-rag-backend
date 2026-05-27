package com.example.gaokao.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DifyHealthVO {

    private boolean available;
    private String status;
    private String message;
    private Long latencyMs;
}
