package br.com.sicredi.votacao_api.pauta.repository;

import br.com.sicredi.votacao_api.pauta.entity.Pauta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PautaRepository extends JpaRepository<Pauta, Long> {
}
