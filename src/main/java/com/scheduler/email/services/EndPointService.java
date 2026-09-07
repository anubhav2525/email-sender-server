package com.scheduler.email.services;

import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.common.PagedResponse;
import com.scheduler.email.data.dtos.Request.EndPointRequest;
import com.scheduler.email.data.dtos.Response.EndPointResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.UUID;

public interface EndPointService {
    ResponseEntity<CustomResponse<EndPointResponse.Detail>> getById(
            UUID id,
            WebRequest webRequest);

    ResponseEntity<CustomResponse<PagedResponse<EndPointResponse.Summary>>> search(
            EndPointRequest.Search request,
            WebRequest webRequest);

    ResponseEntity<CustomResponse<EndPointResponse.Detail>> enable(
            UUID id,
            WebRequest webRequest);

    ResponseEntity<CustomResponse<EndPointResponse.Detail>> disable(
            UUID id,
            WebRequest webRequest);

    ResponseEntity<CustomResponse<Void>> softDelete(
            UUID id,
            WebRequest webRequest);

    ResponseEntity<CustomResponse<EndPointResponse.Stats>> getStats(WebRequest webRequest);
}
