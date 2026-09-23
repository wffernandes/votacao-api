package br.com.sicredi.votacao_api.integracao;

import br.com.sicredi.votacao_api.integracao.elegibilidade.exception.service.ElegibilidadeAssociadoService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class AbstractVotacaoIntegracaoTest extends AbstractIntegracaoTest {

    @MockitoBean
    protected ElegibilidadeAssociadoService elegibilidadeService;
}
