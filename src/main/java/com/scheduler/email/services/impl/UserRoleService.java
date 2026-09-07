package com.scheduler.email.services.impl;

import com.scheduler.email.data.entities.auth.Roles;
import com.scheduler.email.exceptions.custom.ResourceNotExistsException;
import com.scheduler.email.repositories.auth.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRoleService {
    private final RoleRepository roleRepository;

    public Set<Roles> resolveRoles(Set<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty())
            return new HashSet<>();
        Set<Roles> roles = new HashSet<>(roleRepository.findAllById(roleIds));
        Set<UUID> foundIds = roles.stream().map(Roles::getId).collect(Collectors.toSet());
        List<UUID> missing = roleIds.stream().filter(id -> !foundIds.contains(id)).toList();
        if (!missing.isEmpty()) throw new ResourceNotExistsException("Roles not found: " + missing);
        return roles;
    }

    public Set<Roles> resolveRolesOrDefault(Set<UUID> roleIds) {
        if (roleIds != null && !roleIds.isEmpty())
            return resolveRoles(roleIds);
        return roleRepository.findByNameAndDeletedFalse("USER")
                .map(role -> new HashSet<>(Set.of(role)))
                .orElseGet(HashSet::new);
    }
}
