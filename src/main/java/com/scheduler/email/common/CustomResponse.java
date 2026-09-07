package com.scheduler.email.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomResponse<T> {
    private String message;
    private T data;
    private T errors;
    private HttpStatus status;
    private OffsetDateTime timestamp;
    private String apiUrl;
}
