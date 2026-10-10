package com.tcc.rj_system.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.tcc.rj_system.service.AuthService;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;

@SpringBootTest
class SecurityConfigTest {
    @Autowired WebApplicationContext context;
    @MockitoBean AuthService authService;
    private MockMvc mvc;
    private static final String REGISTER = "{\"nome\":\"Operador\",\"email\":\"user@example.com\",\"senha\":\"senha123\"}";

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void cadastroAnonimoERejeitado() throws Exception {
        mvc.perform(post("/api/auth/register").contentType("application/json").content(REGISTER))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(authService);
    }

    @Test
    void operadorNaoPodeCadastrarUsuarios() throws Exception {
        mvc.perform(post("/api/auth/register").with(user("operador").roles("OPERADOR"))
                .contentType("application/json").content(REGISTER)).andExpect(status().isForbidden());
        verifyNoInteractions(authService);
    }

    @Test
    void administradorPodeCadastrarUsuarios() throws Exception {
        mvc.perform(post("/api/auth/register").with(user("admin").roles("ADMIN"))
                .contentType("application/json").content(REGISTER)).andExpect(status().isCreated());
        verify(authService).register(any());
    }

    @Test
    void consultaDeUsuarioExigeAutenticacao() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        verifyNoInteractions(authService);
    }

    @Test
    void loginContinuaPublico() throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json")
                .content("{\"email\":\"user@example.com\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk());
        verify(authService).login(any());
    }
}
