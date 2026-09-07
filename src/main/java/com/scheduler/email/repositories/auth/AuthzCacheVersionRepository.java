package com.scheduler.email.repositories.auth;

import com.scheduler.email.data.entities.auth.AuthzCacheVersion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthzCacheVersionRepository extends CrudRepository<AuthzCacheVersion, String> {
    @Query(value = "SELECT version FROM authz_cache_version WHERE key = 'GLOBAL'", nativeQuery = true)
    Long getGlobalVersion();
}
