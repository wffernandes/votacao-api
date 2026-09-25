package br.com.sicredi.votacao_api.voto.service;

import br.com.sicredi.votacao_api.integracao.elegibilidade.service.ElegibilidadeAssociadoService;
import br.com.sicredi.votacao_api.pauta.repository.PautaRepository;
import br.com.sicredi.votacao_api.sessao.entity.SessaoVotacao;
import br.com.sicredi.votacao_api.sessao.exception.PautaNaoEncontradaException;
import br.com.sicredi.votacao_api.sessao.repository.SessaoVotacaoRepository;
import br.com.sicredi.votacao_api.voto.dto.RegistrarVotoRequest;
import br.com.sicredi.votacao_api.voto.dto.VotoResponse;
import br.com.sicredi.votacao_api.voto.entity.Voto;
import br.com.sicredi.votacao_api.voto.exception.AssociadoJaVotouException;
import br.com.sicredi.votacao_api.voto.exception.SessaoFechadaException;
import br.com.sicredi.votacao_api.voto.exception.SessaoNaoEncontradaException;
import br.com.sicredi.votacao_api.voto.exception.VotoDuplicadoConstraintDetector;
import br.com.sicredi.votacao_api.voto.mapper.VotoMapper;
import br.com.sicredi.votacao_api.voto.repository.VotoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
public class VotoService {

    private final PautaRepository pautaRepository;
    private final SessaoVotacaoRepository sessaoRepository;
    private final VotoRepository votoRepository;
    private final VotoMapper votoMapper;
    private final Clock clock;
    private final VotoDuplicadoConstraintDetector constraintDetector;
    private final ElegibilidadeAssociadoService elegibilidadeService;

    public VotoService(PautaRepository pautaRepository,
                       SessaoVotacaoRepository sessaoRepository,
                       VotoRepository votoRepository,
                       VotoMapper votoMapper,
                       Clock clock,
                       VotoDuplicadoConstraintDetector constraintDetector,
                       ElegibilidadeAssociadoService elegibilidadeService) {
        this.pautaRepository = pautaRepository;
        this.sessaoRepository = sessaoRepository;
        this.votoRepository = votoRepository;
        this.votoMapper = votoMapper;
        this.clock = clock;
        this.constraintDetector = constraintDetector;
        this.elegibilidadeService = elegibilidadeService;
    }

    @Transactional
    public VotoResponse registrar(Long pautaId, RegistrarVotoRequest request) {

        // 1. Verifica se a pauta existe.
        if (!pautaRepository.existsById(pautaId)) {
            throw new PautaNaoEncontradaException(pautaId);
        }

        // 2. Recupera a sessão vinculada à pauta.
        SessaoVotacao sessao =
                sessaoRepository.findByPautaId(pautaId)
                        .orElseThrow(() ->
                                new SessaoNaoEncontradaException(pautaId));

        // 3. Verifica se a sessão está aberta.
        OffsetDateTime agora = OffsetDateTime.now(clock);

        if (!sessao.estaAbertaEm(agora)) {
            throw new SessaoFechadaException(pautaId);
        }

        // 4. Normaliza o identificador do associado.
        String associadoId = request.associadoId().trim();

        // 5. Verifica se o associado já votou.
        if (votoRepository.existsBySessaoIdAndAssociadoId(sessao.getId(),associadoId)) {
            throw new AssociadoJaVotouException(pautaId, associadoId);
        }

        // 6. Consultar elegibilidade do associado
        elegibilidadeService.validar(associadoId);

        // 7. Cria a entidade Voto.
        Voto voto = new Voto(sessao, associadoId, request.opcao(), agora);

        // 8. Persiste e trata possíveis votos concorrentes.
        try {
            Voto votoSalvo = votoRepository.saveAndFlush(voto);

            return votoMapper.toResponse(votoSalvo);
        } catch (DataIntegrityViolationException exception) {
            if (constraintDetector.isVotoDuplicado(exception)) {
                throw new AssociadoJaVotouException(pautaId, associadoId);
            }
            throw exception;
        }
    }
}
