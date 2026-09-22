package br.com.sicredi.votacao_api.pauta.dto;

import java.time.OffsetDateTime;

public record PautaResponse(
        Long id,
        String titulo,
        String descricao,
        OffsetDateTime criadaEm
) {
}
