package br.com.sicredi.votacao_api.voto.entity;

import br.com.sicredi.votacao_api.sessao.entity.SessaoVotacao;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "voto", uniqueConstraints = {@UniqueConstraint(name = "uk_voto_sessao_associado",columnNames = {"sessao_id", "associado_id"})})
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sessao_id", nullable = false)
    private SessaoVotacao sessao;

    @Column(name = "associado_id", nullable = false, length = 100)
    private String associadoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private OpcaoVoto opcao;

    @Column(name = "votado_em", nullable = false)
    private OffsetDateTime votadoEm;

    protected Voto() {
    }

    public Voto(SessaoVotacao sessao, String associadoId, OpcaoVoto opcao, OffsetDateTime votadoEm) {
        this.sessao = sessao;
        this.associadoId = associadoId;
        this.opcao = opcao;
        this.votadoEm = votadoEm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SessaoVotacao getSessao() {
        return sessao;
    }

    public void setSessao(SessaoVotacao sessao) {
        this.sessao = sessao;
    }

    public String getAssociadoId() {
        return associadoId;
    }

    public void setAssociadoId(String associadoId) {
        this.associadoId = associadoId;
    }

    public OpcaoVoto getOpcao() {
        return opcao;
    }

    public void setOpcao(OpcaoVoto opcao) {
        this.opcao = opcao;
    }

    public OffsetDateTime getVotadoEm() {
        return votadoEm;
    }

    public void setVotadoEm(OffsetDateTime votadoEm) {
        this.votadoEm = votadoEm;
    }
}
