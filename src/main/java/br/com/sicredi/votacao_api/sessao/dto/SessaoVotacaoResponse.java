package br.com.sicredi.votacao_api.sessao.dto;

import java.time.OffsetDateTime;

public record SessaoVotacaoResponse(
        Long id,
        Long pautaId,
        OffsetDateTime inicio,
        OffsetDateTime fim
) {
}
