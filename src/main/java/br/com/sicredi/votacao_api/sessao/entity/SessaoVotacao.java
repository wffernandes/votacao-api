package br.com.sicredi.votacao_api.sessao.entity;

import br.com.sicredi.votacao_api.pauta.entity.Pauta;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sessao_votacao", uniqueConstraints ={@UniqueConstraint(name = "uk_sessao_votacao_pauta", columnNames = "pauta_id")})
public class SessaoVotacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pauta_id", nullable = false)
    private Pauta pauta;

    @Column(nullable = false)
    private OffsetDateTime inicio;

    @Column(nullable = false)
    private OffsetDateTime fim;

    public boolean estaAbertaEm(OffsetDateTime instante) {
        return !instante.isBefore(inicio)
                && instante.isBefore(fim);
    }

    protected SessaoVotacao() {
    }

    public SessaoVotacao(Pauta pauta, OffsetDateTime inicio, OffsetDateTime fim) {
        this.pauta = pauta;
        this.inicio = inicio;
        this.fim = fim;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public void setPauta(Pauta pauta) {
        this.pauta = pauta;
    }

    public OffsetDateTime getInicio() {
        return inicio;
    }

    public void setInicio(OffsetDateTime inicio) {
        this.inicio = inicio;
    }

    public OffsetDateTime getFim() {
        return fim;
    }

    public void setFim(OffsetDateTime fim) {
        this.fim = fim;
    }
}
