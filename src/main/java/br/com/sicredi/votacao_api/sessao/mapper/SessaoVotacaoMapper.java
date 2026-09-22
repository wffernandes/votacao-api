package br.com.sicredi.votacao_api.sessao.mapper;

import br.com.sicredi.votacao_api.sessao.dto.SessaoVotacaoResponse;
import br.com.sicredi.votacao_api.sessao.entity.SessaoVotacao;
import org.springframework.stereotype.Component;

@Component
public class SessaoVotacaoMapper {

    public SessaoVotacaoResponse toResponse(SessaoVotacao sessao) {
        return new SessaoVotacaoResponse(
                sessao.getId(),
                sessao.getPauta().getId(),
                sessao.getInicio(),
                sessao.getFim()
        );
    }
}
