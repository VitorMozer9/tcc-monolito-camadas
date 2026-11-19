package com.github.vitormozer9.pedidosestoque.dto;

import java.math.BigDecimal;
import com.github.vitormozer9.pedidosestoque.entity.Produto;

public record ProdutoResponse(Long id, String nome, String descricao, BigDecimal preco, boolean ativo) {
    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(produto.getId(), produto.getNome(), produto.getDescricao(),
                produto.getPreco(), produto.isAtivo());
    }
}
