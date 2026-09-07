package com.scheduler.email.services.impl;

import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.common.PagedResponse;
import com.scheduler.email.data.dtos.Request.RoleRequest;
import com.scheduler.email.data.dtos.Response.RoleResponse;
import com.scheduler.email.data.entities.auth.Permissions;
import com.scheduler.email.data.entities.auth.Roles;
import com.scheduler.email.exceptions.custom.*;
import com.scheduler.email.repositories.auth.RoleRepository;
import com.scheduler.email.security.AuthorizationCacheService;
import com.scheduler.email.services.RoleService;
import com.scheduler.email.utils.BuildPageable;
import com.scheduler.email.utils.CreateResponseEntity;
import com.scheduler.email.utils.StringOperations;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private static final String RESOURCE = "Role";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name", "displayName", "createdAt", "updatedAt", "enabled"
    );
    private static final String DEFAULT_SORT_FIELD = "createdAt";
    private final RoleRepository roleRepository;
    private final StringOperations stringOperations;
    private final BuildPageable buildPageable;
    private final RoleResponse roleResponse;
    private final CreateResponseEntity createResponse;
    private final AuthorizationCacheService authorizationCacheService;
    private final PermissionRoleService permissionRoleService;

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<RoleResponse.Detail>> create(RoleRequest.Create request, WebRequest webRequest) {
        String name = stringOperations.normalizeNameInUppercase(request.getName());
        if (roleRepository.existsByName(name))
            throw new ResourceAlreadyExistsException(
                    RESOURCE + " already exists with name: " + name);

        Set<Permissions> permissions = permissionRoleService.resolvePermissions(request.getPermissionIds());
        Roles role = Roles.builder()
                .name(name)
                .displayName(stringOperations.trimOrNull(request.getDisplayName()))
                .description(stringOperations.trimOrNull(request.getDescription()))
                .enabled(true)
                .deleted(false)
                .permissions(permissions)
                .build();
        role = roleRepository.save(role);
        authorizationCacheService.updateAllCaches();

        return createResponse.buildResponse(
                RESOURCE + " created",
                roleResponse.toDetail(role),
                HttpStatus.CREATED,
                webRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<RoleResponse.Detail>> getById(UUID id, WebRequest webRequest) {
        return createResponse.buildResponse(
                RESOURCE + " fetched successfully",
                roleResponse.toDetail(getRole(id)),
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<RoleResponse.Detail>> update(UUID id, RoleRequest.Update request, WebRequest webRequest) {
        Roles role = getRole(id);
        if (request.getName() != null && !request.getName().isBlank()) {
            String name = stringOperations.normalizeNameInUppercase(request.getName());
            roleRepository.findByNameAndDeletedFalse(name)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new ResourceAlreadyExistsException(RESOURCE + " already exists with name: " + name);
                    });
            role.setName(name);
        }
        role.setDisplayName(stringOperations.trimOrNull(request.getDisplayName()));
        role.setDescription(stringOperations.trimOrNull(request.getDescription()));

        roleRepository.save(role);
        authorizationCacheService.updateAllCaches();

        return createResponse.buildResponse(
                RESOURCE + " updated",
                roleResponse.toDetail(role),
                HttpStatus.OK,
                webRequest);
    }

    @Override
    public ResponseEntity<CustomResponse<RoleResponse.Detail>> updatePermissions(UUID id, RoleRequest.UpdatePermissions request, WebRequest webRequest) {
        Roles role = getRole(id);

        Set<Permissions> permissions = permissionRoleService.resolvePermissions(request.getPermissionIds());
        role.setPermissions(permissions);
        role = roleRepository.save(role);

        authorizationCacheService.updateAllCaches();

        return createResponse.buildResponse(
                RESOURCE + " updated",
                roleResponse.toDetail(role),
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<PagedResponse<RoleResponse.Summary>>> search(RoleRequest.Search request, WebRequest webRequest) {
        Pageable pageable = buildPageable.build(
                request.getPage(), request.getSize(),
                request.getSortBy(), request.getSortDir(),
                ALLOWED_SORT_FIELDS, DEFAULT_SORT_FIELD);

        Page<Roles> page = request.getKeyword() != null && !request.getKeyword().isBlank()
                ? roleRepository.search(request.getKeyword(), pageable)
                : roleRepository.findAllWithRelations(pageable);

        return createResponse.buildResponse(
                RESOURCE + " list fetched successfully",
                PagedResponse.of(page.map(roleResponse::toSummary)),
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<RoleResponse.Detail>> enable(UUID id, WebRequest webRequest) {
        Roles role = getRole(id);

        if (Boolean.TRUE.equals(role.getEnabled()))
            throw new ResourceAlreadyEnabledException(RESOURCE + " is already enabled");

        role.setEnabled(true);

        roleRepository.save(role);
        authorizationCacheService.updateAllCaches();

        return createResponse.buildResponse(
                RESOURCE + " enabled successfully",
                roleResponse.toDetail(role),
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<RoleResponse.Detail>> disable(UUID id, WebRequest webRequest) {
        Roles role = getRole(id);

        if (Boolean.FALSE.equals(role.getEnabled()))
            throw new ResourceAlreadyDisabledException(RESOURCE + " is already disabled");

        role.setEnabled(false);

        roleRepository.save(role);
        authorizationCacheService.updateAllCaches();

        return createResponse.buildResponse(
                RESOURCE + " disabled",
                roleResponse.toDetail(role),
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<Void>> softDelete(UUID id, WebRequest webRequest) {
        Roles role = getRole(id);
        if (role.getUsers() != null && !role.getUsers().isEmpty())
            throw new ResourceInUseException(RESOURCE + " is assigned to users and cannot be deleted");

        role.setDeleted(true);
        role.setEnabled(false);

        roleRepository.save(role);
        authorizationCacheService.updateAllCaches();

        return createResponse.buildResponse(
                RESOURCE + " removed",
                HttpStatus.OK,
                webRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<RoleResponse.Stats>> getStats(WebRequest webRequest) {
        RoleRepository.RoleStatsProjection raw = roleRepository.getRoleStats();
        RoleResponse.Stats stats = RoleResponse.Stats.builder()
                .totalRoles(raw.getTotalRoles())
                .activeRoles(raw.getActiveRoles())
                .disabledRoles(raw.getDisabledRoles())
                .deletedRoles(raw.getDeletedRoles())
                .build();

        return createResponse.buildResponse(
                "Record found",
                stats,
                HttpStatus.OK,
                webRequest
        );
    }

    private Roles getRole(UUID id) {
        return roleRepository.findWithPermissions(id)
                .orElseThrow(() -> new ResourceNotExistsException(RESOURCE + " not found with id: " + id));
    }
}
