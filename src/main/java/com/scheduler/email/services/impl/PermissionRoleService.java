package com.scheduler.email.services.impl;

import com.scheduler.email.data.entities.auth.Permissions;
import com.scheduler.email.exceptions.custom.ResourceNotExistsException;
import com.scheduler.email.repositories.auth.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PermissionRoleService {
    private final PermissionRepository permissionRepository;

    public Set<Permissions> resolvePermissions(Set<UUID> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty())
            return new HashSet<>();

        Set<Permissions> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        Set<UUID> foundIds = permissions
                .stream()
                .map(Permissions::getId)
                .collect(Collectors.toSet());

        List<UUID> missing = permissionIds
                .stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
        if (!missing.isEmpty())
            throw new ResourceNotExistsException("Permissions not found: " + missing);
        return permissions;
    }
}
