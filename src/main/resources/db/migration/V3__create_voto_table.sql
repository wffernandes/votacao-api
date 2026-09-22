CREATE TABLE voto (
    id BIGSERIAL PRIMARY KEY,
    sessao_id BIGINT NOT NULL,
    associado_id VARCHAR(100) NOT NULL,
    opcao VARCHAR(3) NOT NULL,
    votado_em TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_voto_sessao
        FOREIGN KEY (sessao_id)
        REFERENCES sessao_votacao(id),

    CONSTRAINT uk_voto_sessao_associado
        UNIQUE (sessao_id, associado_id),

    CONSTRAINT ck_voto_opcao
        CHECK (opcao IN ('SIM', 'NAO'))
);