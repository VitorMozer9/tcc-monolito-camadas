package com.github.vitormozer9.pedidosestoque.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.vitormozer9.pedidosestoque.dto.*;
import com.github.vitormozer9.pedidosestoque.entity.Pedido;
import com.github.vitormozer9.pedidosestoque.repository.PedidoRepository;
import com.github.vitormozer9.pedidosestoque.exception.RecursoNaoEncontradoException;

@Service
@Transactional
public class PedidoService {
    private final PedidoRepository pedidos;
    private final ProdutoService produtos;

    public PedidoService(PedidoRepository pedidos, ProdutoService produtos) {
        this.pedidos = pedidos;
        this.produtos = produtos;
    }

    public PedidoResponse criar(CriacaoPedidoRequest dados) {
        var itens = dados.itens().iterator();
        var primeiro = itens.next();
        var pedido = new Pedido(produtos.buscarAtivo(primeiro.produtoId()), primeiro.quantidade());
        itens.forEachRemaining(item -> pedido.adicionar(produtos.buscarAtivo(item.produtoId()), item.quantidade()));
        return PedidoResponse.de(pedidos.save(pedido));
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidos.findAllByOrderById().stream().map(PedidoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse consultar(Long id) {
        return PedidoResponse.de(pedidos.findById(id).orElseThrow(() -> ausente(id)));
    }

    public PedidoResponse adicionar(Long id, ItemPedidoRequest item) {
        var pedido = buscarParaAlterar(id);
        pedido.exigirAberto();
        pedido.adicionar(produtos.buscarAtivo(item.produtoId()), item.quantidade());
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse atualizar(Long id, Long produtoId, AtualizacaoQuantidadeRequest dados) {
        var pedido = buscarParaAlterar(id);
        pedido.alterarQuantidade(produtoId, dados.quantidade());
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse remover(Long id, Long produtoId) {
        var pedido = buscarParaAlterar(id);
        pedido.remover(produtoId);
        return PedidoResponse.de(pedido);
    }

    public PedidoResponse cancelar(Long id) {
        var pedido = buscarParaAlterar(id);
        pedido.cancelar();
        return PedidoResponse.de(pedido);
    }

    private Pedido buscarParaAlterar(Long id) {
        return pedidos.buscarParaAlterar(id).orElseThrow(() -> ausente(id));
    }

    private RecursoNaoEncontradoException ausente(Long id) {
        return new RecursoNaoEncontradoException("Pedido não encontrado: " + id);
    }
}
