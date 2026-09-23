package br.com.sicredi.votacao_api.voto.service;

import br.com.sicredi.votacao_api.pauta.repository.PautaRepository;
import br.com.sicredi.votacao_api.sessao.entity.SessaoVotacao;
import br.com.sicredi.votacao_api.sessao.repository.SessaoVotacaoRepository;
import br.com.sicredi.votacao_api.voto.dto.ResultadoVotacaoResponse;
import br.com.sicredi.votacao_api.voto.entity.OpcaoVoto;
import br.com.sicredi.votacao_api.voto.entity.ResultadoVotacao;
import br.com.sicredi.votacao_api.voto.exception.ResultadoAindaIndisponivelException;
import br.com.sicredi.votacao_api.voto.repository.VotoRepository;
import br.com.sicredi.votacao_api.voto.repository.projection.ContagemVotosProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultadoVotacaoServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    @Mock
    private SessaoVotacaoRepository sessaoRepository;

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private SessaoVotacao sessao;

    @Mock
    private ContagemVotosProjection contagem;

    private ResultadoVotacaoService service;

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-09-22T15:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void setUp() {

        service = new ResultadoVotacaoService(
                pautaRepository,
                sessaoRepository,
                votoRepository,
                clock
        );
    }

    @Test
    void deveRetornarResultadoAprovado() {

        when(pautaRepository.existsById(1L)).thenReturn(true);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(sessao.getId()).thenReturn(10L);
        when(sessao.estaEncerradaEm(any())).thenReturn(true);
        when(votoRepository.contarVotosPorSessao(10L, OpcaoVoto.SIM, OpcaoVoto.NAO )).thenReturn(contagem);
        when(contagem.getTotalSim()).thenReturn(10L);
        when(contagem.getTotalNao()).thenReturn(5L);

        ResultadoVotacaoResponse response = service.consultar(1L);

        assertThat(response.totalVotos()).isEqualTo(15);
        assertThat(response.totalSim()).isEqualTo(10);
        assertThat(response.totalNao()).isEqualTo(5);
        assertThat(response.resultado()).isEqualTo(ResultadoVotacao.APROVADA);
    }

    @Test
    void deveRetornarResultadoRejeitado() {

        when(pautaRepository.existsById(1L)).thenReturn(true);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(sessao.getId()).thenReturn(10L);
        when(sessao.estaEncerradaEm(any())).thenReturn(true);
        when(votoRepository.contarVotosPorSessao(10L, OpcaoVoto.SIM, OpcaoVoto.NAO )).thenReturn(contagem);
        when(contagem.getTotalSim()).thenReturn(4L);
        when(contagem.getTotalNao()).thenReturn(7L);

        ResultadoVotacaoResponse response = service.consultar(1L);

        assertThat(response.totalVotos()).isEqualTo(11);
        assertThat(response.totalSim()).isEqualTo(4);
        assertThat(response.totalNao()).isEqualTo(7);
        assertThat(response.resultado()).isEqualTo(ResultadoVotacao.REJEITADA);
    }

    @Test
    void deveRetornarResultadoEmpate() {

        when(pautaRepository.existsById(1L)).thenReturn(true);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(sessao.getId()).thenReturn(10L);
        when(sessao.estaEncerradaEm(any())).thenReturn(true);
        when(votoRepository.contarVotosPorSessao(10L, OpcaoVoto.SIM, OpcaoVoto.NAO )).thenReturn(contagem);
        when(contagem.getTotalSim()).thenReturn(5L);
        when(contagem.getTotalNao()).thenReturn(5L);

        ResultadoVotacaoResponse response = service.consultar(1L);

        assertThat(response.totalVotos()).isEqualTo(10);
        assertThat(response.totalSim()).isEqualTo(5);
        assertThat(response.totalNao()).isEqualTo(5);
        assertThat(response.resultado()).isEqualTo(ResultadoVotacao.EMPATE);
    }

    @Test
    void deveRetornarResultadoSemVotos() {

        when(pautaRepository.existsById(1L)).thenReturn(true);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(sessao.getId()).thenReturn(10L);
        when(sessao.estaEncerradaEm(any())).thenReturn(true);
        when(votoRepository.contarVotosPorSessao(10L, OpcaoVoto.SIM, OpcaoVoto.NAO )).thenReturn(contagem);
        when(contagem.getTotalSim()).thenReturn(null);
        when(contagem.getTotalNao()).thenReturn(null);

        ResultadoVotacaoResponse response = service.consultar(1L);

        assertThat(response.totalVotos()).isZero();
        assertThat(response.totalSim()).isZero();
        assertThat(response.totalNao()).isZero();
        assertThat(response.resultado()).isEqualTo(ResultadoVotacao.EMPATE);
    }

    @Test
    void deveRejeitarConsultaAntesDoEncerramento() {

        when(pautaRepository.existsById(1L)).thenReturn(true);
        when(sessaoRepository.findByPautaId(1L)).thenReturn(Optional.of(sessao));
        when(sessao.estaEncerradaEm(any())).thenReturn(false);

        assertThatThrownBy(() -> service.consultar(1L))
                .isInstanceOf(ResultadoAindaIndisponivelException.class);

        verifyNoInteractions(votoRepository);
    }
}
