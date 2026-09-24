package br.com.sicredi.votacao_api.voto.dto;

import br.com.sicredi.votacao_api.voto.entity.ResultadoVotacao;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado final da votação de uma pauta")
public record ResultadoVotacaoResponse(
        @Schema(
                description = "Identificador da pauta",
                example = "1"
        )
        Long pautaId,

        @Schema(
                description = "Quantidade total de votos",
                example = "100"
        )
        long totalVotos,

        @Schema(
                description = "Quantidade de votos SIM",
                example = "60"
        )
        long totalSim,

        @Schema(
                description = "Quantidade de votos NAO",
                example = "40"
        )
        long totalNao,

        @Schema(
                description = "Resultado final da votação",
                example = "APROVADA",
                allowableValues = {
                        "APROVADA",
                        "REJEITADA",
                        "EMPATE"
                }
        )
        ResultadoVotacao resultado
) {
}
