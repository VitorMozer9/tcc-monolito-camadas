package com.github.vitormozer9.pedidosestoque.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.github.vitormozer9.pedidosestoque.entity.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Optional<Produto> findByIdAndAtivoTrue(Long id);
    List<Produto> findByAtivoTrueOrderById();
}
