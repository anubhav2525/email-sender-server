package com.scheduler.email.services;

import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.common.PagedResponse;
import com.scheduler.email.data.dtos.Request.PermissionRequest;
import com.scheduler.email.data.dtos.Response.PermissionsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.UUID;

public interface PermissionService {
    ResponseEntity<CustomResponse<PermissionsResponse.Detail>> getById(UUID id, WebRequest webRequest);

    ResponseEntity<CustomResponse<PagedResponse<PermissionsResponse.Summary>>> search(
            PermissionRequest.Search request, WebRequest webRequest);

    ResponseEntity<CustomResponse<PermissionsResponse.Detail>> enable(UUID id, WebRequest webRequest);

    ResponseEntity<CustomResponse<PermissionsResponse.Detail>> disable(UUID id, WebRequest webRequest);

    ResponseEntity<CustomResponse<PagedResponse<PermissionsResponse.Summary>>> getAllActivePermissions(PermissionRequest.AllActive request, WebRequest webRequest);

    ResponseEntity<CustomResponse<PermissionsResponse.Stats>> getStats(WebRequest webRequest);
}
