package com.scheduler.email.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EndpointPolicy implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String httpMethod;
    private String pathPattern;
    private boolean isPublic;
    private String requiredPermission; // null if public
}
