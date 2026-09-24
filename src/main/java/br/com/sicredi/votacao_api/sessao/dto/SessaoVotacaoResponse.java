package br.com.sicredi.votacao_api.sessao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Dados da sessão de votação")
public record SessaoVotacaoResponse(
        @Schema(
                description = "Identificador da sessão",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Identificador da pauta",
                example = "1"
        )
        Long pautaId,

        @Schema(
                description = "Data e hora de início da sessão",
                example = "2026-09-23T22:30:00Z"
        )
        OffsetDateTime inicio,

        @Schema(
                description = "Data e hora de encerramento da sessão",
                example = "2026-09-23T22:35:00Z"
        )
        OffsetDateTime fim
) {
}
