# Informe de Estado y Refactorización del Proyecto eCommerce

## 1. Estado Actual del Proyecto
El proyecto actual es una versión temprana y presenta una arquitectura básica de Spring Boot, pero carece de muchas funcionalidades y convenciones necesarias para un sistema en producción.

### 1.1 Entidades Existentes
- **User**: Representa a los usuarios del sistema. Contiene información básica, un rol (USER/ADMIN), una relación `OneToOne` con `Cart` y una lista de transacciones.
- **Product**: Representa los productos a vender. Tiene campos como nombre, descripción, marca, precio, categoría, stock y una bandera `creationDeletion` que parece ser un borrado lógico.
- **Cart**: Representa el carrito de compras. Tiene una relación `ManyToMany` con `Product`. **Problema**: Solo guarda referencias a los productos, pero no las cantidades que el usuario quiere comprar.
- **Transaction**: Representa un intento de compra o transacción. **Problema crítico**: Está almacenando datos de tarjetas (`cardNumber`, `expirationDate`) en texto plano directamente en la base de datos, lo cual es una grave violación de seguridad y normativas (como PCI-DSS).
- **Image**: Manejo de imágenes de productos y usuarios.

### 1.2 Controladores y Servicios
- Existen controladores (`UserController`, `ProductController`, `CartController`, `AdminController`) y servicios que manejan la lógica básica (alta de productos, alta de usuarios, agregar al carrito).
- **CartController**: Tiene lógica comentada de otro proyecto (código relacionado a libros y autores) que indica que el código fue reciclado de otros proyectos.
- El controlador de administrador (`AdminController`) solo tiene un endpoint básico para el dashboard.

---

## 2. Desarrollos Faltantes para un eCommerce Completo

Para que el proyecto funcione de manera genérica y completa, se deben desarrollar las siguientes características:

### 2.1 Modelo de Pedidos (Orders)
Actualmente, no hay un concepto real de un "Pedido" finalizado. Se necesita crear:
- **Order (Pedido)**: Entidad que represente una compra confirmada. Debe incluir el usuario, fecha, estado del pedido (PENDIENTE, PAGADO, ENVIADO, CANCELADO), dirección de envío, método de pago, y total a pagar.
- **OrderItem (Detalle de Pedido)**: Entidad que relacione el `Order` con el `Product`, y que incluya el precio al momento de la compra (ya que los precios cambian) y la cantidad comprada.

### 2.2 Gestión de Cantidades en el Carrito (CartItems)
El `Cart` actual solo tiene una lista de `Product`. Se debe reemplazar por una lista de `CartItem`, donde cada item tenga una referencia al `Product` y la `cantidad` seleccionada.

### 2.3 Pasarela de Pagos (Payment Gateway)
No se deben guardar números de tarjeta de crédito en la base de datos bajo ninguna circunstancia.
- Se debe integrar una pasarela de pagos real (ej. Stripe, MercadoPago, PayPal).
- La aplicación solo debe manejar un identificador de pago externo o un token seguro generado por la pasarela, y dejar que los datos sensibles sean procesados por el proveedor de pagos.

### 2.4 Manejo Concurrente de Stock
Al momento de confirmar una compra, se debe descontar el stock del producto de manera segura para evitar que dos usuarios compren el último artículo al mismo tiempo (condiciones de carrera). Se recomienda usar bloqueos optimistas (Optimistic Locking) o transacciones estrictas.

---

## 3. Mejores Prácticas de Arquitectura a Implementar

### 3.1 Uso de DTOs (Data Transfer Objects)
Actualmente, las entidades (como `User` o `Product`) se exponen directamente o se construyen a partir de parámetros sueltos en los controladores. Es altamente recomendado:
- Utilizar clases DTO (ej. `ProductRequestDTO`, `UserResponseDTO`) para separar el modelo de base de datos de los datos que se envían y reciben en las APIs/Vistas.

### 3.2 Manejo de Excepciones Global (Global Exception Handler)
Implementar un `@ControllerAdvice` para capturar excepciones genéricas y de negocio (ej. `MyException`) y devolver respuestas o vistas de error de manera estandarizada.

### 3.3 Autenticación y Autorización (JWT)
El proyecto utiliza una configuración básica de Spring Security con inicio de sesión por formulario (`formLogin`). Para modernizarlo (especialmente si en el futuro se separa el Frontend del Backend):
- **Implementar JWT (JSON Web Tokens)**: Configurar Spring Security como "Stateless" y usar un filtro de autenticación personalizado que genere un token JWT tras el login, y lo valide en cada petición subsiguiente mediante la cabecera `Authorization: Bearer <token>`.
- Restringir adecuadamente los endpoints mediante roles y permisos (`@PreAuthorize` o configuración en `HttpSecurity`).

### 3.4 Validaciones
Agregar dependencias como `spring-boot-starter-validation` y usar anotaciones como `@NotBlank`, `@NotNull`, `@Min` en los DTOs para validar automáticamente los datos de entrada antes de llegar a la lógica de negocio, reemplazando el método manual `validar()` actual.

### 3.5 Limpieza del Código
- Eliminar el código muerto y comentado de proyectos anteriores (como el código de "Libro" y "Autor" en `CartController`).
- Separar claramente la lógica del negocio (Servicios) del manejo web (Controladores).
