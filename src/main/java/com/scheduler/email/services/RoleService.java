package com.scheduler.email.services;

import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.common.PagedResponse;
import com.scheduler.email.data.dtos.Request.RoleRequest;
import com.scheduler.email.data.dtos.Response.RoleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.UUID;

public interface RoleService {
    ResponseEntity<CustomResponse<RoleResponse.Detail>> create(RoleRequest.Create request, WebRequest webRequest);

    ResponseEntity<CustomResponse<RoleResponse.Detail>> getById(UUID id, WebRequest webRequest);

    ResponseEntity<CustomResponse<RoleResponse.Detail>> update(UUID id, RoleRequest.Update request, WebRequest webRequest);

    ResponseEntity<CustomResponse<RoleResponse.Detail>> updatePermissions(UUID id, RoleRequest.UpdatePermissions request, WebRequest webRequest);

    ResponseEntity<CustomResponse<PagedResponse<RoleResponse.Summary>>> search(RoleRequest.Search request, WebRequest webRequest);

    ResponseEntity<CustomResponse<RoleResponse.Detail>> enable(UUID id, WebRequest webRequest);

    ResponseEntity<CustomResponse<RoleResponse.Detail>> disable(UUID id, WebRequest webRequest);

    ResponseEntity<CustomResponse<Void>> softDelete(UUID id, WebRequest webRequest);

    ResponseEntity<CustomResponse<RoleResponse.Stats>> getStats(WebRequest webRequest);
}
