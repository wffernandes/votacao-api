package br.com.sicredi.votacao_api.integracao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VotoIntegracaoTest extends AbstractVotacaoIntegracaoTest  {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveRegistrarVotosSimENao() throws Exception {

        Long pautaId = criarPauta("Pauta para registrar votos");

        abrirSessao(pautaId, 5);

        var votoSim = registrarVoto(
                pautaId,
                "ASSOC-001",
                "SIM"
        );

        assertThat(votoSim.getResponse().getStatus())
                .isEqualTo(201);

        var votoNao = registrarVoto(
                pautaId,
                "ASSOC-002",
                "NAO"
        );

        assertThat(votoNao.getResponse().getStatus())
                .isEqualTo(201);
    }

    @Test
    void naoDevePermitirVotoDuplicado() throws Exception {

        Long pautaId = criarPauta("Pauta para voto duplicado");

        abrirSessao(pautaId, 5);

        registrarVoto(
                pautaId,
                "ASSOC-001",
                "SIM"
        );

        var segundoVoto = registrarVoto(
                pautaId,
                "ASSOC-001",
                "NAO"
        );

        assertThat(segundoVoto.getResponse().getStatus())
                .isEqualTo(409);
    }

    @Test
    void deveImpedirVotoDuplicadoPelaConstraintDoBanco()
            throws Exception {

        Long pautaId = criarPauta(
                "Pauta para testar constraint"
        );

        Long sessaoId = abrirSessao(pautaId, 5);

        jdbcTemplate.update("""
            INSERT INTO voto (
                sessao_id,
                associado_id,
                opcao,
                votado_em
            )
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            """,
                sessaoId,
                "ASSOC-CONSTRAINT",
                "SIM"
        );

        assertThatThrownBy(() ->
                jdbcTemplate.update("""
                    INSERT INTO voto (
                        sessao_id,
                        associado_id,
                        opcao,
                        votado_em
                    )
                    VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                    """,
                        sessaoId,
                        "ASSOC-CONSTRAINT",
                        "NAO"
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
