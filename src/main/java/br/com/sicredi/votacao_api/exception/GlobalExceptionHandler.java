package br.com.sicredi.votacao_api.exception;


import br.com.sicredi.votacao_api.sessao.exception.PautaNaoEncontradaException;
import br.com.sicredi.votacao_api.sessao.exception.SessaoJaExistenteException;
import br.com.sicredi.votacao_api.voto.exception.AssociadoJaVotouException;
import br.com.sicredi.votacao_api.voto.exception.ResultadoAindaIndisponivelException;
import br.com.sicredi.votacao_api.voto.exception.SessaoFechadaException;
import br.com.sicredi.votacao_api.voto.exception.SessaoNaoEncontradaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(PautaNaoEncontradaException.class)
    public ResponseEntity<ApiError> handlePautaNaoEncontrada(
            PautaNaoEncontradaException exception,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(SessaoNaoEncontradaException.class)
    public ResponseEntity<ApiError> handleSessaoNaoEncontrada(
            SessaoNaoEncontradaException exception,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(SessaoJaExistenteException.class)
    public ResponseEntity<ApiError> handleSessaoJaExistente(
            SessaoJaExistenteException exception,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(AssociadoJaVotouException.class)
    public ResponseEntity<ApiError> handleAssociadoJaVotou(
            AssociadoJaVotouException exception,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(SessaoFechadaException.class)
    public ResponseEntity<ApiError> handleSessaoFechada(
            SessaoFechadaException exception,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.UNPROCESSABLE_CONTENT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
    MethodArgumentNotValidException exception,
    HttpServletRequest request) {

        Map<String, String> fields = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fields.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ApiError error = new ApiError(
                OffsetDateTime.now(clock),
                status.value(),
                status.getReasonPhrase(),
                "Dados inválidos",
                request.getRequestURI(),
                fields
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Corpo da requisição inválido",
                request
        );
    }

    @ExceptionHandler(ResultadoAindaIndisponivelException.class)
    public ResponseEntity<ApiError> handleResultadoAindaIndisponivel(
            ResultadoAindaIndisponivelException exception,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.UNPROCESSABLE_CONTENT,
                exception.getMessage(),
                request
        );
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                OffsetDateTime.now(clock),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
}