package br.com.sicredi.votacao_api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.Map;

@Schema(description = "Resposta padronizada de erro da API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        @Schema(
                description = "Data e hora em que o erro ocorreu",
                example = "2026-09-23T22:40:00Z"
        )
        OffsetDateTime timestamp,

        @Schema(
                description = "Código HTTP",
                example = "422"
        )
        int status,

        @Schema(
                description = "Descrição do status HTTP",
                example = "Unprocessable Content"
        )
        String error,

        @Schema(
                description = "Mensagem explicativa do erro",
                example = "O associado não está habilitado para votar"
        )
        String message,

        @Schema(
                description = "Endpoint que originou o erro",
                example = "/api/v1/pautas/1/votos"
        )
        String path,

        @Schema(
                description = "Erros específicos de validação dos campos",
                nullable = true
        )
        Map<String, String> fields
) {
}
