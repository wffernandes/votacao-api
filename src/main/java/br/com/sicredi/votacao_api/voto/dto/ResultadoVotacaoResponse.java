package br.com.sicredi.votacao_api.voto.dto;

import br.com.sicredi.votacao_api.voto.entity.ResultadoVotacao;

public record ResultadoVotacaoResponse(
        Long pautaId,
        long totalVotos,
        long totalSim,
        long totalNao,
        ResultadoVotacao resultado
) {
}
