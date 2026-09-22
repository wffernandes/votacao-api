package br.com.sicredi.votacao_api.voto.dto;

import br.com.sicredi.votacao_api.voto.entity.OpcaoVoto;

import java.time.OffsetDateTime;

public record VotoResponse(
        Long id,
        Long pautaId,
        String associadoId,
        OpcaoVoto opcao,
        OffsetDateTime votadoEm
) {
}
