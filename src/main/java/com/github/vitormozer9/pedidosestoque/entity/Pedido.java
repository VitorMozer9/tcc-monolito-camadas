package com.github.vitormozer9.pedidosestoque.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import jakarta.persistence.*;
import com.github.vitormozer9.pedidosestoque.exception.*;

@Entity
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status = StatusPedido.ABERTO;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemPedido> itens = new ArrayList<>();

    protected Pedido() {}

    public Pedido(Produto primeiroProduto, int quantidade) {
        adicionar(primeiroProduto, quantidade);
    }

    public void exigirAberto() {
        if (status == StatusPedido.CANCELADO) {
            throw new ConflitoDeNegocioException("Pedido cancelado.");
        }
    }

    public void adicionar(Produto produto, int quantidade) {
        exigirAberto();
        if (itens.stream().anyMatch(item -> item.getProdutoId().equals(produto.getId()))) {
            throw new ConflitoDeNegocioException("Produto duplicado no pedido.");
        }
        itens.add(new ItemPedido(this, produto, quantidade));
    }

    public void alterarQuantidade(Long produtoId, int quantidade) {
        exigirAberto();
        buscarItem(produtoId).alterarQuantidade(quantidade);
    }

    public void remover(Long produtoId) {
        exigirAberto();
        var item = buscarItem(produtoId);
        if (itens.size() == 1) throw new ConflitoDeNegocioException("Não é possível remover o último item.");
        itens.remove(item);
    }

    private ItemPedido buscarItem(Long produtoId) {
        return itens.stream().filter(item -> item.getProdutoId().equals(produtoId)).findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado: " + produtoId));
    }

    public void cancelar() {
        exigirAberto();
        status = StatusPedido.CANCELADO;
    }

    public BigDecimal getTotal() {
        return itens.stream().map(ItemPedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return id; }
    public StatusPedido getStatus() { return status; }
    public Instant getCriadoEm() { return criadoEm; }
    public List<ItemPedido> getItens() { return Collections.unmodifiableList(itens); }
}
