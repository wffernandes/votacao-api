package br.com.sicredi.votacao_api.pauta.service;

import br.com.sicredi.votacao_api.pauta.dto.CriarPautaRequest;
import br.com.sicredi.votacao_api.pauta.dto.PautaResponse;
import br.com.sicredi.votacao_api.pauta.entity.Pauta;
import br.com.sicredi.votacao_api.pauta.mapper.PautaMapper;
import br.com.sicredi.votacao_api.pauta.repository.PautaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest {

    @Mock
    private PautaRepository pautaRepository;

    private PautaService pautaService;

    private final PautaMapper pautaMapper = new PautaMapper();

    private final Clock clock = Clock.fixed(
            Instant.parse("2026-09-22T15:00:00Z"),
            ZoneOffset.UTC
    );

    @BeforeEach
    void setUp() {
        pautaService = new PautaService(pautaRepository,pautaMapper,clock);
    }

    @Test
    void deveCriarPauta() {

        CriarPautaRequest request = new CriarPautaRequest(
                "  Aprovação do orçamento  ",
                "  Orçamento anual  "
        );

        when(pautaRepository.save(any(Pauta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PautaResponse response = pautaService.criar(request);

        ArgumentCaptor<Pauta> argumentCaptor = ArgumentCaptor.forClass(Pauta.class);

        verify(pautaRepository).save(argumentCaptor.capture());

        Pauta pautaSalva = argumentCaptor.getValue();

        assertThat(pautaSalva.getTitulo()).isEqualTo("Aprovação do orçamento");
        assertThat(pautaSalva.getDescricao()).isEqualTo("Orçamento anual");
        assertThat(pautaSalva.getCriadaEm()).isEqualTo("2026-09-22T15:00:00Z");
        assertThat(response.titulo()).isEqualTo("Aprovação do orçamento");
    }

    @Test
    void deveCriarPautaComDescricaoNula() {

        CriarPautaRequest request = new CriarPautaRequest(
                "Nova pauta",
                null
        );

        when(pautaRepository.save(any(Pauta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        pautaService.criar(request);

        ArgumentCaptor<Pauta> argumentCaptor = ArgumentCaptor.forClass(Pauta.class);

        verify(pautaRepository).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getDescricao()).isNull();
    }

    @Test
    void deveNormalizarDescricaoEmBrancoParaNull() {

        CriarPautaRequest request = new CriarPautaRequest(
                "Nova pauta",
                "  "
        );

        when(pautaRepository.save(any(Pauta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        pautaService.criar(request);

        ArgumentCaptor<Pauta> argumentCaptor = ArgumentCaptor.forClass(Pauta.class);

        verify(pautaRepository).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getDescricao()).isNull();
    }
}
