# Informe de Integración con CS 1.6

## 1. Estado Actual de la Web (eCommerce)
La aplicación web actual es un eCommerce básico en Spring Boot (con Hibernate/JPA y MySQL).
- **Backend:** Desarrollado en Java 17 con Spring Boot.
- **Base de Datos:** MySQL.
- **Entidades actuales:** `User`, `Product`, `Cart`, `Transaction`, `Image`.
- **Faltantes críticos:** No existe lógica de compra finalizada ni control de inventario de cada usuario. Solo hay un carrito (`Cart`) y una `Transaction` con problemas de seguridad.

## 2. Cambios Necesarios en la Base de Datos
Para implementar el sistema de vinculación y entrega de beneficios en el juego de CS 1.6, deben realizarse los siguientes cambios:

### 2.1 Tabla de Usuarios (`User`)
- Agregar campo `steamId` (String): Para identificar al usuario de manera inequívoca en el servidor del juego.
- Agregar campo `csLinkCode` (String): Código temporal de vinculación generado aleatoriamente de 6 caracteres.
- Agregar campo `csLinkCodeExpiration` (LocalDateTime): Fecha y hora límite para usar el código de vinculación.

### 2.2 Tablas de Inventario (`Inventory` e `InventoryItem`)
El juego necesita saber "qué" ha comprado el usuario.
- **Entidad `Inventory`:** Relacionada 1 a 1 con `User`. Representa todo lo que el usuario posee.
- **Entidad `InventoryItem`:** Relacionada Muchos a 1 con `Inventory`. Contiene:
    - Referencia al producto (`Product`, que representaría el beneficio: skin, VIP, etc.).
    - Estado de equipamiento: `isEquipped` (Boolean) para que el servidor sepa si debe aplicar el modelo/trail de inmediato al revivir.

### 2.3 Tipos de Productos
- La entidad `Product` actual debe distinguir si el producto es un ítem de juego (Ej: Model de admin, Trail, VIP) u otra cosa. Esto se puede lograr extendiendo el enum `Category`.
- El producto debe tener un identificador interno (ej. `game_model_path`, `game_item_id`) para que la respuesta de la API pueda enviarle al servidor del juego exactamente qué recurso usar (ej. `models/player/admin/admin.mdl`).

## 3. Nuevos Endpoints en el Backend (API REST)
Dado que se recomienda la comunicación mediante API HTTP (con un plugin AMXX compatible como Grip HTTP), necesitamos crear nuevos endpoints seguros. Estos endpoints estarán protegidos por una **API Key** o **Token Fijo** para los servidores de CS 1.6, que debe pasarse por los headers (Ej: `x-api-key`).

### 3.1 Endpoint de Vinculación en el Juego
- **POST `/api/cs/link`**
- **Cuerpo (JSON):** `{"steamId": "STEAM_0:1:1234567", "code": "A1B2C3"}`
- **Lógica:** El backend busca al usuario cuyo `csLinkCode` sea igual al recibido y verifica que no haya expirado según `csLinkCodeExpiration`. Si es válido, actualiza el `steamId` del usuario, borra el código temporal y responde `200 OK`. Si el SteamID ya estaba en uso, debe manejar la reasignación de cuenta adecuadamente.

### 3.2 Endpoint de Inventario (Lectura)
- **GET `/api/cs/inventory/{steamid}`**
- **Lógica:** Busca al usuario por su SteamID. Si existe, retorna un JSON estructurado con todos los items comprados y su estado de equipamiento.
- **Ejemplo de Respuesta:**
```json
{
  "steamId": "STEAM_0:1:1234567",
  "items": [
    {"type": "MODEL", "resourcePath": "models/player/admin/admin.mdl", "equipped": true},
    {"type": "TRAIL", "resourcePath": "sprites/laserbeam.spr", "color": "255 0 0", "equipped": false}
  ]
}
```

### 3.3 Endpoint de Equipamiento (Escritura desde el juego)
- **POST `/api/cs/inventory/{steamid}/equip`**
- **Cuerpo (JSON):** `{"itemId": 12, "equip": true}`
- **Lógica:** Permite al plugin AMXX notificar a la base de datos cuando el jugador selecciona y equipa un skin desde el menú del juego (`/tienda`), para que quede guardado para su próxima conexión.

## 4. Cambios Necesarios en el Frontend (Thymeleaf)
- **Panel de Usuario:** Crear una nueva sección (ej. `/user/cs-link`).
- **Botón "Vincular mi cuenta CS":** Al presionarlo, hace un POST que genera el código temporal de 6 caracteres y lo muestra en pantalla con una cuenta regresiva (ej. 5 minutos).
- **Aviso de Vinculación:** Mostrar el SteamID actual vinculado y dar la opción de desvincular la cuenta si fuera necesario.

## 5. Arquitectura del Plugin AMXX
- **Conexión HTTP:** Utilizar Grip para las peticiones REST. Las consultas como la carga de inventario al conectar un usuario deben ser asíncronas (`grip_request` sin bloquear).
- **Menú y caché:** Almacenar el inventario de los jugadores en arrays/tries locales (Trie o Array) mientras estén en el servidor, para evitar consultar la API cada vez que reviven.
- **Eventos:** Aplicar los modelos usando ReAPI (`rg_set_user_model`) al invocar `RG_CBasePlayer_Spawn` (Spawn del jugador).
- **Seguridad:** Hardcodear la API_KEY y la URL_BASE del backend de forma segura en las *cvars* del servidor (no en el plugin .sma crudo si es posible, o protegerlo bien).

## 6. Conclusión
Para pasar de un eCommerce abandonado a un sistema real de venta para CS 1.6, se deben aplicar urgentemente los arreglos de arquitectura listados en el `INFORME_ECOMMERCE.md` previo (eliminar el almacenamiento de tarjetas en plano, mejorar DTOs) y agregar el módulo de la **API de Integración**, aislando esta lógica de la API pública o web. Con estas entidades, DTOs y endpoints, el servidor AMXX podrá funcionar como un cliente ciego de la web, cumpliendo exactamente con las reglas indicadas.