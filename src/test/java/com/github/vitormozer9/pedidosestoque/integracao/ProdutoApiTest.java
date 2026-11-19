package com.github.vitormozer9.pedidosestoque.integracao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProdutoApiTest extends ApiTestBase {
    @Test
    void cicloDeVida() throws Exception {
        long id = criarProduto();
        String rota = PRODUTOS + "/" + id;
        mvc.perform(get(rota)).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Mouse"));
        enviar(put(rota), """
                {"nome":"Teclado","preco":10.99}
                """).andExpect(status().isOk()).andExpect(jsonPath("$.preco").value(10.99));
        mvc.perform(get(PRODUTOS)).andExpect(jsonPath("$[?(@.id == " + id + ")]").isNotEmpty());
        mvc.perform(delete(rota)).andExpect(status().isNoContent());
        mvc.perform(get(rota)).andExpect(status().isNotFound()).andExpect(jsonPath("$.erro").isString());
        mvc.perform(get(PRODUTOS)).andExpect(jsonPath("$[?(@.id == " + id + ")]").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{", "{\"nome\":\" \",\"preco\":1}",
            "{\"nome\":\"A\",\"preco\":0}", "{\"nome\":\"A\",\"preco\":2.005}"})
    void rejeitaCadastroInvalido(String dados) throws Exception {
        enviar(post(PRODUTOS), dados).andExpect(status().isBadRequest());
    }

    @Test
    void verificaLimitesDeTexto() throws Exception {
        String formato = "{\"nome\":\"%s\",\"descricao\":\"%s\",\"preco\":1}";
        enviar(post(PRODUTOS), formato.formatted("a".repeat(120), "b".repeat(500))).andExpect(status().isCreated());
        enviar(post(PRODUTOS), formato.formatted("a".repeat(121), "")).andExpect(status().isBadRequest());
        enviar(post(PRODUTOS), formato.formatted("a", "b".repeat(501))).andExpect(status().isBadRequest());
    }
}
