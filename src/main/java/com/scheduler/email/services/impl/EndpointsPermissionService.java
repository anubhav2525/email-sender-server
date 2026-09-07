package com.scheduler.email.services.impl;

import com.scheduler.email.data.entities.auth.Permissions;
import com.scheduler.email.repositories.auth.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EndpointsPermissionService {
    private final PermissionRepository permissionRepository;

    public Optional<Permissions> findPermission(UUID id) {
        return permissionRepository.findByIdAndDeletedFalse(id);
    }

    public String getPermissionName(UUID id) {
        Optional<Permissions> permissions = findPermission(id);
        return permissions.map(Permissions::getName).orElse(null);
    }
}
