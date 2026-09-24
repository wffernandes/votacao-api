package br.com.sicredi.votacao_api.voto.dto;

import br.com.sicredi.votacao_api.voto.entity.OpcaoVoto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Dados do voto registrado")
public record VotoResponse(
        @Schema(
                description = "Identificador do voto",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Identificador da pauta",
                example = "1"
        )
        Long pautaId,

        @Schema(
                description = "Identificador do associado",
                example = "69037798098"
        )
        String associadoId,

        @Schema(
                description = "Opção registrada",
                example = "SIM",
                allowableValues = {"SIM", "NAO"}
        )
        OpcaoVoto opcao,

        @Schema(
                description = "Data e hora em que o voto foi registrado",
                example = "2026-09-23T22:32:15Z"
        )
        OffsetDateTime votadoEm
) {
}
