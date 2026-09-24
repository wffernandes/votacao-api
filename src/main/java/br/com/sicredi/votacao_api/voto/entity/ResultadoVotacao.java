package br.com.sicredi.votacao_api.voto.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Situação final da votação",
        allowableValues = {
                "APROVADA",
                "REJEITADA",
                "EMPATE"
        }
)
public enum ResultadoVotacao {
    APROVADA,
    REJEITADA,
    EMPATE
}
