package com.github.vitormozer9.pedidosestoque.controller;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.github.vitormozer9.pedidosestoque.dto.*;
import com.github.vitormozer9.pedidosestoque.service.PedidoService;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {
    private final PedidoService pedidos;

    public PedidoController(PedidoService pedidos) {
        this.pedidos = pedidos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse criar(@Valid @RequestBody CriacaoPedidoRequest dados) {
        return pedidos.criar(dados);
    }

    @GetMapping
    public List<PedidoResponse> listar() {
        return pedidos.listar();
    }

    @GetMapping("/{pedidoId}")
    public PedidoResponse consultar(@PathVariable @Positive Long pedidoId) {
        return pedidos.consultar(pedidoId);
    }

    @PostMapping("/{pedidoId}/itens")
    public PedidoResponse adicionar(@PathVariable @Positive Long pedidoId, @Valid @RequestBody ItemPedidoRequest dados) {
        return pedidos.adicionar(pedidoId, dados);
    }

    @PutMapping("/{pedidoId}/itens/{produtoId}")
    public PedidoResponse atualizar(@PathVariable @Positive Long pedidoId, @PathVariable @Positive Long produtoId,
                                    @Valid @RequestBody AtualizacaoQuantidadeRequest dados) {
        return pedidos.atualizar(pedidoId, produtoId, dados);
    }

    @DeleteMapping("/{pedidoId}/itens/{produtoId}")
    public PedidoResponse remover(@PathVariable @Positive Long pedidoId, @PathVariable @Positive Long produtoId) {
        return pedidos.remover(pedidoId, produtoId);
    }

    @PostMapping("/{pedidoId}/cancelamento")
    public PedidoResponse cancelar(@PathVariable @Positive Long pedidoId) {
        return pedidos.cancelar(pedidoId);
    }
}
