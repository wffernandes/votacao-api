package br.com.sicredi.votacao_api.resultado.mapper;

import br.com.sicredi.votacao_api.voto.dto.ResultadoVotacaoResponse;
import br.com.sicredi.votacao_api.voto.entity.ResultadoVotacao;
import org.springframework.stereotype.Component;

@Component
public class ResultadoVotacaoMapper {

    public ResultadoVotacaoResponse toResponse(
            Long pautaId,
            long totalVotos,
            long totalSim,
            long totalNao,
            ResultadoVotacao resultado) {

        return new ResultadoVotacaoResponse(
                pautaId,
                totalVotos,
                totalSim,
                totalNao,
                resultado
        );
    }
}
