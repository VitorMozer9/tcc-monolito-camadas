package com.github.vitormozer9.pedidosestoque.repository;

import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import com.github.vitormozer9.pedidosestoque.entity.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @EntityGraph(attributePaths = "itens")
    List<Pedido> findAllByOrderById();

    @EntityGraph(attributePaths = "itens")
    Optional<Pedido> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> buscarParaAlterar(Long id);
}
