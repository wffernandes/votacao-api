package br.com.sicredi.votacao_api.voto.controller;

import br.com.sicredi.votacao_api.voto.dto.ResultadoVotacaoResponse;
import br.com.sicredi.votacao_api.voto.service.ResultadoVotacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/resultado")
public class ResultadoVotacaoController {

    private final ResultadoVotacaoService resultadoService;

    public ResultadoVotacaoController(ResultadoVotacaoService resultadoService) {
        this.resultadoService = resultadoService;
    }

    @GetMapping
    public ResponseEntity<ResultadoVotacaoResponse> consultar(
            @PathVariable Long pautaId) {

        return ResponseEntity.ok(resultadoService.consultar(pautaId));
    }
}
