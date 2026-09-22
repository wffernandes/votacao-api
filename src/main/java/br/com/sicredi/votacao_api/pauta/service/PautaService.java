package br.com.sicredi.votacao_api.pauta.service;

import br.com.sicredi.votacao_api.pauta.dto.CriarPautaRequest;
import br.com.sicredi.votacao_api.pauta.dto.PautaResponse;
import br.com.sicredi.votacao_api.pauta.entity.Pauta;
import br.com.sicredi.votacao_api.pauta.repository.PautaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class PautaService {

    private final PautaRepository pautaRepository;

    public PautaService(PautaRepository pautaRepository) {
        this.pautaRepository = pautaRepository;
    }

    @Transactional
    public PautaResponse criar(CriarPautaRequest request) {
        Pauta pauta = new Pauta(
                request.titulo().trim(),
                normalizarDescricao(request.descricao()),
                OffsetDateTime.now()
        );
        Pauta pautaSalva = pautaRepository.save(pauta);

        return toResponse(pautaSalva);
    }

    private PautaResponse toResponse(Pauta pauta) {
        return new PautaResponse(
                pauta.getId(),
                pauta.getTitulo(),
                pauta.getDescricao(),
                pauta.getCriadaEm()
        );
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }
        return descricao.trim();
    }
}
