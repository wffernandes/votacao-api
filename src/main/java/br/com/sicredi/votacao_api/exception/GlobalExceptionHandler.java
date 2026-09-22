package br.com.sicredi.votacao_api.exception;


import br.com.sicredi.votacao_api.sessao.exception.PautaNaoEncontradaException;
import br.com.sicredi.votacao_api.sessao.exception.SessaoJaExistenteException;
import br.com.sicredi.votacao_api.voto.exception.AssociadoJaVotouException;
import br.com.sicredi.votacao_api.voto.exception.SessaoFechadaException;
import br.com.sicredi.votacao_api.voto.exception.SessaoNaoEncontradaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.OffsetDateTime;


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

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                OffsetDateTime.now(clock),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
}