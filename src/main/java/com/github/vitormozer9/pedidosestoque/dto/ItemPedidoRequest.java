package com.github.vitormozer9.pedidosestoque.dto;

import jakarta.validation.constraints.*;

public record ItemPedidoRequest(@NotNull @Positive Long produtoId, @NotNull @Positive Integer quantidade) {}
