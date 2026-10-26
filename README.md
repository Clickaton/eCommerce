# AntognParfum eCommerce

Tienda online en Spring Boot 3.5 (Java 17) con vistas Thymeleaf, API REST con JWT, MySQL y pagos con MercadoPago.

## Arrancar en local

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

El perfil `dev` usa la base `ecommerce_dev` (se crea sola), carga productos de ejemplo y crea el administrador
`admin@antognparfum.test` / `admin12345`. Los pagos usan la **pasarela simulada** (pantalla para aprobar/rechazar).

Tests (usan H2 en memoria, no necesitan MySQL):

```bash
./mvnw test
```

## Configuración (variables de entorno)

| Variable | Uso | Default |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Conexión MySQL | `localhost:3306/ecommerce`, `root`/`root` |
| `APP_BASE_URL` | URL pública (retornos y webhooks de pago) | `http://localhost:8080` |
| `JWT_SECRET` | Clave Base64 ≥ 256 bits (`openssl rand -base64 32`) | clave de desarrollo, **cambiar en producción** |
| `JWT_EXPIRATION` | Vigencia del token | `2h` |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Admin inicial (se crea si no existe) | vacío |
| `ORDER_RESERVATION_TIME` | Tiempo que un pedido sin pagar reserva stock | `60m` |
| `PAYMENT_PROVIDER` | `simulated` o `mercadopago` | `simulated` |
| `MP_ACCESS_TOKEN` | Access token de MercadoPago (`TEST-...` en sandbox) | — |
| `MP_WEBHOOK_SECRET` | Clave secreta de webhooks para validar `x-signature` | — |

### MercadoPago

1. Crear una aplicación en el panel de desarrolladores de MercadoPago y copiar el access token.
2. `PAYMENT_PROVIDER=mercadopago`, `MP_ACCESS_TOKEN=...`, `APP_BASE_URL=https://tu-dominio`.
3. Configurar el webhook (evento *Pagos*) apuntando a `https://tu-dominio/payments/webhook/mercadopago` y copiar la clave secreta en `MP_WEBHOOK_SECRET`.

La aplicación nunca recibe datos de tarjeta: crea una preferencia de pago, redirige a MercadoPago y confirma el
pago consultando la API (por webhook y al volver a `/payments/return`).

## Flujo de compra

Carrito (`CartItem` con cantidades) → checkout con datos de envío → se crea un `Order` **PENDIENTE** y se descuenta
el stock con un `UPDATE ... WHERE stock >= cantidad` atómico (dos compras simultáneas del último artículo no pueden
ganar ambas) → pago en la pasarela → **PAGADO** → el admin lo marca **ENVIADO** / **ENTREGADO**. Los pedidos
pendientes que vencen se cancelan solos y devuelven el stock.

## API REST

Autenticación: `POST /api/auth/login` `{"email","password"}` → `{"accessToken"}`; luego `Authorization: Bearer <token>`.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/products`, `/api/products/{id}` | Catálogo (público; filtros `q`, `category`, paginado) |
| GET | `/api/auth/me` | Usuario actual |
| GET/POST/PUT/DELETE | `/api/cart`, `/api/cart/items[/{productId}]` | Carrito |
| GET/POST | `/api/orders`, `/api/orders/{id}` | Pedidos; `POST` hace checkout y devuelve `paymentUrl` |
| POST | `/api/orders/{id}/payment`, `/api/orders/{id}/cancel` | Reintentar pago / cancelar |
| GET/PATCH | `/api/admin/stats`, `/api/admin/orders[/{id}/status]` | Solo ADMIN |

Los errores se devuelven como `application/problem+json`.
