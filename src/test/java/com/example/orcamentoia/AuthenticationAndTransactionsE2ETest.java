package com.example.orcamentoia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationAndTransactionsE2ETest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @Test
    void registrationLoginAndTransactionFlowIsolatedPerUser() throws Exception {
        String alice = register("alice");
        String bob = register("bob");

        mvc.perform(post("/api/transactions").header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Mercado","amount":25.50,"type":"EXPENSE","category":"Alimentação"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/transactions").header("Authorization", "Bearer " + bob))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0]").doesNotExist());

        mvc.perform(get("/api/transactions/summary").header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalExpense").value(25.5));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"correct-horse-battery\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void protectedEndpointRejectsMissingOrInvalidToken() throws Exception {
        mvc.perform(get("/api/transactions")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/transactions").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidTransactionAmount() throws Exception {
        String token = register("charlie");
        mvc.perform(post("/api/transactions").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Mercado\",\"amount\":0,\"type\":\"EXPENSE\",\"category\":\"Alimentação\"}"))
                .andExpect(status().isBadRequest());
    }

    private String register(String username) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"correct-horse-battery\"}"))
                .andExpect(status().isCreated()).andReturn();
        JsonNode json = mapper.readTree(result.getResponse().getContentAsString());
        return json.get("accessToken").asText();
    }
}
