package com.attus.prazos.web;

import com.attus.prazos.exception.ConflitoDeVersaoException;
import com.attus.prazos.exception.PrazoDuplicadoException;
import com.attus.prazos.exception.PrazoNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String UK_PRAZO_DUPLICADO = "uk_prazo_processo_descricao_hash_data";

    @ExceptionHandler(PrazoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNaoEncontrado(PrazoNaoEncontradoException ex, HttpServletRequest request) {
        log.warn("Recurso não encontrado em {}: {}", request.getRequestURI(), ex.getMessage());
        return new ApiError(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler(ConflitoDeVersaoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflitoVersao(ConflitoDeVersaoException ex, HttpServletRequest request) {
        log.warn("Conflito de versão em {}: {}", request.getRequestURI(), ex.getMessage());
        return new ApiError(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConcorrencia(ObjectOptimisticLockingFailureException ex,
            HttpServletRequest request) {
        log.warn("Conflito de concorrência (lock otimista) em {}", request.getRequestURI());
        return new ApiError(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "O prazo foi modificado por outra operação. Recarregue e tente novamente.",
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler(PrazoDuplicadoException.class)
    public ResponseEntity<ApiError> handleDuplicado(PrazoDuplicadoException ex,
            HttpServletRequest request) {
        log.warn("Conflito de prazo duplicado em {}: {}", request.getRequestURI(), ex.getMessage());
        ApiError corpo = new ApiError(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI(),
                List.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(corpo);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegridade(DataIntegrityViolationException ex,
            HttpServletRequest request) {

        if (violouConstraint(ex, UK_PRAZO_DUPLICADO)) {
            return handleDuplicado(new PrazoDuplicadoException(), request);
        }

        log.error("Violação de integridade inesperada em {}", request.getRequestURI(), ex);
        ApiError corpo = new ApiError(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Erro interno no servidor",
                request.getRequestURI(),
                List.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corpo);
    }

    private boolean violouConstraint(DataIntegrityViolationException ex, String nomeConstraint) {
        Throwable causa = ex.getCause();
        while (causa != null) {
            if (causa instanceof ConstraintViolationException cve) {
                String nome = cve.getConstraintName();
                return nome != null && nome.toLowerCase().contains(nomeConstraint);
            }
            causa = causa.getCause();
        }
        return false;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiError.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        log.warn("Requisição inválida em {}: {}", request.getRequestURI(), fieldErrors);
        return new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Erro de validação",
                request.getRequestURI(),
                fieldErrors);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleRotaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("Rota inexistente: {}", request.getRequestURI());
        return new ApiError(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "Recurso nao encontrado",
                request.getRequestURI(),
                List.of());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), ex);
        return new ApiError(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Erro interno no servidor",
                request.getRequestURI(),
                List.of());
    }
}
