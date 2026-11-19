package com.github.vitormozer9.pedidosestoque.service;

import java.util.Locale;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.vitormozer9.pedidosestoque.dto.*;
import com.github.vitormozer9.pedidosestoque.entity.Estoque;
import com.github.vitormozer9.pedidosestoque.exception.*;
import com.github.vitormozer9.pedidosestoque.repository.EstoqueRepository;

@Service
@Transactional
public class EstoqueService {
    private final EstoqueRepository estoques;
    private final ProdutoService produtos;

    public EstoqueService(EstoqueRepository estoques, ProdutoService produtos) {
        this.estoques = estoques;
        this.produtos = produtos;
    }

    public EstoqueResponse cadastrar(CadastroEstoqueRequest dados) {
        produtos.buscarAtivo(dados.produtoId());
        var estoque = new Estoque(dados.produtoId(), dados.quantidadeInicial());
        try {
            return EstoqueResponse.de(estoques.saveAndFlush(estoque));
        } catch (DataIntegrityViolationException erro) {
            if (erro.getCause() instanceof ConstraintViolationException violacao
                    && violacao.getConstraintName() != null
                    && violacao.getConstraintName().toLowerCase(Locale.ROOT).contains("uk_estoque_produto")) {
                throw new ConflitoDeNegocioException("Estoque já cadastrado para o produto.");
            }
            throw erro;
        }
    }

    @Transactional(readOnly = true)
    public EstoqueResponse consultar(Long produtoId) {
        return EstoqueResponse.de(buscar(produtoId));
    }

    public EstoqueResponse movimentar(Long produtoId, MovimentacaoEstoqueRequest dados) {
        var estoque = buscar(produtoId);
        estoque.movimentar(dados.tipo(), dados.quantidade());
        return EstoqueResponse.de(estoque);
    }

    private Estoque buscar(Long produtoId) {
        return estoques.findByProdutoId(produtoId).orElseThrow(
                () -> new RecursoNaoEncontradoException("Estoque não encontrado: " + produtoId));
    }
}
