package br.com.sicredi.votacao_api.voto.mapper;

import br.com.sicredi.votacao_api.voto.dto.VotoResponse;
import br.com.sicredi.votacao_api.voto.entity.Voto;
import org.springframework.stereotype.Component;

@Component
public class VotoMapper {

    public VotoResponse toResponse(Voto voto) {
        return new VotoResponse(
                voto.getId(),
                voto.getSessao().getPauta().getId(),
                voto.getAssociadoId(),
                voto.getOpcao(),
                voto.getVotadoEm()
        );
    }
}
