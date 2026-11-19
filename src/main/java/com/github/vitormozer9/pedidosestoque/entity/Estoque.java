package com.github.vitormozer9.pedidosestoque.entity;

import jakarta.persistence.*;
import com.github.vitormozer9.pedidosestoque.exception.ConflitoDeNegocioException;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_estoque_produto", columnNames = "produto_id"))
public class Estoque {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "produto_id", nullable = false, updatable = false)
    private Long produtoId;

    @Column(nullable = false)
    private int quantidadeDisponivel;

    @Version
    private long versao;

    protected Estoque() {}

    public Estoque(Long produtoId, int quantidadeInicial) {
        if (quantidadeInicial < 0) throw new IllegalArgumentException("Quantidade inicial negativa.");
        this.produtoId = produtoId;
        this.quantidadeDisponivel = quantidadeInicial;
    }

    public void movimentar(TipoMovimentacao tipo, int quantidade) {
        if (quantidade <= 0) throw new IllegalArgumentException("A quantidade deve ser positiva.");
        switch (tipo) {
            case ENTRADA -> {
                if (quantidadeDisponivel > Integer.MAX_VALUE - quantidade) {
                    throw new ConflitoDeNegocioException("Saldo excede o limite suportado.");
                }
                quantidadeDisponivel += quantidade;
            }
            case SAIDA -> {
                if (quantidade > quantidadeDisponivel) {
                    throw new ConflitoDeNegocioException("Estoque insuficiente.");
                }
                quantidadeDisponivel -= quantidade;
            }
        }
    }

    public Long getProdutoId() { return produtoId; }
    public int getQuantidadeDisponivel() { return quantidadeDisponivel; }
}
