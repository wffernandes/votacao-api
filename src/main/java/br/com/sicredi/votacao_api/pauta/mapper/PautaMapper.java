package br.com.sicredi.votacao_api.pauta.mapper;

import br.com.sicredi.votacao_api.pauta.dto.PautaResponse;
import br.com.sicredi.votacao_api.pauta.entity.Pauta;
import org.springframework.stereotype.Component;

@Component
public class PautaMapper {

    public PautaResponse toResponse(Pauta pauta) {
        return new PautaResponse(
                pauta.getId(),
                pauta.getTitulo(),
                pauta.getDescricao(),
                pauta.getCriadaEm()
        );
    }
}
