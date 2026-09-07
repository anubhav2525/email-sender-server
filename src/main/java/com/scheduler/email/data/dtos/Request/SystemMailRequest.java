package com.scheduler.email.data.dtos.Request;

import lombok.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SystemMailRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailRequest {
        private String to;
        private String subject;
        private String templateName;
        private Map<String, Object> variables;
    }
}
