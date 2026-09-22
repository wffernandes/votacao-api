package br.com.sicredi.votacao_api.sessao.repository;

import br.com.sicredi.votacao_api.sessao.entity.SessaoVotacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessaoVotacaoRepository extends JpaRepository<SessaoVotacao, Long> {

    boolean existsByPautaId(Long pautaId);
}
