package br.com.sicredi.votacao_api.voto.repository;

import br.com.sicredi.votacao_api.voto.entity.Voto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    boolean existsBySessaoIdAndAssociadoId(Long sessaoId,String associadoId);
}
