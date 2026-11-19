package com.github.vitormozer9.pedidosestoque.controller;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.github.vitormozer9.pedidosestoque.dto.*;
import com.github.vitormozer9.pedidosestoque.service.ProdutoService;

@RestController
@RequestMapping("/api/v1/produtos")
public class ProdutoController {
    private final ProdutoService produtos;

    public ProdutoController(ProdutoService produtos) {
        this.produtos = produtos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse cadastrar(@Valid @RequestBody ProdutoRequest dados) {
        return produtos.cadastrar(dados);
    }

    @GetMapping
    public List<ProdutoResponse> listar() {
        return produtos.listar();
    }

    @GetMapping("/{id}")
    public ProdutoResponse consultar(@PathVariable @Positive Long id) {
        return produtos.consultar(id);
    }

    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable @Positive Long id, @Valid @RequestBody ProdutoRequest dados) {
        return produtos.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable @Positive Long id) {
        produtos.desativar(id);
    }
}
