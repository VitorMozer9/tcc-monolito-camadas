package com.github.vitormozer9.pedidosestoque.entity;

import org.junit.jupiter.api.Test;
import com.github.vitormozer9.pedidosestoque.exception.ConflitoDeNegocioException;
import static org.junit.jupiter.api.Assertions.*;

class EstoqueTest {
    @Test
    void saidaInsuficientePreservaSaldoESaidaExataZera() {
        var estoque = new Estoque(1L, 0);
        estoque.movimentar(TipoMovimentacao.ENTRADA, 3);
        assertThrows(ConflitoDeNegocioException.class, () -> estoque.movimentar(TipoMovimentacao.SAIDA, 4));
        assertEquals(3, estoque.getQuantidadeDisponivel());
        estoque.movimentar(TipoMovimentacao.SAIDA, 3);
        assertEquals(0, estoque.getQuantidadeDisponivel());
    }
}
