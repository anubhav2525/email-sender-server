package com.scheduler.email.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class EndpointAuthorizationFilter extends OncePerRequestFilter {
    private final AuthorizationCacheService authorizationCacheService;
    private final PathPatternParser pathPatternParser = new PathPatternParser();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = extractPath(request);
        return path.startsWith("/v1/auth/")
                || path.startsWith("/actuator/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/ws/")
                || path.startsWith("/uploads/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String method = request.getMethod();
        String path = extractPath(request);

        // ── 1. Load endpoint policies from cache ────────────────
        List<EndpointPolicy> policies;
        try {
            policies = authorizationCacheService.getEndpointPolicies();
        } catch (Exception e) {
            log.error("Failed to load endpoint policies — allowing request to proceed", e);
            filterChain.doFilter(request, response);
            return;
        }

        // ── 2. Find matching policy ─────────────────────────────
        EndpointPolicy matchedPolicy = findMatchingPolicy(policies, method, path);

        if (matchedPolicy == null) {
            // No policy registered → fallback to requiring authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || !(authentication.getPrincipal() instanceof UserPrincipal)) {
                writeErrorResponse(response, HttpStatus.UNAUTHORIZED,
                        "Authentication required", path);
                return;
            }
            filterChain.doFilter(request, response);
            return;
        }

        // ── 3. Public endpoint — allow without auth ─────────────
        if (matchedPolicy.isPublic()) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── 4. Protected endpoint — verify authentication ───────
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            writeErrorResponse(response, HttpStatus.UNAUTHORIZED,
                    "Authentication required", path);
            return;
        }

        // ── 5. Verify user holds the required permission ────────
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String requiredPermission = matchedPolicy.getRequiredPermission();

        if (requiredPermission == null || requiredPermission.isBlank()) {
            // Policy exists but no specific permission required — just needs auth
            filterChain.doFilter(request, response);
            return;
        }

        Set<String> userPermissions = authorizationCacheService
                .getUserPermissions(userPrincipal.getId());

        if (userPermissions.contains(requiredPermission)) {
            // User has the required permission
            filterChain.doFilter(request, response);
        } else {
            log.warn("Access denied: user {} lacks permission '{}' for {} {}",
                    userPrincipal.getId(), requiredPermission, method, path);
            writeErrorResponse(response, HttpStatus.FORBIDDEN,
                    "Access denied: insufficient permissions", path);
        }
    }

    // ═════════════════════════════════════════════════════════════
    //  PATH MATCHING
    // ═════════════════════════════════════════════════════════════

    /**
     * Extracts the servlet-relative path (strips context path like /api).
     */
    private String extractPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }

    /**
     * Finds the first endpoint policy matching the request method and path.
     * Uses Spring's {@link PathPattern} for pattern matching (supports {@code {id}},
     * {@code **}, etc.).
     */
    private EndpointPolicy findMatchingPolicy(
            List<EndpointPolicy> policies, String method, String path) {
        PathContainer pathContainer = PathContainer.parsePath(path);

        // Specific patterns (no variables) ko pehle check karo
        List<EndpointPolicy> sorted = policies.stream()
                .filter(p -> p.getHttpMethod().equalsIgnoreCase(method))
                .sorted(Comparator.comparingInt(p ->
                        p.getPathPattern().contains("{") ? 1 : 0))
                .toList();

        for (EndpointPolicy policy : sorted) {
            try {
                PathPattern pattern = pathPatternParser.parse(policy.getPathPattern());
                if (pattern.matches(pathContainer)) {
                    return policy;
                }
            } catch (Exception e) {
                log.warn("Invalid path pattern: '{}'", policy.getPathPattern(), e);
            }
        }
        return null;
    }

    // ═════════════════════════════════════════════════════════════
    //  ERROR RESPONSE
    // ═════════════════════════════════════════════════════════════

    /**
     * Writes a JSON error response consistent with {@code CustomResponse} format.
     * Avoids Jackson dependency by building JSON manually.
     */
    private void writeErrorResponse(
            HttpServletResponse response,
            HttpStatus status,
            String message,
            String path
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String timestamp = OffsetDateTime.now().toString();

        String json = """
                {"message":"%s","data":null,"errors":null,"status":"%s","timestamp":"%s","apiPath":"%s"}"""
                .formatted(
                        escapeJson(message),
                        status.name(),
                        timestamp,
                        escapeJson(path)
                );

        response.getWriter().write(json);
        response.getWriter().flush();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
