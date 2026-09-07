package com.scheduler.email.controllers;

import com.scheduler.email.common.ApiVersion;
import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.common.PagedResponse;
import com.scheduler.email.data.dtos.Request.PermissionRequest;
import com.scheduler.email.data.dtos.Response.PermissionsResponse;
import com.scheduler.email.services.PermissionService;
import com.scheduler.email.utils.CreateResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(ApiVersion.V1 + "/permissions")
public class PermissionController {
    private final PermissionService permissionService;
    private final CreateResponseEntity createResponse;

    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<PermissionsResponse.Detail>> getById(
            @PathVariable(name = "id") UUID id, WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Get permissions by id:{}", id);
        return permissionService.getById(id, webRequest);
    }

    @GetMapping
    public ResponseEntity<CustomResponse<PagedResponse<PermissionsResponse.Summary>>> search(
            @Valid @ModelAttribute PermissionRequest.Search request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Search request for permission");
        return permissionService.search(request, webRequest);
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<CustomResponse<PermissionsResponse.Detail>> enable(
            @PathVariable(name = "id") UUID id,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Enable permissions by id:{}", id);
        return permissionService.enable(id, webRequest);
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<CustomResponse<PermissionsResponse.Detail>> disable(
            @PathVariable(name = "id") UUID id,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Disable permissions by id:{}", id);
        return permissionService.disable(id, webRequest);
    }

    @GetMapping("/all-active")
    public ResponseEntity<CustomResponse<PagedResponse<PermissionsResponse.Summary>>> getAllActivePermissions(PermissionRequest.AllActive request, WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Get all active permissions");
        return permissionService.getAllActivePermissions(request, webRequest);
    }

    @GetMapping("/stats")
    public ResponseEntity<CustomResponse<PermissionsResponse.Stats>> getStats(WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Get stats of permissions");
        return permissionService.getStats(webRequest);
    }
}
