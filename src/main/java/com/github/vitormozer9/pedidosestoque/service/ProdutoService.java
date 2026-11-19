package com.github.vitormozer9.pedidosestoque.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.vitormozer9.pedidosestoque.dto.*;
import com.github.vitormozer9.pedidosestoque.entity.Produto;
import com.github.vitormozer9.pedidosestoque.repository.ProdutoRepository;
import com.github.vitormozer9.pedidosestoque.exception.RecursoNaoEncontradoException;

@Service
@Transactional(readOnly = true)
public class ProdutoService {
    private final ProdutoRepository produtos;

    public ProdutoService(ProdutoRepository produtos) {
        this.produtos = produtos;
    }

    @Transactional
    public ProdutoResponse cadastrar(ProdutoRequest dados) {
        var produto = new Produto(dados.nome(), dados.descricao(), dados.preco());
        return ProdutoResponse.de(produtos.save(produto));
    }

    public List<ProdutoResponse> listar() {
        return produtos.findByAtivoTrueOrderById().stream().map(ProdutoResponse::de).toList();
    }

    public ProdutoResponse consultar(Long id) {
        return ProdutoResponse.de(buscarAtivo(id));
    }

    Produto buscarAtivo(Long id) {
        return produtos.findByIdAndAtivoTrue(id).orElseThrow(
                () -> new RecursoNaoEncontradoException("Produto ativo não encontrado: " + id));
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest dados) {
        var produto = buscarAtivo(id);
        produto.atualizar(dados.nome(), dados.descricao(), dados.preco());
        return ProdutoResponse.de(produto);
    }

    @Transactional
    public void desativar(Long id) {
        buscarAtivo(id).desativar();
    }
}
