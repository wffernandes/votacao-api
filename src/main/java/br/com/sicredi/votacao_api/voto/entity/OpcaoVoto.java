package br.com.sicredi.votacao_api.voto.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "Opção de voto",
        allowableValues = {"SIM", "NAO"}
)
public enum OpcaoVoto {
    SIM, NAO
}
