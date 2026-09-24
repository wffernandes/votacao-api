package br.com.sicredi.votacao_api.pauta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Dados da pauta cadastrada")
public record PautaResponse(
        @Schema(
                description = "Identificador da pauta",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Título da pauta",
                example = "Aprovação do orçamento anual"
        )
        String titulo,

        @Schema(
                description = "Descrição da pauta",
                example = "Votação para aprovação do orçamento do próximo exercício"
        )
        String descricao,

        @Schema(
                description = "Data e hora de criação da pauta",
                example = "2026-09-23T22:30:00Z"
        )
        OffsetDateTime criadaEm
) {
}
