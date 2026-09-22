package br.com.sicredi.votacao_api.pauta.service;

import br.com.sicredi.votacao_api.pauta.dto.CriarPautaRequest;
import br.com.sicredi.votacao_api.pauta.dto.PautaResponse;
import br.com.sicredi.votacao_api.pauta.entity.Pauta;
import br.com.sicredi.votacao_api.pauta.mapper.PautaMapper;
import br.com.sicredi.votacao_api.pauta.repository.PautaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
public class PautaService {

    private final PautaRepository pautaRepository;
    private final PautaMapper pautaMapper;
    private final Clock clock;

    public PautaService(PautaRepository pautaRepository,
                        PautaMapper pautaMapper,
                        Clock clock) {
        this.pautaRepository = pautaRepository;
        this.pautaMapper = pautaMapper;
        this.clock = clock;
    }

    @Transactional
    public PautaResponse criar(CriarPautaRequest request) {
        Pauta pauta = new Pauta(
                request.titulo().trim(),
                normalizarDescricao(request.descricao()),
                OffsetDateTime.now(clock)
        );
        Pauta pautaSalva = pautaRepository.save(pauta);

        return pautaMapper.toResponse(pautaSalva);
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }
        return descricao.trim();
    }
}
