package com.github.vitormozer9.pedidosestoque.entity;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import com.github.vitormozer9.pedidosestoque.exception.ConflitoDeNegocioException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoTest {
    @Test
    void calculaTotalComCentavosEPreservaItensAoCancelar() {
        var produto = mock(Produto.class);
        when(produto.getId()).thenReturn(1L);
        when(produto.getPreco()).thenReturn(new BigDecimal("3.19"));
        var pedido = new Pedido(produto, 3);
        assertThrows(ConflitoDeNegocioException.class, () -> pedido.adicionar(produto, 1));
        assertThrows(ConflitoDeNegocioException.class, () -> pedido.remover(1L));
        pedido.cancelar();
        assertEquals(StatusPedido.CANCELADO, pedido.getStatus());
        assertEquals(new BigDecimal("9.57"), pedido.getTotal());
        assertThrows(ConflitoDeNegocioException.class, () -> pedido.alterarQuantidade(1L, 4));
    }
}
