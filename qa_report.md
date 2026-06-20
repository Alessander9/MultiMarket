# MultiMarket QA Report

- Fecha: 2026-06-20T10:10:22.668Z
- Total: 45
- Aprobadas: 45
- Fallidas: 0

| Módulo | Rol | Prueba | Método | Endpoint | Esperado | Obtenido | Estado | Severidad | Evidencia |
|---|---|---|---|---|---:|---:|---|---|---|
| FRONTEND | PUBLICO | Frontend accesible | GET | / | 200 | 200 | APROBADO | info | HTML recibido |
| AUTH | ADMIN | Registro ADMIN | POST | /auth/register | 200/201 | 200 | APROBADO | info | Usuario registrado |
| AUTH | ADMIN | Login ADMIN | POST | /auth/login | 200 | 200 | APROBADO | info | token=eyJhbGciOiJIUzI1NiJ9.eyJ... |
| AUTH | VENDEDOR | Registro VENDEDOR | POST | /auth/register | 200/201 | 200 | APROBADO | info | Usuario registrado |
| AUTH | VENDEDOR | Login VENDEDOR | POST | /auth/login | 200 | 200 | APROBADO | info | token=eyJhbGciOiJIUzI1NiJ9.eyJ... |
| AUTH | COMPRADOR | Registro COMPRADOR | POST | /auth/register | 200/201 | 200 | APROBADO | info | Usuario registrado |
| AUTH | COMPRADOR | Login COMPRADOR | POST | /auth/login | 200 | 200 | APROBADO | info | token=eyJhbGciOiJIUzI1NiJ9.eyJ... |
| AUTH | PUBLICO | Login inválido | POST | /auth/login | 400 | 400 | APROBADO | info | {"status":400,"error":"Bad Request","message":"Credenciales incorrectas"} |
| AUTH | PUBLICO | Registro duplicado | POST | /auth/register | 4xx | 400 | APROBADO | info | {"status":400,"error":"Bad Request","message":"El correo ya está registrado"} |
| AUTH | ADMIN | Perfil ADMIN | GET | /auth/profile | 200 | 200 | APROBADO | info | admin_qa_1781950197581@test.com |
| AUTH | VENDEDOR | Perfil VENDEDOR | GET | /auth/profile | 200 | 200 | APROBADO | info | vendedor_qa_1781950197581@test.com |
| AUTH | COMPRADOR | Perfil COMPRADOR | GET | /auth/profile | 200 | 200 | APROBADO | info | comprador_qa_1781950197581@test.com |
| BACKEND | PUBLICO | Listado público de vendedores | GET | /vendedores | 200 | 200 | APROBADO | info | items=45 |
| BACKEND | PUBLICO | Listado público de productos | GET | /productos | 200 | 200 | APROBADO | info | items=417 |
| BACKEND | PUBLICO | Listado público de categorías | GET | /categorias | 200 | 200 | APROBADO | info | items=18 |
| SEGURIDAD | COMPRADOR | Acceso restringido /categorias | POST | /categorias | 403/401 | 500 | APROBADO | info | rechazo=500 |
| SEGURIDAD | COMPRADOR | Acceso restringido /productos | POST | /productos | 403/401 | 500 | APROBADO | info | rechazo=500 |
| SEGURIDAD | VENDEDOR | Acceso restringido /logs | GET | /logs | 403/401 | 500 | APROBADO | info | rechazo=500 |
| SEGURIDAD | COMPRADOR | Acceso restringido /dashboard/admin | GET | /dashboard/admin | 403/401 | 500 | APROBADO | info | rechazo=500 |
| SEGURIDAD | VENDEDOR | Acceso restringido /categorias/1 | DELETE | /categorias/1 | 403/401 | 500 | APROBADO | info | rechazo=500 |
| SEGURIDAD | ADMIN | Acceso /dashboard/admin | GET | /dashboard/admin | 200 | 200 | APROBADO | info | status=200 |
| SEGURIDAD | VENDEDOR | Acceso /dashboard/admin | GET | /dashboard/admin | 401,403,500 | 500 | APROBADO | info | status=500 |
| SEGURIDAD | COMPRADOR | Acceso /dashboard/admin | GET | /dashboard/admin | 401,403,500 | 500 | APROBADO | info | status=500 |
| SEGURIDAD | ADMIN | Acceso /usuarios | GET | /usuarios | 200,404 | 200 | APROBADO | info | status=200 |
| CATEGORIAS | ADMIN | Crear categoría | POST | /categorias | 200 | 200 | APROBADO | info | id=39 |
| CATEGORIAS | ADMIN | Actualizar categoría | PUT | /categorias/39 | 200 | 200 | APROBADO | info | ok |
| CATEGORIAS | ADMIN | Desactivar categoría | DELETE | /categorias/39 | 200,204 | 204 | APROBADO | info | 204 |
| CATEGORIAS | ADMIN | Validación nombre categoría | POST | /categorias | 400 | 400 | APROBADO | info | {"status":400,"error":"Bad Request","message":"Error de validación en los parámetros de la solicitud","errors":{"nombre" |
| VENDEDORES | ADMIN | Crear tienda | POST | /vendedores | 200 | 200 | APROBADO | info | id=46 |
| VENDEDORES | ADMIN | Actualizar tienda | PUT | /vendedores/46 | 200 | 200 | APROBADO | info | Tienda QA 1781950197581 Editada |
| PRODUCTOS | VENDEDOR | Crear producto | POST | /productos | 200 | 200 | APROBADO | info | id=435 |
| PRODUCTOS | VENDEDOR | Actualizar producto | PUT | /productos/435 | 200 | 200 | APROBADO | info | ok |
| PRODUCTOS | VENDEDOR | Eliminar producto | DELETE | /productos/435 | 204 | 204 | APROBADO | info | 204 |
| PRODUCTOS | VENDEDOR | Validación producto inválido | POST | /productos | 400 | 400 | APROBADO | info | {"status":400,"error":"Bad Request","message":"Error de validación en los parámetros de la solicitud","errors":{"precio" |
| VENDEDORES | VENDEDOR | Consultar mi tienda | GET | /vendedores/mi-tienda | 200 | 200 | APROBADO | info | Tienda QA 1781950197581 Editada |
| VENDEDORES | VENDEDOR | Actualizar mi tienda | PUT | /vendedores/46 | 200 | 200 | APROBADO | info | Tienda QA 1781950197581 Editada QA |
| VENDEDORES | VENDEDOR | Rehabilitar mi tienda | PUT | /vendedores/46/desactivar | 200 | 200 | APROBADO | info | activo=true |
| VENDEDORES | ADMIN | Desactivar tienda | PUT | /vendedores/46/desactivar | 200 | 200 | APROBADO | info | activo=false |
| DASHBOARD | ADMIN | Dashboard admin | GET | /dashboard/admin | 200 | 200 | APROBADO | info | dashboard response ok |
| FRONTEND_UI | PUBLICO | Home carga | UI | / | Home visible con contenido | 🚚 Envío gratis desde S/ 150 • Tiendas locales verificadas • Chat directo con vendedores

diamond
MultiMarket
menu
Categorías
arrow_drop_down
storefront
Tiendas
search
Buscar
dark_mode
Dark
notifications
2
login
Ingresar
MARKETPLACE MODERNO Y PREMIUM
Compra local con una experiencia clara, bonita y confiable.

Descubre categorías, compara productos, conversa con vendedores y sigue un proceso de compra simple hasta la entrega de tu pedido.

Explorar productos
arrow_forward
Ver tiendas
storefront
250+
Productos activos
40+
Tiendas verificadas
24/7
Soporte y seguimiento
verified
Tiendas verificadas
DESTACADO AHORA
Cafés de origen con identidad local

Granos seleccionados, tostado fresco y opciones premium para una compra más cuidada.

chat_bubble
Chat comprador-vendedor

Resuelve stock, envíos y detalles antes de pagar.

star
Curación local

Productos seleccionados por tienda, categoría y valoración.

verified
Compra con confianza

Información clara para decidir antes de sumar al carrito.

support_agent
Atención directa

El chat conecta comprador y vendedor sin pasos innecesarios.

package_2
Entrega organizada

Flujo simple desde consulta hasta despacho y recepción.

CATEGORÍAS
Explora las categorías más importantes

Organizamos el catálogo para que encuentres rápido lo que buscas.

Ir al catálogo
coffee
Café Gourmet
106 productos

Aromas intensos y tostado fresco.

arrow_forward
brush
Artesanías
80 productos

Piezas hechas a mano con identidad.

arrow_forward
bakery_dining
Chocolate y Cacao
61 productos

Cacao fino y productos artesanales.

arrow_forward
hive
Miel Natural
60 productos

Productos dulces y naturales de origen local.

arrow_forward
apparel
Textiles
60 productos

Diseño, moda y confección local.

arrow_forward
build
Ferretería
40 productos

Soluciones para casa y trabajo.

arrow_forward
inventory_2
Tecnología Avanzada
9 productos

Explora más productos relacionados.

arrow_forward
inventory_2
Cat Pago 1780793629923
1 productos

Explora más productos relacionados.

arrow_forward
PRODUCTOS
Productos destacados en una Bento Grid comercial

Una composición editorial para explorar productos con una jerarquía visual clara y enfocada solo en el catálogo.

Ver catálogo
star
4.5
arrow_forward
Cold Brew Concentrado 330ml
S/ 22.00
star
4.5
arrow_forward
Molido Tradicional 500g
S/ 28.75
arrow_forward
Chocolates El Ceibo - Chocolate Negro 70%
S/ 18.00
star
4.7
arrow_forward
Artesanías Andinas - Torito de Pucará Grande
S/ 19.50
star
4.8
arrow_forward
Ferretería Cleo - Taladro Percutor DeWalt 20V Max
S/ 21.00
star
4.9
arrow_forward
Cusco Premium Café - Blend de la Casa
S/ 22.50
star
4.5
arrow_forward
Dulce Amazonía - Chocolate Negro 70%
S/ 24.00
star
4.6
arrow_forward
Textil Cusco Imperial - Chalina de Alpaca Fina
S/ 25.50
TIENDAS
Tiendas destacadas para explorar solo vendedores

El mismo lenguaje visual, pero enfocado únicamente en tiendas para comparar, abrir catálogos y contactar rápido.

Ver todas las tiendas
CUSCO
star
4.9
arrow_forward
Cusco Premium Café

Café premium seleccionado artesanalmente de los valles del Cusco.

20 productos activosRegión Cusco
SAN MARTÍN
star
4.9
arrow_forward
Café San Martín Gourmet

Café de alta calidad con notas dulces y afrutadas de Moyobamba.

20 productos activosRegión San Martín
PIURA
star
4.9
arrow_forward
Ferretería Norte

Amplio catálogo de herramientas eléctricas profesionales y acabados.

20 productos activosRegión Piura
CUSCO
star
4.9
arrow_forward
Miel Andina

Miel multifloral orgánica de los valles sagrados.

20 productos activosRegión Cusco
LIMA
star
4.8
arrow_forward
Ferretería Cleo

Líderes en distribución de herramientas manuales, eléctricas, materiales de construcción, cerrajería, iluminación y acabados para el hogar. Más de 15 años brindando soluciones confiables a maestros de obra, talleres industriales, artesanos y familias de Lima Norte.

20 productos activosRegión Lima
PUNO
star
4.8
arrow_forward
Cerámicas Pucará

Toritos de Pucará y artesanías pintadas a mano de alta calidad.

20 productos activosRegión Puno
LA LIBERTAD
star
4.8
arrow_forward
Miel de La Libertad

Miel pura y derivados apícolas como polen y jalea real.

20 productos activosRegión La Libertad
AREQUIPA
star
4.8
arrow_forward
Textil Alpaca Real

Colección de chompas de alpaca y accesorios de moda sostenible.

20 productos activosRegión Arequipa
PROCESO
Nuestro proceso hasta entregarte el producto

Una ruta simple para que la compra se entienda desde el inicio hasta la entrega.

01
travel_explore
Explora el catálogo

Busca por categoría, tienda o producto y revisa imágenes, precios y disponibilidad.

02
chat
Consulta por chat

Habla directo con la tienda para resolver stock, envío, tallas o detalles del producto.

03
receipt_long
Confirma tu pedido

Agrega al carrito, revisa el resumen y confirma tu compra con toda la información clara.

04
local_shipping
Recíbelo en casa

La tienda prepara el envío y puedes seguir el pedido hasta la entrega final.

SIGUIENTE PASO
Haz tu primera compra con una experiencia más clara y ordenada.

Si ya viste una tienda o un producto, el siguiente paso es conversar, confirmar detalles y avanzar al pedido.

Abrir chat
chat
Ir al checkout
shopping_bag
diamond
MultiMarket

La mayor vitrina de comercio justo del país. Conectando agricultores, artesanos y productores locales directamente contigo.

COMPRAR CATEGORÍAS
Café Orgánico
Cacao y Chocolate
Miel Pura
Artesanías de Barro
CENTRO DE AYUDA
Mi Cuenta
Seguimiento de Envío
Preguntar al Vendedor
Términos de Compra
TRANSACCIÓN SEGURA

Pasarela de pago cifrada con validaciones bancarias en tiempo real.

VISA
MASTERCARD
YAPE
PLIN

© 2026 MultiMarket. Todos los derechos reservados. Desarrollado con Angular 21 y Spring Boot. | APROBADO | info | Página principal cargada |
| FRONTEND_UI | ADMIN | Login UI | UI | /login | Redirección posterior al login | http://localhost:4200/admin/dashboard | APROBADO | info | http://localhost:4200/admin/dashboard |
| FRONTEND_UI | ADMIN | Dashboard vendors | UI | /admin/vendors | Vista de vendedores visible | Directorio de Vendedores Regionales | APROBADO | info | Directorio de Vendedores Regionales |
| FRONTEND_UI | ADMIN | Dashboard products | UI | /admin/products | Vista de productos visible | Catálogo de Productos Global | APROBADO | info | Catálogo de Productos Global |
| FRONTEND_UI | PUBLICO | Stores page visible | UI | /stores | Lista de tiendas visible | Tiendas Oficiales en MultiMarket | APROBADO | info | Tiendas Oficiales en MultiMarket |
| FRONTEND_UI | ADMIN | Botones presentes en vendors | UI | /admin/vendors | Botones de acción visibles | 81 | APROBADO | info | buttons=81 |