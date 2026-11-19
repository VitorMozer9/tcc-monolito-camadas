package com.github.vitormozer9.pedidosestoque.integracao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.vitormozer9.pedidosestoque.repository.PedidoRepository;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PedidoApiTest extends ApiTestBase {
    @Autowired PedidoRepository pedidos;

    private String item(long produto, int quantidade) {
        return "{\"produtoId\":%d,\"quantidade\":%d}".formatted(produto, quantidade);
    }

    private String pedido(String... itens) {
        return "{\"itens\":[" + String.join(",", itens) + "]}";
    }

    @Test
    void pedidoPreservaPrecoNomeESaldoDuranteTodoOCiclo() throws Exception {
        long produto = criarProduto();
        long outro = criarProduto();
        enviar(post(ESTOQUES), "{\"produtoId\":%d,\"quantidadeInicial\":1}".formatted(produto))
                .andExpect(status().isCreated());
        long id = id(enviar(post(PEDIDOS), pedido(item(produto, 3))).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO")).andExpect(jsonPath("$.total").value(150))
                .andExpect(jsonPath("$.criadoEm").isString()).andExpect(jsonPath("$.itens[0].length()").value(5))
                .andExpect(jsonPath("$.itens[0].id").doesNotExist()));
        String rota = PEDIDOS + "/" + id;
        String itens = rota + "/itens";
        mvc.perform(get(ESTOQUES + "/" + produto)).andExpect(jsonPath("$.quantidadeDisponivel").value(1));
        enviar(put(PRODUTOS + "/" + produto), "{\"nome\":\"Novo\",\"preco\":55}").andExpect(status().isOk());
        mvc.perform(delete(PRODUTOS + "/" + produto)).andExpect(status().isNoContent());
        enviar(put(itens + "/" + produto), "{\"quantidade\":2}").andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].subtotal").value(100))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(50))
                .andExpect(jsonPath("$.itens[0].nomeProduto").value("Mouse"));
        enviar(post(itens), item(outro, 1)).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(150));
        enviar(post(itens), item(outro, 2)).andExpect(status().isConflict());
        mvc.perform(delete(itens + "/" + outro)).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(100));
        mvc.perform(delete(itens + "/" + outro)).andExpect(status().isNotFound());
        mvc.perform(delete(itens + "/" + produto)).andExpect(status().isConflict());
        mvc.perform(post(rota + "/cancelamento")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELADO"));
        mvc.perform(post(rota + "/cancelamento")).andExpect(status().isConflict()).andExpect(jsonPath("$.erro").isString());
        enviar(post(itens), item(outro, 1)).andExpect(status().isConflict());
        enviar(put(itens + "/" + produto), "{\"quantidade\":1}").andExpect(status().isConflict());
        mvc.perform(delete(itens + "/" + produto)).andExpect(status().isConflict());
        mvc.perform(get(rota)).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(100));
        mvc.perform(get(PEDIDOS)).andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == " + id + " && @.status == 'CANCELADO')]").isNotEmpty());
        mvc.perform(get(ESTOQUES + "/" + produto)).andExpect(jsonPath("$.quantidadeDisponivel").value(1));
    }

    @Test
    void aceitaPedidoSemEstoqueEReverteCriacaoInvalida() throws Exception {
        long produto = criarProduto();
        enviar(post(PEDIDOS), pedido(item(produto, 2))).andExpect(status().isCreated());
        mvc.perform(get(ESTOQUES + "/" + produto)).andExpect(status().isNotFound());
        long antes = pedidos.count();
        enviar(post(PEDIDOS), pedido(item(produto, 1), item(Long.MAX_VALUE, 1))).andExpect(status().isNotFound());
        enviar(post(PEDIDOS), pedido(item(produto, 1), item(produto, 2))).andExpect(status().isConflict());
        mvc.perform(delete(PRODUTOS + "/" + produto)).andExpect(status().isNoContent());
        enviar(post(PEDIDOS), pedido(item(produto, 1))).andExpect(status().isNotFound());
        assertEquals(antes, pedidos.count());
        mvc.perform(get(PEDIDOS + "/" + Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"itens\":[]}", "{\"itens\":[null]}", "{\"itens\":[{}]}",
            "{\"itens\":[{\"produtoId\":1,\"quantidade\":0}]}", "{\"itens\":[{\"produtoId\":1,\"quantidade\":1.5}]}"})
    void criacaoInvalidaRetorna400(String corpo) throws Exception {
        enviar(post(PEDIDOS), corpo).andExpect(status().isBadRequest());
    }
}
