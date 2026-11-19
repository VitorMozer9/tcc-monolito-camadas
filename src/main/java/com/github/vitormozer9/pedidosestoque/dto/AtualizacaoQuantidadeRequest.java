package com.github.vitormozer9.pedidosestoque.dto;

import jakarta.validation.constraints.*;

public record AtualizacaoQuantidadeRequest(@NotNull @Positive Integer quantidade) {}
