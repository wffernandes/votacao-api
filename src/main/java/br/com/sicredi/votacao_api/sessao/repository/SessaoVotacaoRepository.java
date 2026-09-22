package br.com.sicredi.votacao_api.sessao.repository;

import br.com.sicredi.votacao_api.sessao.entity.SessaoVotacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessaoVotacaoRepository extends JpaRepository<SessaoVotacao, Long> {

    boolean existsByPautaId(Long pautaId);

    Optional<SessaoVotacao> findByPautaId(Long pautaId);
}
