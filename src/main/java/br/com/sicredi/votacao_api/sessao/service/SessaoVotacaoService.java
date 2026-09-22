package br.com.sicredi.votacao_api.sessao.service;

import br.com.sicredi.votacao_api.pauta.entity.Pauta;
import br.com.sicredi.votacao_api.pauta.repository.PautaRepository;
import br.com.sicredi.votacao_api.sessao.dto.AbrirSessaoRequest;
import br.com.sicredi.votacao_api.sessao.dto.SessaoVotacaoResponse;
import br.com.sicredi.votacao_api.sessao.entity.SessaoVotacao;
import br.com.sicredi.votacao_api.sessao.exception.PautaNaoEncontradaException;
import br.com.sicredi.votacao_api.sessao.exception.SessaoJaExistenteException;
import br.com.sicredi.votacao_api.sessao.mapper.SessaoVotacaoMapper;
import br.com.sicredi.votacao_api.sessao.repository.SessaoVotacaoRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
public class SessaoVotacaoService {

    private static final int DURACAO_PADRAO_MINUTOS = 1;

    private final SessaoVotacaoRepository sessaoRepository;
    private final PautaRepository pautaRepository;
    private final SessaoVotacaoMapper sessaoMapper;
    private final Clock clock;

    public SessaoVotacaoService(SessaoVotacaoRepository sessaoRepository,
                                PautaRepository pautaRepository,
                                SessaoVotacaoMapper sessaoMapper,
                                Clock clock) {
        this.sessaoRepository = sessaoRepository;
        this.pautaRepository = pautaRepository;
        this.sessaoMapper = sessaoMapper;
        this.clock = clock;
    }

    public SessaoVotacaoResponse abrir(Long pautaId, AbrirSessaoRequest request) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() ->
                        new PautaNaoEncontradaException(pautaId));

        if (sessaoRepository.existsByPautaId(pautaId)) {
            throw new SessaoJaExistenteException(pautaId);
        }

        int duracaoMinutos = request.duracaoMinutos() == null
                ? DURACAO_PADRAO_MINUTOS
                : request.duracaoMinutos();

        OffsetDateTime inicio = OffsetDateTime.now(clock);
        OffsetDateTime fim = inicio.plusMinutes(duracaoMinutos);

        SessaoVotacao sessao = new SessaoVotacao(pauta, inicio, fim);

        SessaoVotacao sessaoSalva = sessaoRepository.save(sessao);

        return sessaoMapper.toResponse(sessaoSalva);
    }
}
