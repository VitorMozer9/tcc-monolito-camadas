package com.github.vitormozer9.pedidosestoque.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record CriacaoPedidoRequest(@NotEmpty List<@NotNull @Valid ItemPedidoRequest> itens) {}
