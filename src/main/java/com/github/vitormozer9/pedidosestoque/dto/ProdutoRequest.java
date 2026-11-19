package com.github.vitormozer9.pedidosestoque.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record ProdutoRequest(@NotBlank @Size(max = 120) String nome,
                             @Size(max = 500) String descricao,
                             @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal preco) {}
