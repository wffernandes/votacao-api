package br.com.sicredi.votacao_api.sessao.controller;

import br.com.sicredi.votacao_api.sessao.dto.AbrirSessaoRequest;
import br.com.sicredi.votacao_api.sessao.dto.SessaoVotacaoResponse;
import br.com.sicredi.votacao_api.sessao.service.SessaoVotacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/sessoes")
public class SessaoVotacaoController {

    private final SessaoVotacaoService sessaoService;

    public SessaoVotacaoController(SessaoVotacaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @PostMapping
    public ResponseEntity<SessaoVotacaoResponse> abrir(
            @PathVariable Long pautaId,
            @Valid @RequestBody AbrirSessaoRequest request) {

        SessaoVotacaoResponse sessao = sessaoService.abrir(pautaId, request);

        URI uri = URI.create("/api/v1/pautas/%d/sessoes/%d" .formatted(pautaId, sessao.id()));

        return ResponseEntity.created(uri).body(sessao);
    }
}
