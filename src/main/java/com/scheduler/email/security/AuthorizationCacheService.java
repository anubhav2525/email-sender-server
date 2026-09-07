package com.scheduler.email.security;

import com.scheduler.email.data.entities.auth.Permissions;
import com.scheduler.email.repositories.auth.EndpointPermissionRepository;
import com.scheduler.email.repositories.auth.PermissionRepository;
import com.scheduler.email.repositories.auth.RoleRepository;
import com.scheduler.email.utils.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationCacheService {
    // ── Redis key constants ─────────────────────────────────────
    private static final String ENDPOINTS_KEY = "auth:endpoints";
    private static final String ROLES_KEY = "auth:roles";
    private static final String PERMISSIONS_KEY = "auth:permissions";
    private static final String USER_PERMS_PREFIX = "auth:user:";
    private static final String USER_PERMS_SUFFIX = "auth:permissions";
    private static final String USER_DETAILS_PREFIX = "auth:userdetails:";

    // ── TTL constants (seconds) ─────────────────────────────────
    private static final long USER_PERMS_TTL = 900;    // 15 minutes
    private static final long USER_DETAILS_TTL = 300;  // 5 minutes

    // ── Dependencies ────────────────────────────────────────────
    private final RedisService redisService;
    private final EndpointPermissionRepository endpointPermissionRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    // ── L1 in-memory cache ──────────────────────────────────────
    private volatile List<EndpointPolicy> localPolicyCache = null;
    private volatile List<RoleRecord> localRoleCache = null;
    private volatile List<PermissionRecord> localPermissionCache = null;

    // ═════════════════════════════════════════════════════════════
    //  ENDPOINT POLICIES
    // ═════════════════════════════════════════════════════════════

    /**
     * Returns the list of active endpoint policies.
     * Checks L1 (memory) → L2 (Redis) → L3 (DB).
     */
    @SuppressWarnings("unchecked")
    public List<EndpointPolicy> getEndpointPolicies() {
        // L1: in-memory
        if (localPolicyCache != null) {
            return localPolicyCache;
        }

        // L2: Redis
        try {
            Optional<Object> cached = redisService.get(ENDPOINTS_KEY);
            if (cached.isPresent()) {
                List<EndpointPolicy> policies = (List<EndpointPolicy>) cached.get();
                localPolicyCache = policies;
                return policies;
            }
        } catch (Exception e) {
            log.warn("Redis read failed for endpoint policies, falling back to DB", e);
        }

        // L3: Database
        return reloadEndpointPolicies();
    }

    // ═════════════════════════════════════════════════════════════
    //  ROLES
    // ═════════════════════════════════════════════════════════════

    /**
     * Returns the list of active roles.
     * Checks L1 (memory) → L2 (Redis) → L3 (DB).
     */
    @SuppressWarnings("unchecked")
    public List<RoleRecord> getRoles() {
        if (localRoleCache != null) {
            return localRoleCache;
        }

        try {
            Optional<Object> cached = redisService.get(ROLES_KEY);
            if (cached.isPresent()) {
                List<RoleRecord> roles = (List<RoleRecord>) cached.get();
                localRoleCache = roles;
                return roles;
            }
        } catch (Exception e) {
            log.warn("Redis read failed for roles, falling back to DB", e);
        }

        return reloadRoles();
    }

    // ═════════════════════════════════════════════════════════════
    //  PERMISSIONS
    // ═════════════════════════════════════════════════════════════

    /**
     * Returns the list of active permissions.
     * Checks L1 (memory) → L2 (Redis) → L3 (DB).
     */
    @SuppressWarnings("unchecked")
    public List<PermissionRecord> getPermissions() {
        if (localPermissionCache != null) {
            return localPermissionCache;
        }

        try {
            Optional<Object> cached = redisService.get(PERMISSIONS_KEY);
            if (cached.isPresent()) {
                List<PermissionRecord> permissions = (List<PermissionRecord>) cached.get();
                localPermissionCache = permissions;
                return permissions;
            }
        } catch (Exception e) {
            log.warn("Redis read failed for permissions, falling back to DB", e);
        }

        return reloadPermissions();
    }

// ═════════════════════════════════════════════════════════════
    //  USER PERMISSIONS
    // ═════════════════════════════════════════════════════════════

    /**
     * Returns the flat set of permission names for the given user.
     * Checks Redis first; on miss, loads from DB and caches with TTL.
     */
    @SuppressWarnings("unchecked")
    public Set<String> getUserPermissions(UUID userId) {
        String key = USER_PERMS_PREFIX + userId + USER_PERMS_SUFFIX;

        try {
            Optional<Object> cached = redisService.get(key);
            if (cached.isPresent()) {
                return (Set<String>) cached.get();
            }
        } catch (Exception e) {
            log.warn("Redis read failed for user permissions, falling back to DB", e);
        }

        // Cache miss → load from DB
        Set<String> permissions = permissionRepository.findPermissionNamesByUserId(userId);
        // Wrap in HashSet to ensure Serializable
        Set<String> serializablePermissions = new HashSet<>(permissions);

        try {
            redisService.set(key, (Serializable) serializablePermissions, USER_PERMS_TTL);
        } catch (Exception e) {
            log.warn("Failed to cache user permissions in Redis", e);
        }

        return serializablePermissions;
    }

    // ═════════════════════════════════════════════════════════════
    //  USER DETAILS (UserPrincipal cache for JwtAuthenticationFilter)
    // ═════════════════════════════════════════════════════════════

    /**
     * Returns cached UserPrincipal for the given email, or empty if not cached.
     */
    public Optional<UserPrincipal> getCachedUserDetails(String email) {
        String key = USER_DETAILS_PREFIX + email;
        try {
            Optional<Object> cached = redisService.get(key);
            if (cached.isPresent()) {
                return Optional.of((UserPrincipal) cached.get());
            }
        } catch (Exception e) {
            log.warn("Redis read failed for user details, falling back to DB", e);
        }
        return Optional.empty();
    }

    /**
     * Caches a UserPrincipal in Redis with a short TTL.
     */
    public void cacheUserDetails(String email, UserPrincipal principal) {
        String key = USER_DETAILS_PREFIX + email;
        try {
            redisService.set(key, principal, USER_DETAILS_TTL);
        } catch (Exception e) {
            log.warn("Failed to cache user details in Redis", e);
        }
    }

    // ═════════════════════════════════════════════════════════════
    //  EVICTION & UPDATE
    // ═════════════════════════════════════════════════════════════

    /**
     * Evicts a specific user's cached permissions and details.
     * Call when user's roles are changed.
     */
    public void evictUserCaches(UUID userId, String email) {
        try {
            redisService.delete(USER_PERMS_PREFIX + userId + USER_PERMS_SUFFIX);
            if (email != null) {
                redisService.delete(USER_DETAILS_PREFIX + email);
            }
        } catch (Exception e) {
            log.warn("Failed to evict user caches from Redis", e);
        }
    }

    /**
     * Evicts only user details cache by email.
     * Call when user's account status changes (enable/disable/delete).
     */
    public void evictUserDetails(String email) {
        if (email == null) return;
        try {
            redisService.delete(USER_DETAILS_PREFIX + email);
        } catch (Exception e) {
            log.warn("Failed to evict user details from Redis", e);
        }
    }

    /**
     * Evicts all endpoint policy caches (L1 + L2).
     * Call when endpoint_permissions, roles, or permissions data changes.
     */
    public void evictEndpointPolicies() {
        localPolicyCache = null;
        try {
            redisService.delete(ENDPOINTS_KEY);
        } catch (Exception e) {
            log.warn("Failed to evict endpoint policies from Redis", e);
        }
        log.info("Endpoint policy caches evicted");
    }

    /**
     * Evicts all authorization caches.
     * Nuclear option — use when broad RBAC changes occur (permission edits, role edits).
     */
    public void evictAllCaches() {
        localPolicyCache = null;
        localRoleCache = null;
        localPermissionCache = null;
        try {
            redisService.delete(ENDPOINTS_KEY);
            redisService.delete(ROLES_KEY);
            redisService.delete(PERMISSIONS_KEY);
            // User permission and details caches expire via TTL;
            // for immediate effect on specific users, call evictUserCaches() explicitly.
        } catch (Exception e) {
            log.warn("Failed to evict all caches from Redis", e);
        }
        log.info("All authorization caches evicted");
    }

    /**
     * Actively updates (reloads and caches) all roles, permissions, and endpoints in Redis.
     * Call when any role, permission, or endpoint changes.
     */
    public synchronized void updateAllCaches() {
        localPolicyCache = null;
        localRoleCache = null;
        localPermissionCache = null;

        reloadEndpointPolicies();
        reloadRoles();
        reloadPermissions();

        log.info("Successfully updated Redis with all records of roles, permissions, and endpoints");
    }

    // ═════════════════════════════════════════════════════════════
    //  CACHE WARMING
    // ═════════════════════════════════════════════════════════════

    /**
     * Pre-loads endpoint policies, roles, and permissions into L1 + L2 cache.
     * Called on application startup.
     */
    public void warmCache() {
        updateAllCaches();
        log.info("Authorization cache warmed on startup");
    }

    // ═════════════════════════════════════════════════════════════
    //  INTERNAL
    // ═════════════════════════════════════════════════════════════

    private synchronized List<EndpointPolicy> reloadEndpointPolicies() {
        // Double-check after acquiring lock
        if (localPolicyCache != null) {
            return localPolicyCache;
        }

        List<EndpointPolicy> policies = endpointPermissionRepository.findAllActivePolicies()
                .stream()
                .filter(ep -> Boolean.TRUE.equals(ep.getEnabled()))
                .map(ep -> new EndpointPolicy(
                        ep.getHttpMethod(),
                        ep.getPathPattern(),
                        Boolean.TRUE.equals(ep.getIsPublic()),
                        ep.getPermission() != null ? ep.getPermission().getName() : null
                ))
                .toList();

        // Wrap in ArrayList to ensure Serializable
        ArrayList<EndpointPolicy> serializablePolicies = new ArrayList<>(policies);

        try {
            redisService.set(ENDPOINTS_KEY, serializablePolicies);
        } catch (Exception e) {
            log.warn("Failed to cache endpoint policies in Redis", e);
        }

        localPolicyCache = serializablePolicies;
        log.info("Loaded {} endpoint policies into cache", serializablePolicies.size());
        return serializablePolicies;
    }

    private synchronized List<RoleRecord> reloadRoles() {
        if (localRoleCache != null) {
            return localRoleCache;
        }

        List<RoleRecord> roles = roleRepository.findAllWithPermissionsAndUsers()
                .stream()
                .filter(r -> Boolean.FALSE.equals(r.getDeleted()))
                .map(r -> new RoleRecord(
                        r.getId(),
                        r.getName(),
                        r.getDisplayName(),
                        r.getDescription(),
                        Boolean.TRUE.equals(r.getEnabled()),
                        r.getPermissions().stream().map(Permissions::getName).collect(Collectors.toSet())
                ))
                .toList();

        ArrayList<RoleRecord> serializableRoles = new ArrayList<>(roles);

        try {
            redisService.set(ROLES_KEY, serializableRoles);
        } catch (Exception e) {
            log.warn("Failed to cache roles in Redis", e);
        }

        localRoleCache = serializableRoles;
        log.info("Loaded {} roles into cache", serializableRoles.size());
        return serializableRoles;
    }

    private synchronized List<PermissionRecord> reloadPermissions() {
        if (localPermissionCache != null) {
            return localPermissionCache;
        }

        List<Permissions> permissions = permissionRepository.findAllByDeletedFalse();
        List<PermissionRecord> records = permissions.stream()
                .map(p -> new PermissionRecord(
                        p.getId(),
                        p.getName(),
                        p.getResource(),
                        p.getAction(),
                        p.getDescription(),
                        Boolean.TRUE.equals(p.getEnabled())
                ))
                .toList();

        ArrayList<PermissionRecord> serializablePermissions = new ArrayList<>(records);

        try {
            redisService.set(PERMISSIONS_KEY, serializablePermissions);
        } catch (Exception e) {
            log.warn("Failed to cache permissions in Redis", e);
        }

        localPermissionCache = serializablePermissions;
        log.info("Loaded {} permissions into cache", serializablePermissions.size());
        return serializablePermissions;
    }
}
