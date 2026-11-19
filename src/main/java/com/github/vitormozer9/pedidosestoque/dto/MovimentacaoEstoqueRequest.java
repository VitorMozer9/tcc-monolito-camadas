package com.github.vitormozer9.pedidosestoque.dto;

import jakarta.validation.constraints.*;
import com.github.vitormozer9.pedidosestoque.entity.TipoMovimentacao;

public record MovimentacaoEstoqueRequest(@NotNull TipoMovimentacao tipo,
                                        @NotNull @Positive Integer quantidade) {}
