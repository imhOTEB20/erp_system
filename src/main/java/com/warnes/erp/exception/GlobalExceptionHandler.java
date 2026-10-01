package com.warnes.erp.exception;

import jakarta.persistence.PessimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String TIMESTAMP_PROPERTY = "timestamp";
    private static final String BASE_TYPE_URI = "https://warnes.erp/errors/";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Recurso No Encontrado");
        problem.setType(URI.create(BASE_TYPE_URI + "not-found"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRuleException(BusinessRuleException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Inconsistencia en Regla de Negocio");
        problem.setType(URI.create(BASE_TYPE_URI + "business-rule-violation"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }

    @ExceptionHandler({ConcurrencyConflictException.class, CannotAcquireLockException.class, PessimisticLockException.class})
    public ProblemDetail handleConcurrencyException(Exception ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "El recurso está siendo modificado por otra transacción concurrente. Reintente la operación."
        );
        problem.setTitle("Conflicto de Concurrencia");
        problem.setType(URI.create(BASE_TYPE_URI + "concurrency-conflict"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "La operación viola una restricción de integridad en la base de datos (clave foránea o duplicidad)."
        );
        problem.setTitle("Violación de Integridad de Datos");
        problem.setType(URI.create(BASE_TYPE_URI + "data-integrity-violation"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "No cuenta con los privilegios suficientes para ejecutar esta acción."
        );
        problem.setTitle("Acceso Denegado");
        problem.setType(URI.create(BASE_TYPE_URI + "forbidden"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Fallaron una o más validaciones estructurales del payload enviado."
        );
        problem.setTitle("Error de Validación de Entrada");
        problem.setType(URI.create(BASE_TYPE_URI + "validation-error"));
        problem.setInstance(URI.create(servletWebRequest.getRequest().getRequestURI()));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("invalidParams", fieldErrors);

        return new ResponseEntity<>(problem, headers, status);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUncaughtException(Exception ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno no controlado en el servidor."
        );
        problem.setTitle("Error Interno del Servidor");
        problem.setType(URI.create(BASE_TYPE_URI + "internal-error"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(org.springframework.security.authentication.BadCredentialsException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Usuario o contraseña incorrectos."
        );
        problem.setTitle("Error de Autenticación");
        problem.setType(URI.create("https://warnes.erp/errors/unauthorized"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición (JSON) está malformado o contiene tipos de datos incompatibles."
        );
        problem.setTitle("Error de Lectura de Petición");
        problem.setType(URI.create(BASE_TYPE_URI + "bad-request"));
        problem.setInstance(URI.create(servletWebRequest.getRequest().getRequestURI()));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());

        // Opcional: Agregar el mensaje técnico real solo en entornos de desarrollo
        // problem.setProperty("debugMessage", ex.getMessage());

        return new ResponseEntity<>(problem, headers, status);
    }

    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    public ProblemDetail handleJwtException(io.jsonwebtoken.JwtException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "El token de acceso es inválido, ha expirado o su firma no coincide."
        );
        problem.setTitle("Token JWT Inválido");
        problem.setType(URI.create(BASE_TYPE_URI + "invalid-token"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }

    @ExceptionHandler(StockAlreadyInitializedException.class)
    public ProblemDetail handleStockAlreadyInitialized(StockAlreadyInitializedException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Regla de Negocio Violada");
        problem.setType(URI.create(BASE_TYPE_URI + "stock-unprocessable"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }

    @ExceptionHandler(ElementNotDeletedException.class)
    public ProblemDetail handleElementNotDeleted(ElementNotDeletedException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Regla de Negocio Violada");
        problem.setType(URI.create(BASE_TYPE_URI + "product-restore-unprocessable"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TIMESTAMP_PROPERTY, Instant.now());
        return problem;
    }
}