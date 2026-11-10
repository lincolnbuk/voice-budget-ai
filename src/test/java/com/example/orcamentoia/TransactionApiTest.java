package com.example.orcamentoia;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.security.api-key=test-secret", "spring.ai.openai.api-key=test-openai-key"})
class TransactionApiTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void rejectsRequestsWithoutApiKey() throws Exception {
        mvc.perform(get("/api/transactions")).andExpect(status().isUnauthorized());
    }

    @Test
    void createsAndSummarizesValidatedExpense() throws Exception {
        mvc.perform(post("/api/transactions")
                        .header("X-API-Key", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Mercado","amount":25.50,"type":"EXPENSE","category":"Alimentação"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        mvc.perform(get("/api/transactions/summary").header("X-API-Key", "test-secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpense").value(25.5))
                .andExpect(jsonPath("$.balance").value(-25.5));
    }

    @Test
    void rejectsNonPositiveAmount() throws Exception {
        mvc.perform(post("/api/transactions")
                        .header("X-API-Key", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Mercado\",\"amount\":0,\"type\":\"EXPENSE\",\"category\":\"Alimentação\"}"))
                .andExpect(status().isBadRequest());
    }
}
