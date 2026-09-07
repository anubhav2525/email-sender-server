package com.scheduler.email.controllers;

import com.scheduler.email.common.ApiVersion;
import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.common.PagedResponse;
import com.scheduler.email.data.dtos.Request.EndPointRequest;
import com.scheduler.email.data.dtos.Response.EndPointResponse;
import com.scheduler.email.services.EndPointService;
import com.scheduler.email.utils.CreateResponseEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.UUID;

@RequestMapping(ApiVersion.V1 + "/endpoints")
@RestController
@Slf4j
@RequiredArgsConstructor
public class EndPointController {
    private final EndPointService endPointService;
    private final CreateResponseEntity createResponse;

    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<EndPointResponse.Detail>> getById(
            @PathVariable(name = "id") UUID id,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Get endpoint by id: {}", id);
        return endPointService.enable(id, webRequest);
    }

    @GetMapping
    public ResponseEntity<CustomResponse<PagedResponse<EndPointResponse.Summary>>> search(
            @Valid @ModelAttribute EndPointRequest.Search request,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Search request for endpoints");
        return endPointService.search(request, webRequest);
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<CustomResponse<EndPointResponse.Detail>> enable(
            @PathVariable(name = "id") UUID id,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Enable endpoint by id: {}", id);
        return endPointService.enable(id, webRequest);
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<CustomResponse<EndPointResponse.Detail>> disable(
            @PathVariable(name = "id") UUID id,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Disable endpoint by id: {}", id);
        return endPointService.disable(id, webRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> softDelete(
            @PathVariable(name = "id") UUID id,
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Delete endpoint by id: {}", id);
        return endPointService.softDelete(id, webRequest);
    }

    @GetMapping("/stats")
    public ResponseEntity<CustomResponse<EndPointResponse.Stats>> getStats(
            WebRequest webRequest) {
        log.info("API path: {}", createResponse.getApiPath(webRequest));
        log.info("Get stats for endpoints");
        return endPointService.getStats(webRequest);
    }

}
