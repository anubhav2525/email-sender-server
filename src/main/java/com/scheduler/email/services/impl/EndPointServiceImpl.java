package com.scheduler.email.services.impl;

import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.common.PagedResponse;
import com.scheduler.email.data.dtos.Request.EndPointRequest;
import com.scheduler.email.data.dtos.Response.EndPointResponse;
import com.scheduler.email.data.entities.auth.EndpointPermission;
import com.scheduler.email.exceptions.custom.*;
import com.scheduler.email.repositories.auth.EndpointPermissionRepository;
import com.scheduler.email.security.AuthorizationCacheService;
import com.scheduler.email.services.EndPointService;
import com.scheduler.email.utils.BuildPageable;
import com.scheduler.email.utils.CreateResponseEntity;
import com.scheduler.email.utils.StringOperations;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.WebRequest;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EndPointServiceImpl implements EndPointService {
    private static final String RESOURCE = "Endpoint permission";
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "httpMethod", "pathPattern", "isPublic", "enabled", "createdAt", "updatedAt"
    );
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    private final EndpointPermissionRepository endpointPermissionRepository;
    private final EndPointResponse endPointResponse;
    private final AuthorizationCacheService authorizationCacheService;
    private final CreateResponseEntity createResponseEntity;
    private final BuildPageable buildPageable;
    private final StringOperations stringOperations;

    private EndpointPermission getEndpointPermission(UUID id) {
        return endpointPermissionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotExistsException(
                        RESOURCE + " not found with id: " + id));
    }

    private String normalizeMethod(String method) {
        String normalized = stringOperations.trimOrNull(method);
        if (normalized == null) throw new ResourceValidationException(
                "HTTP method is required");
        return normalized.toUpperCase(Locale.ROOT);
    }

    private Specification<EndpointPermission> buildSearchSpec(EndPointRequest.Search request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));

            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String pattern = "%" + request.getKeyword().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("httpMethod")), pattern),
                        cb.like(cb.lower(root.get("pathPattern")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }
            if (request.getHttpMethod() != null && !request.getHttpMethod().isBlank()) {
                predicates.add(cb.equal(root.get("httpMethod"), normalizeMethod(request.getHttpMethod())));
            }
            if (request.getIsPublic() != null)
                predicates.add(cb.equal(root.get("isPublic"), request.getIsPublic()));
            if (request.getEnabled() != null)
                predicates.add(cb.equal(root.get("enabled"), request.getEnabled()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<EndPointResponse.Detail>> getById(UUID id, WebRequest webRequest) {
        return createResponseEntity.buildResponse(
                RESOURCE + " fetched successfully",
                endPointResponse.toDetail(getEndpointPermission(id)),
                HttpStatus.OK,
                webRequest
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<PagedResponse<EndPointResponse.Summary>>> search(EndPointRequest.Search request, WebRequest webRequest) {
        Pageable pageable = buildPageable.build(
                request.getPage(), request.getSize(),
                request.getSortBy(), request.getSortDir(),
                ALLOWED_SORT_FIELDS, DEFAULT_SORT_FIELD
        );

        Page<EndpointPermission> page = endpointPermissionRepository.findAll(
                buildSearchSpec(request), pageable);

        return createResponseEntity.buildResponse(
                RESOURCE + " list fetched successfully",
                PagedResponse.of(page.map(endPointResponse::toSummary)),
                HttpStatus.OK,
                webRequest
        );
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<EndPointResponse.Detail>> enable(UUID id, WebRequest webRequest) {
        EndpointPermission endpointPermission = getEndpointPermission(id);

        if (Boolean.TRUE.equals(endpointPermission.getEnabled()))
            throw new ResourceAlreadyEnabledException(RESOURCE + " is already enabled");

        endpointPermission.setEnabled(true);
        endpointPermissionRepository.save(endpointPermission);

        authorizationCacheService.updateAllCaches();

        return createResponseEntity.buildResponse(
                RESOURCE + " enabled successfully",
                endPointResponse.toDetail(endpointPermission),
                HttpStatus.OK,
                webRequest
        );
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<EndPointResponse.Detail>> disable(UUID id, WebRequest webRequest) {
        EndpointPermission endpointPermission = getEndpointPermission(id);

        if (Boolean.FALSE.equals(endpointPermission.getEnabled()))
            throw new ResourceAlreadyDisabledException(RESOURCE + " is already disabled");

        endpointPermission.setEnabled(false);
        endpointPermissionRepository.save(endpointPermission);

        authorizationCacheService.updateAllCaches();

        return createResponseEntity.buildResponse(
                RESOURCE + " disabled successfully",
                endPointResponse.toDetail(endpointPermission),
                HttpStatus.OK,
                webRequest
        );
    }

    @Override
    @Transactional
    public ResponseEntity<CustomResponse<Void>> softDelete(UUID id, WebRequest webRequest) {
        EndpointPermission endpointPermission = getEndpointPermission(id);

        endpointPermission.setDeleted(true);
        endpointPermission.setEnabled(false);

        endpointPermissionRepository.save(endpointPermission);
        authorizationCacheService.updateAllCaches();

        return createResponseEntity.buildResponse(
                RESOURCE + " deleted successfully",
                HttpStatus.OK,
                webRequest
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<CustomResponse<EndPointResponse.Stats>> getStats(WebRequest webRequest) {
        EndpointPermissionRepository.EndpointStatsProjection raw =
                endpointPermissionRepository.getEndpointStats();

        EndPointResponse.Stats stats = EndPointResponse.Stats.builder()
                .totalEndpoints(raw.getTotalEndpoints())
                .publicEndpoints(raw.getPublicEndpoints())
                .privateEndpoints(raw.getPrivateEndpoints())
                .activeEndpoints(raw.getActiveEndpoints())
                .disabledEndpoints(raw.getDisabledEndpoints())
                .deletedEndpoints(raw.getDeletedEndpoints())
                .build();

        return createResponseEntity.buildResponse(
                "Endpoint Permission stats fetched successfully",
                stats,
                HttpStatus.OK,
                webRequest
        );
    }
}
