package com.scheduler.email.services.impl;

import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.common.PagedResponse;
import com.scheduler.email.data.dtos.Request.PermissionRequest;
import com.scheduler.email.data.dtos.Response.PermissionsResponse;
import com.scheduler.email.data.entities.auth.Permissions;
import com.scheduler.email.exceptions.custom.ResourceAlreadyDisabledException;
import com.scheduler.email.exceptions.custom.ResourceAlreadyEnabledException;
import com.scheduler.email.exceptions.custom.ResourceNotExistsException;
import com.scheduler.email.repositories.auth.PermissionRepository;
import com.scheduler.email.security.AuthorizationCacheService;
import com.scheduler.email.services.PermissionService;
import com.scheduler.email.utils.BuildPageable;
import com.scheduler.email.utils.CreateResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.WebRequest;

import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private static final String RESOURCE = "Permission";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "resource", "action", "createdAt", "updatedAt", "enabled");
    private static final String DEFAULT_SORT_FIELD = "name";

    private final PermissionRepository permissionRepository;
    private final AuthorizationCacheService authorizationCacheService;
    private final CreateResponseEntity createResponseEntity;
    private final BuildPageable buildPageable;
    private final PermissionsResponse permissionsResponse;

    private Permissions getPermission(UUID id) {
        return permissionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotExistsException(RESOURCE + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<PermissionsResponse.Detail>> getById(UUID id, WebRequest webRequest) {
        log.info("{}", HttpStatus.OK);
        return createResponseEntity.buildResponse(
                RESOURCE + " fetched",
                permissionsResponse.toDetail(getPermission(id)),
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<PagedResponse<PermissionsResponse.Summary>>> search(PermissionRequest.Search request, WebRequest webRequest) {
        Pageable pageable = buildPageable.build(
                request.getPage(), request.getSize(),
                request.getSortBy(), request.getSortDir(),
                ALLOWED_SORT_FIELDS, DEFAULT_SORT_FIELD);

        Page<Permissions> page = request.getName() != null && !request.getName().isBlank()
                ? permissionRepository.search(request.getName(), pageable)
                : request.getEnabled() != null
                ? permissionRepository.findAllByEnabledAndDeletedFalse(request.getEnabled(), pageable)
                : permissionRepository.findAllByDeletedFalse(pageable);

        return createResponseEntity.buildResponse(
                RESOURCE + " list",
                PagedResponse.of(page.map(permissionsResponse::toSummary)),
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<PermissionsResponse.Detail>> enable(UUID id, WebRequest webRequest) {
        Permissions permission = getPermission(id);

        if (Boolean.TRUE.equals(permission.getEnabled()))
            throw new ResourceAlreadyEnabledException(RESOURCE + " is already enabled");

        permission.setEnabled(true);
        permissionRepository.save(permission);

        authorizationCacheService.updateAllCaches();

        return createResponseEntity.buildResponse(
                RESOURCE + " enabled successfully",
                permissionsResponse.toDetail(permission),
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<PermissionsResponse.Detail>> disable(UUID id, WebRequest webRequest) {
        Permissions permission = getPermission(id);

        if (Boolean.FALSE.equals(permission.getEnabled()))
            throw new ResourceAlreadyDisabledException(RESOURCE + " is already disabled");

        permission.setEnabled(false);
        permissionRepository.save(permission);

        authorizationCacheService.updateAllCaches();
        log.info("{}", HttpStatus.OK);
        return createResponseEntity.buildResponse(
                RESOURCE + " disabled successfully",
                permissionsResponse.toDetail(permission),
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<PagedResponse<PermissionsResponse.Summary>>> getAllActivePermissions(PermissionRequest.AllActive request, WebRequest webRequest) {
        Pageable pageable = buildPageable.build(
                request.getPage(), request.getSize(),
                request.getSortBy(), request.getSortDir(),
                ALLOWED_SORT_FIELDS, DEFAULT_SORT_FIELD);

        Page<Permissions> page = permissionRepository.findAllByDeletedFalseAndEnabledTrue(pageable);

        return createResponseEntity
                .buildResponse(
                        "Permissions found",
                        PagedResponse.of(page.map(permissionsResponse::toSummary)),
                        HttpStatus.OK,
                        webRequest
                );
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<PermissionsResponse.Stats>> getStats(WebRequest webRequest) {
        PermissionRepository.PermissionStatsProjection raw =
                permissionRepository.getPermissionStats();

        PermissionsResponse.Stats stats = PermissionsResponse.Stats.builder()
                .totalPermissions(raw.getTotalPermissions())
                .activePermissions(raw.getActivePermissions())
                .disabledPermissions(raw.getDisabledPermissions())
                .deletedPermissions(raw.getDeletedPermissions())
                .build();

        return createResponseEntity.buildResponse(
                "Permissions stats fetched successfully",
                stats,
                HttpStatus.OK,
                webRequest
        );
    }
}
