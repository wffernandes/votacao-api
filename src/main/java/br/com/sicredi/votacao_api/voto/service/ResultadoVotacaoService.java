package br.com.sicredi.votacao_api.voto.service;

import br.com.sicredi.votacao_api.pauta.repository.PautaRepository;
import br.com.sicredi.votacao_api.resultado.mapper.ResultadoVotacaoMapper;
import br.com.sicredi.votacao_api.sessao.entity.SessaoVotacao;
import br.com.sicredi.votacao_api.sessao.exception.PautaNaoEncontradaException;
import br.com.sicredi.votacao_api.sessao.repository.SessaoVotacaoRepository;
import br.com.sicredi.votacao_api.voto.dto.ResultadoVotacaoResponse;
import br.com.sicredi.votacao_api.voto.entity.OpcaoVoto;
import br.com.sicredi.votacao_api.voto.entity.ResultadoVotacao;
import br.com.sicredi.votacao_api.voto.exception.ResultadoAindaIndisponivelException;
import br.com.sicredi.votacao_api.voto.exception.SessaoNaoEncontradaException;
import br.com.sicredi.votacao_api.voto.repository.VotoRepository;
import br.com.sicredi.votacao_api.voto.repository.projection.ContagemVotosProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
public class ResultadoVotacaoService {

    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoRepository;
    private final VotoRepository votoRepository;
    private final Clock clock;
    private final ResultadoVotacaoMapper resultadoVotacaoMapper;

    public ResultadoVotacaoService(PautaRepository pautaRepository, SessaoVotacaoRepository sessaoRepository,
                                   VotoRepository votoRepository, Clock clock, ResultadoVotacaoMapper resultadoVotacaoMapper) {
        this.pautaRepository = pautaRepository;
        this.sessaoRepository = sessaoRepository;
        this.votoRepository = votoRepository;
        this.clock = clock;
        this.resultadoVotacaoMapper = resultadoVotacaoMapper;
    }

    @Transactional(readOnly = true)
    public ResultadoVotacaoResponse consultar(Long pautaId) {

        if (!pautaRepository.existsById(pautaId)) {
            throw new PautaNaoEncontradaException(pautaId);
        }

        SessaoVotacao sessao =
                sessaoRepository.findByPautaId(pautaId)
                        .orElseThrow(() ->
                                new SessaoNaoEncontradaException(pautaId)
                        );

        OffsetDateTime agora = OffsetDateTime.now(clock);

        if (!sessao.estaEncerradaEm(agora)) {
            throw new ResultadoAindaIndisponivelException(pautaId);
        }

        ContagemVotosProjection contagem =
                votoRepository.contarVotosPorSessao(
                        sessao.getId(),
                        OpcaoVoto.SIM,
                        OpcaoVoto.NAO
                );

        long totalSim = contagem.getTotalSim() == null ? 0L : contagem.getTotalSim();
        long totalNao = contagem.getTotalNao() == null? 0L : contagem.getTotalNao();
        long totalVotos = totalSim + totalNao;

        ResultadoVotacao resultado = determinarResultado(totalSim, totalNao);

        return resultadoVotacaoMapper.toResponse(
                pautaId,
                totalVotos,
                totalSim,
                totalNao,
                resultado
        );
    }

    private ResultadoVotacao determinarResultado(
            long totalSim,
            long totalNao) {

        if (totalSim > totalNao) {
            return ResultadoVotacao.APROVADA;
        }

        if (totalNao > totalSim) {
            return ResultadoVotacao.REJEITADA;
        }

        return ResultadoVotacao.EMPATE;
    }
}
