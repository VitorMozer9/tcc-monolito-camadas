package com.github.vitormozer9.pedidosestoque.service;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import com.github.vitormozer9.pedidosestoque.entity.Produto;
import com.github.vitormozer9.pedidosestoque.repository.ProdutoRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProdutoServiceTest {
    @Test
    void exclusaoDesativaRegistroExistente() {
        var repositorio = mock(ProdutoRepository.class);
        var produto = new Produto("Mouse", null, BigDecimal.TEN);
        when(repositorio.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(produto));
        new ProdutoService(repositorio).desativar(1L);
        assertFalse(produto.isAtivo());
        verify(repositorio, never()).delete(produto);
    }
}
