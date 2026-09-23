package br.com.sicredi.votacao_api.sessao.entity;

import br.com.sicredi.votacao_api.pauta.entity.Pauta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SessaoVotacaoTest {
    private final OffsetDateTime inicio =
            OffsetDateTime.parse("2026-09-22T10:00:00Z");

    private final OffsetDateTime fim =
            OffsetDateTime.parse("2026-09-22T10:01:00Z");

    private SessaoVotacao sessao;

    @BeforeEach
    void setUp() {
        Pauta pauta = new Pauta("Pauta",null,OffsetDateTime.parse("2026-09-22T09:00:00Z"));
        sessao = new SessaoVotacao(pauta,inicio,fim);
    }

    @Test
    void deveEstarAbertaExatamenteNoInicio() {
        assertThat(sessao.estaAbertaEm(inicio)).isTrue();
    }

    @Test
    void deveEstarAbertaAntesDoFim() {
        assertThat(sessao.estaAbertaEm(fim.minusNanos(1))).isTrue();
    }

    @Test
    void naoDeveEstarAbertaExatamenteNoFim() {
        assertThat(sessao.estaAbertaEm(fim)).isFalse();
    }

    @Test
    void deveEstarEncerradaExatamenteNoFim() {
        assertThat(sessao.estaEncerradaEm(fim)).isTrue();
    }
}
