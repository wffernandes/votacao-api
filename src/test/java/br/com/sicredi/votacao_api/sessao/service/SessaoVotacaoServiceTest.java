package br.com.sicredi.votacao_api.sessao.service;

import br.com.sicredi.votacao_api.pauta.entity.Pauta;
import br.com.sicredi.votacao_api.pauta.repository.PautaRepository;
import br.com.sicredi.votacao_api.sessao.dto.AbrirSessaoRequest;
import br.com.sicredi.votacao_api.sessao.entity.SessaoVotacao;
import br.com.sicredi.votacao_api.sessao.exception.PautaNaoEncontradaException;
import br.com.sicredi.votacao_api.sessao.exception.SessaoJaExistenteException;
import br.com.sicredi.votacao_api.sessao.mapper.SessaoVotacaoMapper;
import br.com.sicredi.votacao_api.sessao.repository.SessaoVotacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class SessaoVotacaoServiceTest {

    @Mock
    private SessaoVotacaoRepository sessaoRepository;

    @Mock
    private PautaRepository pautaRepository;

    private SessaoVotacaoService sessaoService;

    private final SessaoVotacaoMapper mapper = new SessaoVotacaoMapper();

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-09-22T15:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void setUp() {
        sessaoService = new SessaoVotacaoService(sessaoRepository, pautaRepository, mapper, clock);
    }

    @Test
    void deveAbrirSessaoComDuracaoPadraoDeUmMinuto() {

        Pauta pauta = new Pauta("Pauta", null, OffsetDateTime.parse("2026-09-22T14:00:00Z"));

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.existsByPautaId(1L)).thenReturn(false);
        when(sessaoRepository.save(any(SessaoVotacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AbrirSessaoRequest request = new AbrirSessaoRequest(null);

        sessaoService.abrir(1L, request);

        ArgumentCaptor<SessaoVotacao> argumentCaptor = ArgumentCaptor.forClass(SessaoVotacao.class);

        verify(sessaoRepository).save(argumentCaptor.capture());

        SessaoVotacao sessao = argumentCaptor.getValue();

        assertThat(sessao.getInicio()).isEqualTo("2026-09-22T15:00:00Z");
        assertThat(sessao.getFim()).isEqualTo("2026-09-22T15:01:00Z");
    }

    @Test
    void deveAbrirSessaoComDuracaoInformada() {

        Pauta pauta = new Pauta("Pauta", null, OffsetDateTime.parse("2026-09-22T14:00:00Z"));

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.existsByPautaId(1L)).thenReturn(false);
        when(sessaoRepository.save(any(SessaoVotacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        sessaoService.abrir(1L, new AbrirSessaoRequest(5));

        ArgumentCaptor<SessaoVotacao> argumentCaptor = ArgumentCaptor.forClass(SessaoVotacao.class);

        verify(sessaoRepository).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getFim()).isEqualTo("2026-09-22T15:05:00Z");
    }

    @Test
    void deveRejeitarAberturaQuandoPautaNaoExiste() {

        when(pautaRepository.findById(999L)).thenReturn(Optional.empty());

        AbrirSessaoRequest request = new AbrirSessaoRequest(5);

        assertThatThrownBy(() -> sessaoService.abrir(999L, request))
                .isInstanceOf(PautaNaoEncontradaException.class)
                .hasMessage("Pauta não encontrada: 999");

        verify(sessaoRepository, never()).existsByPautaId(any());
        verify(sessaoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarAberturaQuandoSessaoJaExiste() {

        Pauta pauta = new Pauta("Pauta", null, OffsetDateTime.parse("2026-09-22T14:00:00Z"));

        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(sessaoRepository.existsByPautaId(1L)).thenReturn(true);

        AbrirSessaoRequest request = new AbrirSessaoRequest(5);

        assertThatThrownBy(() -> sessaoService.abrir(1L,request))
                .isInstanceOf(SessaoJaExistenteException.class);

        verify(sessaoRepository, never()).save(any());
    }
}
