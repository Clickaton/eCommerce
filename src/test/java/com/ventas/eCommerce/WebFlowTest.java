package com.ventas.eCommerce;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.entities.User;
import com.ventas.eCommerce.enums.OrderStatus;
import com.ventas.eCommerce.enums.Rol;
import com.ventas.eCommerce.repositories.OrderRepository;
import com.ventas.eCommerce.repositories.ProductRepository;
import com.ventas.eCommerce.security.AuthUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class WebFlowTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private TestData data;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicPagesRender() throws Exception {
        Product product = data.product("1200", 4);
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string(containsString("Novedades")));
        mvc.perform(get("/products").param("q", "Perfume")).andExpect(status().isOk())
                .andExpect(content().string(containsString(product.getName())));
        mvc.perform(get("/products/" + product.getId())).andExpect(status().isOk())
                .andExpect(content().string(containsString("Agregar al carrito")));
        mvc.perform(get("/user/login")).andExpect(status().isOk());
        mvc.perform(get("/user/register")).andExpect(status().isOk());
        mvc.perform(get("/products/999999")).andExpect(status().isNotFound());
    }

    @Test
    void protectedPagesRequireLoginAndAdminRole() throws Exception {
        mvc.perform(get("/cart")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/user/login"));
        mvc.perform(get("/admin").with(user(AuthUser.from(data.user(Rol.USER))))).andExpect(status().isForbidden());
        mvc.perform(post("/cart/items").param("productId", "1").with(user(AuthUser.from(data.user(Rol.USER)))))
                .andExpect(status().isForbidden()); // sin token CSRF
    }

    @Test
    void registrationValidatesAndCreatesUser() throws Exception {
        mvc.perform(post("/user/register").with(csrf())
                        .param("name", "Ana").param("lastName", "García").param("email", "no-es-email")
                        .param("phone", "123").param("password", "1234").param("confirmPassword", "4321"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El email no es válido")));

        mvc.perform(post("/user/register").with(csrf())
                        .param("name", "Ana").param("lastName", "García").param("email", "ana@test.com")
                        .param("phone", "1144445555").param("password", "secreta123").param("confirmPassword", "secreta123"))
                .andExpect(redirectedUrl("/user/login"));

        mvc.perform(post("/logincheck").with(csrf()).param("email", "ana@test.com").param("password", "secreta123"))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void oversizedImageShowsValidationErrorInsteadOf500() throws Exception {
        MockMultipartFile bigImage = new MockMultipartFile("image", "foto.jpg", "image/jpeg", new byte[5_000_000]);
        mvc.perform(multipart("/user/register").file(bigImage).with(csrf())
                        .param("name", "Leo").param("lastName", "Gómez").param("email", "leo@test.com")
                        .param("phone", "1155556666").param("password", "secreta123").param("confirmPassword", "secreta123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("La imagen no puede superar los 2 MB")));
    }

    @Test
    void fullPurchaseWithSimulatedGateway() throws Exception {
        User buyer = data.user(Rol.USER);
        AuthUser auth = AuthUser.from(buyer);
        Product product = data.product("2000", 5);

        mvc.perform(post("/cart/items").with(user(auth)).with(csrf())
                        .param("productId", product.getId().toString()).param("quantity", "2"))
                .andExpect(redirectedUrl("/cart"));
        mvc.perform(get("/cart").with(user(auth))).andExpect(status().isOk())
                .andExpect(content().string(containsString(product.getName())));
        mvc.perform(get("/checkout").with(user(auth))).andExpect(status().isOk());

        MvcResult checkout = mvc.perform(post("/checkout").with(user(auth)).with(csrf())
                        .param("recipientName", "Juan").param("street", "Calle 1").param("city", "CABA")
                        .param("province", "BA").param("postalCode", "1000").param("phone", "1131188134"))
                .andExpect(redirectedUrlPattern("/payments/simulated/*"))
                .andReturn();
        String paymentUrl = checkout.getResponse().getRedirectedUrl();
        Integer orderId = Integer.valueOf(paymentUrl.substring(paymentUrl.lastIndexOf('/') + 1));

        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(3);
        mvc.perform(get(paymentUrl).with(user(auth))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Aprobar pago")));

        MvcResult approved = mvc.perform(post(paymentUrl).with(user(auth)).with(csrf()).param("approved", "true"))
                .andExpect(redirectedUrlPattern("/payments/return?*"))
                .andReturn();
        mvc.perform(get(approved.getResponse().getRedirectedUrl()).with(user(auth)))
                .andExpect(redirectedUrl("/orders/" + orderId));

        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.PAID);
        mvc.perform(get("/orders").with(user(auth))).andExpect(status().isOk());
        mvc.perform(get("/orders/" + orderId).with(user(auth))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Pagado")));

        // Otro usuario no puede ver el pedido.
        mvc.perform(get("/orders/" + orderId).with(user(AuthUser.from(data.user(Rol.USER)))))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminPagesRenderAndManageOrders() throws Exception {
        AuthUser admin = AuthUser.from(data.user(Rol.ADMIN));
        User buyer = data.user(Rol.USER);
        Product product = data.product("300", 2);
        mvc.perform(post("/cart/items").with(user(AuthUser.from(buyer))).with(csrf())
                .param("productId", product.getId().toString()));
        mvc.perform(post("/checkout").with(user(AuthUser.from(buyer))).with(csrf())
                .param("recipientName", "Juan").param("street", "Calle 1").param("city", "CABA")
                .param("province", "BA").param("postalCode", "1000").param("phone", "1131188134"));
        Integer orderId = orderRepository.findAll().stream()
                .filter(o -> o.getUser().getId().equals(buyer.getId())).findFirst().orElseThrow().getId();

        mvc.perform(get("/admin").with(user(admin))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Últimos pedidos")));
        mvc.perform(get("/admin/products").with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/admin/products/new").with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/admin/products/" + product.getId() + "/edit").with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/admin/users").with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/admin/orders").param("status", "PENDING").with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/admin/orders/" + orderId).with(user(admin))).andExpect(status().isOk())
                .andExpect(content().string(containsString("Marcar como pagado")));

        mvc.perform(post("/admin/orders/" + orderId + "/status").with(user(admin)).with(csrf()).param("status", "CANCELLED"))
                .andExpect(redirectedUrl("/admin/orders/" + orderId));
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(2);
    }

    @Test
    void apiUsesJwt() throws Exception {
        User buyer = data.user(Rol.USER);
        Product product = data.product("100", 3);

        mvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
        mvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + buyer.getEmail() + "\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());

        MvcResult login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + buyer.getEmail() + "\",\"password\":\"" + TestData.PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode token = objectMapper.readTree(login.getResponse().getContentAsString());
        String bearer = "Bearer " + token.get("accessToken").asText();

        mvc.perform(post("/api/cart/items").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + product.getId() + ",\"quantity\":5}"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(post("/api/cart/items").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + product.getId() + ",\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(2));

        mvc.perform(post("/api/orders").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recipientName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.recipientName").exists());
        mvc.perform(post("/api/orders").header("Authorization", bearer).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestData.checkoutForm())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentUrl", startsWith("http://localhost:8080/payments/simulated/")));

        mvc.perform(get("/api/admin/stats").header("Authorization", bearer)).andExpect(status().isForbidden());
        mvc.perform(get("/api/cart").header("Authorization", "Bearer token-invalido")).andExpect(status().isUnauthorized());
    }
}
