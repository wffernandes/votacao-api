CREATE TABLE pauta (
                       id BIGSERIAL PRIMARY KEY,
                       titulo VARCHAR(200) NOT NULL,
                       descricao VARCHAR(1000),
                       criada_em TIMESTAMP WITH TIME ZONE NOT NULL
);