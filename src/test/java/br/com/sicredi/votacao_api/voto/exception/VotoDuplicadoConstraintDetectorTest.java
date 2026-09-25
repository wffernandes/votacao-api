package br.com.sicredi.votacao_api.voto.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VotoDuplicadoConstraintDetectorTest {

    private final VotoDuplicadoConstraintDetector detector = new VotoDuplicadoConstraintDetector();

    @Test
    void deveRetornarFalseQuandoNaoEncontrarConstraintViolationException() {

        RuntimeException causa = new RuntimeException("erro interno");

        RuntimeException exception = new RuntimeException("erro externo", causa);

        boolean resultado = detector.isVotoDuplicado(exception);

        assertThat(resultado).isFalse();
    }
}
