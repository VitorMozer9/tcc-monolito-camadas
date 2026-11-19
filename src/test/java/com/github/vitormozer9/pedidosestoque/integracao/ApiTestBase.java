package com.github.vitormozer9.pedidosestoque.integracao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class ApiTestBase {
    static final String PRODUTOS = "/api/v1/produtos";
    static final String ESTOQUES = "/api/v1/estoques";
    static final String PEDIDOS = "/api/v1/pedidos";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    ResultActions enviar(MockHttpServletRequestBuilder request, String corpo) throws Exception {
        return mvc.perform(request.contentType("application/json").content(corpo));
    }

    long id(ResultActions resposta) throws Exception {
        return json.readTree(resposta.andReturn().getResponse().getContentAsByteArray()).get("id").asLong();
    }

    long criarProduto() throws Exception {
        return id(enviar(post(PRODUTOS), """
                {"nome":"Mouse","descricao":"Sem fio","preco":50.00}
                """).andExpect(status().isCreated()).andExpect(jsonPath("$.ativo").value(true)));
    }
}
