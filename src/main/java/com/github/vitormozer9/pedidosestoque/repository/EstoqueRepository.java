package com.github.vitormozer9.pedidosestoque.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.github.vitormozer9.pedidosestoque.entity.Estoque;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    Optional<Estoque> findByProdutoId(Long produtoId);
}
