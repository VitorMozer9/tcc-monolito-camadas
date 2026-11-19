package com.github.vitormozer9.pedidosestoque.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.github.vitormozer9.pedidosestoque.entity.*;

public record PedidoResponse(Long id, StatusPedido status, Instant criadoEm, List<ItemPedidoResponse> itens,
                             BigDecimal total) {
    public static PedidoResponse de(Pedido pedido) {
        return new PedidoResponse(pedido.getId(), pedido.getStatus(), pedido.getCriadoEm(),
                pedido.getItens().stream().map(ItemPedidoResponse::de).toList(), pedido.getTotal());
    }
}
