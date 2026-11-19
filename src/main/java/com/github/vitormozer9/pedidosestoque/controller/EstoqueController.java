package com.github.vitormozer9.pedidosestoque.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.github.vitormozer9.pedidosestoque.dto.*;
import com.github.vitormozer9.pedidosestoque.service.EstoqueService;

@RestController
@RequestMapping("/api/v1/estoques")
public class EstoqueController {
    private final EstoqueService estoques;

    public EstoqueController(EstoqueService estoques) {
        this.estoques = estoques;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstoqueResponse cadastrar(@Valid @RequestBody CadastroEstoqueRequest dados) {
        return estoques.cadastrar(dados);
    }

    @GetMapping("/{produtoId}")
    public EstoqueResponse consultar(@PathVariable @Positive Long produtoId) {
        return estoques.consultar(produtoId);
    }

    @PostMapping("/{produtoId}/movimentacoes")
    public EstoqueResponse movimentar(@PathVariable @Positive Long produtoId, @Valid @RequestBody MovimentacaoEstoqueRequest dados) {
        return estoques.movimentar(produtoId, dados);
    }
}
