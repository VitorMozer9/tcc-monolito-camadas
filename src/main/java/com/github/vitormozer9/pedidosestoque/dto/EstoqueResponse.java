package com.github.vitormozer9.pedidosestoque.dto;

import com.github.vitormozer9.pedidosestoque.entity.Estoque;

public record EstoqueResponse(Long produtoId, int quantidadeDisponivel) {
    public static EstoqueResponse de(Estoque estoque) {
        return new EstoqueResponse(estoque.getProdutoId(), estoque.getQuantidadeDisponivel());
    }
}
