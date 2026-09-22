package br.com.sicredi.votacao_api.voto.repository;

import br.com.sicredi.votacao_api.voto.entity.OpcaoVoto;
import br.com.sicredi.votacao_api.voto.entity.Voto;
import br.com.sicredi.votacao_api.voto.repository.projection.ContagemVotosProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    boolean existsBySessaoIdAndAssociadoId(Long sessaoId,String associadoId);

    @Query("""
            SELECT
                SUM(CASE WHEN v.opcao = :sim THEN 1 ELSE 0 END) AS totalSim,
                SUM(CASE WHEN v.opcao = :nao THEN 1 ELSE 0 END) AS totalNao
            FROM Voto v
            WHERE v.sessao.id = :sessaoId
            """)
    ContagemVotosProjection contarVotosPorSessao(
            @Param("sessaoId") Long sessaoId,
            @Param("sim") OpcaoVoto sim,
            @Param("nao") OpcaoVoto nao
    );
}
