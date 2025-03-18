package es.santander.ascender.proyectoFinal2.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class TestSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Verifica que un usuario anónimo puede acceder a un endpoint protegido
     * dado que la configuración de prueba desactiva la autenticación
     */
    @Test
    @WithAnonymousUser
    public void deberiaSaltarseLaAutenticacionParaEndpointProtegido() throws Exception {
        mockMvc.perform(get("/api/articulos"))
                .andExpect(status().isOk());
    }

    /**
     * Verifica que un usuario anónimo puede acceder a un endpoint de administrador
     * sin autenticación gracias a la configuración de prueba
     */
    @Test
    @WithAnonymousUser
    public void deberiaSaltarseLaAutorizacionParaEndpointDeAdmin() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    /**
     * Verifica que las solicitudes POST funcionan sin token CSRF
     * gracias a la desactivación de CSRF en la configuración de prueba
     */
    @Test
    @WithAnonymousUser
    public void deberiaPermitirPOSTSinTokenCSRF() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\", \"password\":\"password\"}"))
                .andExpect(status().is2xxSuccessful());
    }
}