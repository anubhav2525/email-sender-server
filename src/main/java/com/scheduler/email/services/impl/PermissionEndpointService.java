package com.scheduler.email.services.impl;

import com.scheduler.email.data.entities.auth.Permissions;
import com.scheduler.email.exceptions.custom.ResourceNotExistsException;
import com.scheduler.email.exceptions.custom.ResourceValidationException;
import com.scheduler.email.repositories.auth.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionEndpointService {
    private final PermissionRepository permissionRepository;

    public Permissions resolvePermission(UUID permissionId, boolean isPublic, Permissions existingPermission) {
        if (isPublic) {
            if (permissionId != null) {
                throw new ResourceValidationException("Public endpoints cannot have a permission");
            }
            return null;
        }

        UUID resolvedPermissionId = permissionId != null
                ? permissionId
                : existingPermission == null ? null : existingPermission.getId();

        if (resolvedPermissionId == null) {
            throw new ResourceValidationException("Protected endpoints require a permission");
        }

        return permissionRepository.findByIdAndDeletedFalse(resolvedPermissionId)
                .orElseThrow(() -> new ResourceNotExistsException("Permission not found with id: " + resolvedPermissionId));
    }
}
