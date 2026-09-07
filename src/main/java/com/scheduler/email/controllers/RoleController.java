package com.scheduler.email.controllers;

import com.scheduler.email.common.ApiVersion;
import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.common.PagedResponse;
import com.scheduler.email.data.dtos.Request.RoleRequest;
import com.scheduler.email.data.dtos.Response.RoleResponse;
import com.scheduler.email.services.RoleService;
import com.scheduler.email.utils.CreateResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.UUID;

@RequestMapping(ApiVersion.V1 + "/roles")
@RestController
@Slf4j
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    private final CreateResponseEntity createResponse;

    @PostMapping
    public ResponseEntity<CustomResponse<RoleResponse.Detail>> create(
            @Valid @RequestBody RoleRequest.Create request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Creating new role: {}", request.getDisplayName());
        return roleService.create(request, webRequest);
    }

    @GetMapping
    public ResponseEntity<CustomResponse<PagedResponse<RoleResponse.Summary>>> search(
            @Valid @ModelAttribute RoleRequest.Search request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Search request for roles");
        return roleService.search(request, webRequest);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<RoleResponse.Detail>> getById(
            @PathVariable(name = "id") UUID id,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Get role by id: {}", id);
        return roleService.getById(id, webRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<RoleResponse.Detail>> update(
            @PathVariable(name = "id") UUID id,
            @RequestBody RoleRequest.Update request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Update role by id: {}", id);
        return roleService.update(id, request, webRequest);
    }

    @PutMapping("/{id}/update-permissions")
    public ResponseEntity<CustomResponse<RoleResponse.Detail>> updatePermissions(
            @PathVariable(name = "id") UUID id,
            @RequestBody RoleRequest.UpdatePermissions request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Update permissions for role: {}", id);
        return roleService.updatePermissions(id, request, webRequest);
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<CustomResponse<RoleResponse.Detail>> enable(
            @PathVariable(name = "id") UUID id,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Enable role by id: {}", id);
        return roleService.enable(id, webRequest);
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<CustomResponse<RoleResponse.Detail>> disable(
            @PathVariable(name = "id") UUID id,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Disable role by id: {}", id);
        return roleService.disable(id, webRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> softDelete(
            @PathVariable(name = "id") UUID id,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Delete role by id: {}", id);
        return roleService.softDelete(id, webRequest);
    }

    @GetMapping("/stats")
    public ResponseEntity<CustomResponse<RoleResponse.Stats>> getStats(WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Get stats of roles");
        return roleService.getStats(webRequest);
    }
}
