package com.scheduler.email.exceptions;

import com.scheduler.email.common.CustomResponse;
import com.scheduler.email.exceptions.custom.*;
import com.scheduler.email.utils.CreateResponseEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Order(999)
@RestControllerAdvice
@AllArgsConstructor
public class GlobalException {
    private final CreateResponseEntity createResponseEntity;
    private static final String handler = "Global exception";

    /**
     * Handles @Valid failures on @RequestBody -> 400
     *
     * @param ex      - MethodArgumentNotValidException
     * @param request - WebRequest
     * @return - 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return createResponseEntity.buildExceptionResponse(
                "Validation failed",
                fieldErrors,
                handler,
                request,
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Specially used in enum values
     *
     * @param ex      - HttpMessageNotReadableException
     * @param request - WebRequest
     * @return - 400 BAD REQUEST
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomResponse<Void>> handleUnreadable(
            HttpMessageNotReadableException ex,
            WebRequest request
    ) {
        String message = "Invalid request body";

        Throwable rootCause = ex.getMostSpecificCause();

        if (rootCause instanceof InvalidFormatException invalidFormatException) {

            Class<?> targetType = invalidFormatException.getTargetType();

            if (targetType.isEnum()) {

                String fieldName = invalidFormatException.getPath()
                        .stream()
                        .map(JacksonException.Reference::getPropertyName)
                        .findFirst()
                        .orElse("field");

                Object invalidValue = invalidFormatException.getValue();

                String acceptedValues = Arrays.stream(targetType.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                message = String.format(
                        "Invalid value '%s' for field '%s'. Accepted values are: [%s]",
                        invalidValue,
                        fieldName,
                        acceptedValues
                );
            }
        }

        return createResponseEntity.buildExceptionResponse(
                message,
                handler,
                request,
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * HTTP Operation Not Allowed
     *
     * @param ex      - HttpOperationNotAllowedException
     * @param request - WebRequest
     * @return - 405 BAD REQUEST
     */
    @ExceptionHandler(HttpOperationNotAllowedException.class)
    public ResponseEntity<CustomResponse<Void>> handleOperationNotAllowed(
            HttpOperationNotAllowedException ex,
            WebRequest request) {
        log.error("HTTP Operation Not Allowed Exception: {}", ex.getLocalizedMessage());
        log.trace(Arrays.toString(ex.getStackTrace()));
        return createResponseEntity.buildExceptionResponse(
                ex.getMessage(),
                handler,
                request,
                HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles missing @RequestParam -> 400
     *
     * @param ex      - MissingServletRequestParameterException
     * @param request - WebRequest
     * @return - 400 BAD REQUEST
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CustomResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException ex,
            WebRequest request) {
        log.error("Handles missing @RequestParam Exception: {}", ex.getLocalizedMessage());
        log.trace(Arrays.toString(ex.getStackTrace()));
        return createResponseEntity.buildExceptionResponse(
                "Required parameter '" + ex.getParameterName() + "' is missing",
                handler,
                request,
                HttpStatus.BAD_REQUEST);
    }

    /**
     * handles wrong type for path variable e.g. passing a string instead of UUID
     *
     * @param ex      - MethodArgumentTypeMismatchException
     * @param request - WebRequest
     * @return - 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CustomResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {
        String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'";
        return createResponseEntity.buildExceptionResponse(
                message,
                handler,
                request,
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Resource Already Exists Exception
     *
     * @param ex      - ResourceAlreadyExistsException
     * @param request - WebRequest
     * @return - 409 CONFLICT
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<CustomResponse<Void>> handleResourceAlreadyExists(
            ResourceAlreadyExistsException ex,
            WebRequest request) {
        return createResponseEntity.buildExceptionResponse(
                ex.getMessage(),
                handler,
                request,
                HttpStatus.CONFLICT);
    }

    /**
     * Token Expired Exception
     *
     * @param ex      - TokenExpiredException
     * @param request - WebRequest
     * @return - 403 FORBIDDEN
     */
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<CustomResponse<Void>> handleTokenExpiredException(
            TokenExpiredException ex,
            WebRequest request) {
        return createResponseEntity.buildExceptionResponse(
                ex.getMessage(),
                handler,
                request,
                HttpStatus.FORBIDDEN);
    }

    /**
     * Resource Not Found Exception
     *
     * @param ex      - ResourceNotExistsException
     * @param request - WebRequest
     * @return - 404 NOT FOUND
     */
    @ExceptionHandler(ResourceNotExistsException.class)
    public ResponseEntity<CustomResponse<Void>> handleResourceNotExists(
            ResourceNotExistsException ex,
            WebRequest request) {
        return createResponseEntity.buildExceptionResponse(
                ex.getMessage(),
                handler,
                request,
                HttpStatus.NOT_FOUND);
    }

    /**
     * Resource Already deleted Exception
     *
     * @param ex      - ResourceAlreadyDeletedException
     * @param request - WebRequest
     * @return - 208 ALREADY REPORTED
     */
    @ExceptionHandler(ResourceAlreadyDeletedException.class)
    public ResponseEntity<CustomResponse<Void>> handleResourceAlreadyDeleted(
            ResourceAlreadyDeletedException ex,
            WebRequest request) {
        return createResponseEntity.buildExceptionResponse(
                ex.getMessage(),
                handler,
                request,
                HttpStatus.ALREADY_REPORTED);
    }

    /**
     * Resource Validation Exception
     *
     * @param ex      - ResourceValidationException
     * @param request - WebRequest
     * @return - 400 BAD REQUEST
     */
    @ExceptionHandler(ResourceValidationException.class)
    public ResponseEntity<CustomResponse<Void>> handleResourceValidation(
            ResourceValidationException ex,
            WebRequest request) {
        return createResponseEntity.buildExceptionResponse(
                ex.getMessage(),
                handler,
                request,
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Resource Already Disabled exception
     *
     * @param ex      - ResourceAlreadyDisabledException
     * @param request - WebRequest
     * @return - 409 CONFLICT
     */
    @ExceptionHandler(ResourceAlreadyDisabledException.class)
    public ResponseEntity<CustomResponse<Void>> handleResourceAlreadyDisabled(
            ResourceAlreadyDisabledException ex,
            WebRequest request) {
        return createResponseEntity.buildExceptionResponse(
                ex.getMessage(),
                handler,
                request,
                HttpStatus.CONFLICT);
    }

    /**
     * Resource Not Restore Exception
     *
     * @param ex      - ResourceAlreadyEnabledException
     * @param request - WebRequest
     * @return - 408 ALREADY REPORTED
     */
    @ExceptionHandler(ResourceAlreadyEnabledException.class)
    public ResponseEntity<CustomResponse<Void>> handleResourceAlreadyEnabled(
            ResourceAlreadyEnabledException ex,
            WebRequest request) {
        return createResponseEntity.buildExceptionResponse(
                ex.getMessage(),
                handler,
                request,
                HttpStatus.ALREADY_REPORTED);
    }

    /**
     * Resource In USE Exception
     *
     * @param ex      - ResourceInUseException
     * @param request - WebRequest
     * @return - 226 I M USED
     */
    @ExceptionHandler(ResourceInUseException.class)
    public ResponseEntity<CustomResponse<Void>> handleResourceInUse(
            ResourceInUseException ex,
            WebRequest request) {
        return createResponseEntity.buildExceptionResponse(
                ex.getMessage(),
                handler,
                request,
                HttpStatus.IM_USED);
    }

    /**
     * Resource Not Deleted Exception
     *
     * @param ex      - ResourceNotDeletedException
     * @param request - WebRequest
     * @return - 400 BAD REQUEST
     */
    @ExceptionHandler(ResourceNotDeletedException.class)
    public ResponseEntity<CustomResponse<Void>> handleResourceNotDeleted(
            ResourceNotDeletedException ex,
            WebRequest request) {
        return createResponseEntity.buildExceptionResponse(
                ex.getMessage(),
                handler,
                request,
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Resource Not Restore Exception
     *
     * @param ex      - ResourceNotRestoreException
     * @param request - WebRequest
     * @return - 400 BAD REQUEST
     */
    @ExceptionHandler(ResourceNotRestoreException.class)
    public ResponseEntity<CustomResponse<Void>> handleResourceNotRestore(
            ResourceNotRestoreException ex,
            WebRequest request) {
        return createResponseEntity.buildExceptionResponse(
                ex.getMessage(),
                handler,
                request,
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Resource Operation Not Allowed Exception
     *
     * @param ex      - ResourceOperationNotAllowed
     * @param request - WebRequest
     * @return - 405 METHOD NOT ALLOWED
     */
    @ExceptionHandler(ResourceOperationNotAllowed.class)
    public ResponseEntity<CustomResponse<Void>> handleResourceOperationNotAllowed(
            ResourceOperationNotAllowed ex,
            WebRequest request) {
        return createResponseEntity.buildExceptionResponse(
                ex.getMessage(),
                handler,
                request,
                HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Access Denied Exception
     *
     * @param ex      - AccessDeniedException
     * @param request - WebRequest
     * @return - 403 FORBIDDEN
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CustomResponse<Void>> handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request) {
        return createResponseEntity.buildExceptionResponse(
                "Access denied",
                handler,
                request,
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnAuthorizedException.class)
    public ResponseEntity<CustomResponse<Void>> handleUnauthorized(
            UnAuthorizedException ex,
            WebRequest webRequest
    ) {
        return createResponseEntity.buildExceptionResponse(
                ex.getMessage(),
                handler,
                webRequest,
                HttpStatus.UNAUTHORIZED
        );
    }

    // Unauthorized access -> 401
//    @ExceptionHandler(AuthenticationException.class)
//    public ResponseEntity<CustomResponse<Void>> handleAuthentication(
//            AuthenticationException ex,
//            WebRequest request) {
//        return createResponseEntity.buildExceptionResponse(
//                "Authentication required",
//                handler,
//                request,
//                HttpStatus.UNAUTHORIZED);
//    }

    /**
     * Handle Generic Exception
     *
     * @param ex      - Exception
     * @param request - WebRequest
     * @return - 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse<Void>> handleGeneric(
            Exception ex,
            WebRequest request) {

        log.error("Unhandled exception", ex);
        log.error(Arrays.toString(ex.getStackTrace()));
        String message = ex.getMessage();

        if (message == null || message.isBlank()) {
            message = ex.getClass().getSimpleName();
        }

        return createResponseEntity.buildExceptionResponse(
                "An unexpected error occurred: " + message,
                handler,
                request,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
