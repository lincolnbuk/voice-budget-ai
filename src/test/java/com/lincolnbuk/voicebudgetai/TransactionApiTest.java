package com.lincolnbuk.voicebudgetai;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionApiTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @Test
    void protectedEndpointRejectsRequestsWithoutJwt() throws Exception {
        mvc.perform(get("/api/transactions")).andExpect(status().isUnauthorized());
    }

    @Test
    void createsTransactionWithJwtAndRejectsInvalidAmount() throws Exception {
        String token = register("testuser");
        mvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Groceries\",\"amount\":25.50,\"type\":\"EXPENSE\",\"category\":\"Food\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Invalid\",\"amount\":0,\"type\":\"EXPENSE\",\"category\":\"Food\"}"))
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
