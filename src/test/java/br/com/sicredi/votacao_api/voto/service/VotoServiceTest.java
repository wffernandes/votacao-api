package br.com.sicredi.votacao_api.voto.service;

import br.com.sicredi.votacao_api.integracao.elegibilidade.service.ElegibilidadeAssociadoService;
import br.com.sicredi.votacao_api.pauta.repository.PautaRepository;
import br.com.sicredi.votacao_api.sessao.entity.SessaoVotacao;
import br.com.sicredi.votacao_api.sessao.exception.PautaNaoEncontradaException;
import br.com.sicredi.votacao_api.sessao.repository.SessaoVotacaoRepository;
import br.com.sicredi.votacao_api.voto.dto.RegistrarVotoRequest;
import br.com.sicredi.votacao_api.voto.entity.OpcaoVoto;
import br.com.sicredi.votacao_api.voto.entity.Voto;
import br.com.sicredi.votacao_api.voto.exception.AssociadoJaVotouException;
import br.com.sicredi.votacao_api.voto.exception.SessaoFechadaException;
import br.com.sicredi.votacao_api.voto.exception.SessaoNaoEncontradaException;
import br.com.sicredi.votacao_api.voto.exception.VotoDuplicadoConstraintDetector;
import br.com.sicredi.votacao_api.voto.mapper.VotoMapper;
import br.com.sicredi.votacao_api.voto.repository.VotoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotoServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoVotacaoRepository sessaoRepository;

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private SessaoVotacao sessao;

    @Mock
    private VotoMapper votoMapper;

    @Mock
    private VotoDuplicadoConstraintDetector constraintDetector;

    @Mock
    ElegibilidadeAssociadoService elegibilidadeService;

    private VotoService votoService;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-09-22T15:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void setUp() {

        votoService = new VotoService(
                pautaRepository,
                sessaoRepository,
                votoRepository,
                votoMapper,
                clock,
                constraintDetector,
                elegibilidadeService
        );
    }

    @Test
    void deveRegistrarVotoSim() {

        when(pautaRepository.existsById(1L)).thenReturn(true);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(sessao.getId()).thenReturn(10L);
        when(sessao.estaAbertaEm(any(OffsetDateTime.class))).thenReturn(true);
        when(votoRepository.existsBySessaoIdAndAssociadoId(10L,"ASSOC-001")).thenReturn(false);
        when(votoRepository.saveAndFlush(any(Voto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistrarVotoRequest request = new RegistrarVotoRequest("ASSOC-001", OpcaoVoto.SIM);

        votoService.registrar(1L, request);

        ArgumentCaptor<Voto> argumentCaptor = ArgumentCaptor.forClass(Voto.class);

        verify(votoRepository).saveAndFlush(argumentCaptor.capture());

        Voto voto = argumentCaptor.getValue();

        assertThat(voto.getAssociadoId()).isEqualTo("ASSOC-001");
        assertThat(voto.getOpcao()).isEqualTo(OpcaoVoto.SIM);
        assertThat(voto.getVotadoEm()).isEqualTo(OffsetDateTime.parse("2026-09-22T15:00:00Z"));
    }

    @Test
    void deveRejeitarAssociadoQueJaVotou() {

        when(pautaRepository.existsById(1L)).thenReturn(true);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(sessao.getId()).thenReturn(10L);
        when(sessao.estaAbertaEm(any())).thenReturn(true);
        when(votoRepository.existsBySessaoIdAndAssociadoId(10L,"ASSOC-001")).thenReturn(true);

        RegistrarVotoRequest request = new RegistrarVotoRequest("ASSOC-001", OpcaoVoto.SIM);

        assertThatThrownBy(() -> votoService.registrar(1L,request))
                .isInstanceOf(AssociadoJaVotouException.class);

        verify(votoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarVotoQuandoSessaoEstaFechada() {

        when(pautaRepository.existsById(1L)).thenReturn(true);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(sessao.estaAbertaEm(any())).thenReturn(false);

        RegistrarVotoRequest request = new RegistrarVotoRequest("ASSOC-001", OpcaoVoto.SIM);

        assertThatThrownBy(() -> votoService.registrar(1L, request))
                .isInstanceOf(SessaoFechadaException.class);

        verify(votoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarVotoQuandoPautaNaoExiste() {

        when(pautaRepository.existsById(999L)).thenReturn(false);

        RegistrarVotoRequest request = new RegistrarVotoRequest("ASSOC-001", OpcaoVoto.SIM);

        assertThatThrownBy(() -> votoService.registrar(999L,request)        )
                .isInstanceOf(PautaNaoEncontradaException.class);

        verifyNoInteractions(sessaoRepository);
        verify(votoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarVotoQuandoSessaoNaoExiste() {

        when(pautaRepository.existsById(1L)).thenReturn(true);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.empty());

        RegistrarVotoRequest request = new RegistrarVotoRequest("ASSOC-001", OpcaoVoto.SIM);

        assertThatThrownBy(() -> votoService.registrar(1L,request))
                .isInstanceOf(SessaoNaoEncontradaException.class);

        verify(votoRepository, never()).save(any());
    }

    @Test
    void deveConverterViolacaoDeVotoDuplicadoEmExcecaoDeNegocio() {

        when(pautaRepository.existsById(1L)).thenReturn(true);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(sessao.getId()).thenReturn(10L);
        when(sessao.estaAbertaEm(any())).thenReturn(true);
        when(votoRepository.existsBySessaoIdAndAssociadoId(10L, "ASSOC-001")).thenReturn(false);

        DataIntegrityViolationException erro =
                new DataIntegrityViolationException(
                        "Violação de constraint"
                );

        when(votoRepository.saveAndFlush(any(Voto.class))).thenThrow(erro);
        when(constraintDetector.isVotoDuplicado(erro)).thenReturn(true);

        RegistrarVotoRequest request = new RegistrarVotoRequest("ASSOC-001", OpcaoVoto.SIM);

        assertThatThrownBy(() -> votoService.registrar(1L, request))
                .isInstanceOf(AssociadoJaVotouException.class);
    }

    @Test
    void devePropagarDataIntegrityViolationExceptionQuandoNaoForVotoDuplicado() {

        when(pautaRepository.existsById(1L)).thenReturn(true);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(sessao.getId()).thenReturn(10L);
        when(sessao.estaAbertaEm(any(OffsetDateTime.class))).thenReturn(true);
        when(votoRepository.existsBySessaoIdAndAssociadoId(10L, "ASSOC-001")).thenReturn(false);

        DataIntegrityViolationException erro = new DataIntegrityViolationException("erro de integridade");

        when(votoRepository.saveAndFlush(any(Voto.class))).thenThrow(erro);
        when(constraintDetector.isVotoDuplicado(erro)).thenReturn(false);

        RegistrarVotoRequest request = new RegistrarVotoRequest("ASSOC-001", OpcaoVoto.SIM);

        assertThatThrownBy(() -> votoService.registrar(1L, request)) .isSameAs(erro);

        verify(constraintDetector).isVotoDuplicado(erro);
    }
}
