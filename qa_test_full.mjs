// ============================================================================
// MultiMarket - Script QA Exhaustivo de Endpoints
// Ejecutar: node qa_test_full.mjs
// ============================================================================

const BASE = 'http://localhost:8080';
const TS = Date.now(); // Timestamp único para evitar colisiones

// ─── Utilidades HTTP ────────────────────────────────────────────────────────

async function req(method, path, body, token) {
  const url = `${BASE}${path}`;
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const opts = { method, headers };
  if (body && (method !== 'GET')) opts.body = JSON.stringify(body);

  try {
    const res = await fetch(url, opts);
    let data = null;
    const text = await res.text();
    try { data = JSON.parse(text); } catch { data = text; }
    return { status: res.status, ok: res.ok, data };
  } catch (err) {
    return { status: 'CONN_ERROR', ok: false, data: err.message };
  }
}

async function reqForm(method, path, formBody, token) {
  const url = `${BASE}${path}`;
  const headers = {};
  if (token) headers['Authorization'] = `Bearer ${token}`;
  // formBody is a URLSearchParams string
  headers['Content-Type'] = 'application/x-www-form-urlencoded';
  const opts = { method, headers, body: formBody };
  try {
    const res = await fetch(url, opts);
    let data = null;
    const text = await res.text();
    try { data = JSON.parse(text); } catch { data = text; }
    return { status: res.status, ok: res.ok, data };
  } catch (err) {
    return { status: 'CONN_ERROR', ok: false, data: err.message };
  }
}

// ─── Resultados ─────────────────────────────────────────────────────────────

const results = [];

function log(modulo, metodo, endpoint, desc, status, ok, detalle) {
  const estado = ok ? '✅ OK' : '❌ FALLO';
  results.push({ modulo, metodo, endpoint, desc, status, estado, detalle: detalle || '' });
  const icon = ok ? '✅' : '❌';
  console.log(`  ${icon} [${status}] ${metodo} ${endpoint} — ${desc}`);
}

// ============================================================================
// FASE 1: REGISTRO DE USUARIOS
// ============================================================================

async function registrarUsuarios() {
  console.log('\n══════════════════════════════════════════════');
  console.log('  FASE 1: REGISTRO DE USUARIOS DE PRUEBA');
  console.log('══════════════════════════════════════════════\n');

  const usuarios = [
    {
      tag: 'ADMIN',
      body: {
        correo: `admin_qa_${TS}@test.com`,
        password: 'Admin123',
        nombres: 'Admin', apellidos: 'QA Test',
        dni: `A${String(TS).slice(-7)}`,
        telefono: '999000001',
        direccion: 'Av Test Admin 100',
        fechaNacimiento: '1990-01-15',
        roles: ['ADMIN']
      }
    },
    {
      tag: 'VENDEDOR',
      body: {
        correo: `vendedor_qa_${TS}@test.com`,
        password: 'Vend123',
        nombres: 'Vendedor', apellidos: 'QA Test',
        dni: `V${String(TS).slice(-7)}`,
        telefono: '999000002',
        direccion: 'Av Test Vendedor 200',
        fechaNacimiento: '1992-06-20',
        roles: ['VENDEDOR']
      }
    },
    {
      tag: 'COMPRADOR',
      body: {
        correo: `comprador_qa_${TS}@test.com`,
        password: 'Comp123',
        nombres: 'Comprador', apellidos: 'QA Test',
        dni: `C${String(TS).slice(-7)}`,
        telefono: '999000003',
        direccion: 'Av Test Comprador 300',
        fechaNacimiento: '1995-03-10',
        roles: ['COMPRADOR']
      }
    }
  ];

  const tokens = {};

  for (const u of usuarios) {
    // Registrar
    const r = await req('POST', '/auth/register', u.body);
    log('AUTH', 'POST', '/auth/register', `Registro usuario ${u.tag}`, r.status, r.ok,
      r.ok ? '' : JSON.stringify(r.data).slice(0, 120));

    // Login
    const lr = await req('POST', '/auth/login', { correo: u.body.correo, password: u.body.password });
    log('AUTH', 'POST', '/auth/login', `Login usuario ${u.tag}`, lr.status, lr.ok,
      lr.ok ? `Token obtenido (${lr.data?.token?.slice(0, 20)}...)` : JSON.stringify(lr.data).slice(0, 120));

    if (lr.ok && lr.data?.token) {
      tokens[u.tag] = lr.data.token;
    }
  }

  return { tokens, usuarios };
}

// ============================================================================
// FASE 2: TEST AUTH ENDPOINTS
// ============================================================================

async function testAuth(tokens) {
  console.log('\n══════════════════════════════════════════════');
  console.log('  FASE 2: AUTH — PERFIL Y CONTRASEÑAS');
  console.log('══════════════════════════════════════════════\n');

  // GET /auth/profile (con cada rol)
  for (const rol of ['ADMIN', 'VENDEDOR', 'COMPRADOR']) {
    const r = await req('GET', '/auth/profile', null, tokens[rol]);
    log('AUTH', 'GET', '/auth/profile', `Perfil ${rol}`, r.status, r.ok,
      r.ok ? `correo: ${r.data?.correo}` : JSON.stringify(r.data).slice(0, 120));
  }

  // PUT /auth/change-password (con COMPRADOR)
  const cp = await req('PUT', '/auth/change-password', {
    oldPassword: 'Comp123', newPassword: 'Comp456'
  }, tokens['COMPRADOR']);
  log('AUTH', 'PUT', '/auth/change-password', 'Cambiar contraseña COMPRADOR', cp.status, cp.ok,
    cp.ok ? '' : JSON.stringify(cp.data).slice(0, 120));

  // Re-login con nueva contraseña
  if (cp.ok) {
    const rl = await req('POST', '/auth/login', {
      correo: `comprador_qa_${TS}@test.com`, password: 'Comp456'
    });
    log('AUTH', 'POST', '/auth/login', 'Re-login COMPRADOR con nueva password', rl.status, rl.ok);
    if (rl.ok && rl.data?.token) tokens['COMPRADOR'] = rl.data.token;
  }

  // POST /auth/forgot-password
  const fp = await req('POST', '/auth/forgot-password', { correo: `comprador_qa_${TS}@test.com` });
  log('AUTH', 'POST', '/auth/forgot-password', 'Recuperar contraseña', fp.status, fp.ok);

  // POST /auth/reset-password (con token falso — debe fallar)
  const rp = await req('POST', '/auth/reset-password', {
    token: '00000000-0000-0000-0000-000000000000', newPassword: 'Reset123'
  });
  log('AUTH', 'POST', '/auth/reset-password', 'Reset password (token inválido → error esperado)', rp.status, !rp.ok,
    `Esperado error: ${rp.status}`);

  // Acceso sin token a ruta protegida
  const noToken = await req('GET', '/auth/profile', null);
  log('AUTH', 'GET', '/auth/profile', 'Sin token (debe ser 401/403)', noToken.status, !noToken.ok,
    `Esperado rechazo: ${noToken.status}`);
}

// ============================================================================
// FASE 3: TEST CATEGORÍAS (ADMIN)
// ============================================================================

async function testCategorias(tokens) {
  console.log('\n══════════════════════════════════════════════');
  console.log('  FASE 3: CATEGORÍAS (CRUD — ADMIN)');
  console.log('══════════════════════════════════════════════\n');

  let catId = null;

  // POST /categorias (ADMIN)
  const cr = await req('POST', '/categorias', {
    nombre: `Cat QA ${TS}`, descripcion: 'Categoría de prueba QA'
  }, tokens['ADMIN']);
  log('CATEGORÍA', 'POST', '/categorias', 'Crear categoría (ADMIN)', cr.status, cr.ok,
    cr.ok ? `id: ${cr.data?.id}` : JSON.stringify(cr.data).slice(0, 120));
  if (cr.ok) catId = cr.data.id;

  // POST /categorias (VENDEDOR — debe fallar, solo ADMIN)
  const crv = await req('POST', '/categorias', {
    nombre: `Cat Vendedor ${TS}`, descripcion: 'No debería crearse'
  }, tokens['VENDEDOR']);
  log('CATEGORÍA', 'POST', '/categorias', 'Crear categoría (VENDEDOR → debe fallar)', crv.status, !crv.ok,
    `Esperado 403: ${crv.status}`);

  // GET /categorias (público autenticado)
  const ls = await req('GET', '/categorias', null, tokens['COMPRADOR']);
  log('CATEGORÍA', 'GET', '/categorias', 'Listar categorías activas', ls.status, ls.ok,
    ls.ok ? `Total: ${Array.isArray(ls.data) ? ls.data.length : '?'}` : '');

  // GET /categorias/:id
  if (catId) {
    const gid = await req('GET', `/categorias/${catId}`, null, tokens['COMPRADOR']);
    log('CATEGORÍA', 'GET', `/categorias/${catId}`, 'Consultar categoría por ID', gid.status, gid.ok,
      gid.ok ? `nombre: ${gid.data?.nombre}` : '');
  }

  // PUT /categorias/:id (ADMIN)
  if (catId) {
    const up = await req('PUT', `/categorias/${catId}`, {
      nombre: `Cat QA ${TS} Editada`, descripcion: 'Editada por QA'
    }, tokens['ADMIN']);
    log('CATEGORÍA', 'PUT', `/categorias/${catId}`, 'Editar categoría (ADMIN)', up.status, up.ok);
  }

  // DELETE /categorias/:id (ADMIN)
  if (catId) {
    const del = await req('DELETE', `/categorias/${catId}`, null, tokens['ADMIN']);
    log('CATEGORÍA', 'DELETE', `/categorias/${catId}`, 'Desactivar categoría (ADMIN)', del.status,
      del.status === 204 || del.ok);
  }

  // Crear una categoría fresca para usar en productos
  const cat2 = await req('POST', '/categorias', {
    nombre: `Electrónica QA ${TS}`, descripcion: 'Para productos de prueba'
  }, tokens['ADMIN']);
  const catIdFinal = cat2.ok ? cat2.data.id : null;

  return catIdFinal;
}

// ============================================================================
// FASE 4: TEST VENDEDORES / TIENDAS
// ============================================================================

async function testVendedores(tokens) {
  console.log('\n══════════════════════════════════════════════');
  console.log('  FASE 4: VENDEDORES / TIENDAS');
  console.log('══════════════════════════════════════════════\n');

  let vendedorId = null;

  // POST /vendedores (crear tienda — VENDEDOR)
  const cr = await req('POST', '/vendedores', {
    nombreTienda: `Tienda QA ${TS}`,
    descripcion: 'Tienda de pruebas QA automatizadas',
    region: 'Lima',
    direccion: 'Av Prueba 999',
    logo: 'https://example.com/logo.png',
    banner: 'https://example.com/banner.png'
  }, tokens['VENDEDOR']);
  log('VENDEDOR', 'POST', '/vendedores', 'Crear tienda (VENDEDOR)', cr.status, cr.ok,
    cr.ok ? `id: ${cr.data?.id}, nombre: ${cr.data?.nombreTienda}` : JSON.stringify(cr.data).slice(0, 120));
  if (cr.ok) vendedorId = cr.data.id;

  // GET /vendedores/mi-tienda (VENDEDOR)
  const mi = await req('GET', '/vendedores/mi-tienda', null, tokens['VENDEDOR']);
  log('VENDEDOR', 'GET', '/vendedores/mi-tienda', 'Consultar mi tienda (VENDEDOR)', mi.status, mi.ok,
    mi.ok ? `Nombre: ${mi.data?.nombreTienda}` : JSON.stringify(mi.data).slice(0, 120));
  if (mi.ok && !vendedorId) vendedorId = mi.data.id;

  // GET /vendedores/:id (público)
  if (vendedorId) {
    const gid = await req('GET', `/vendedores/${vendedorId}`, null, tokens['COMPRADOR']);
    log('VENDEDOR', 'GET', `/vendedores/${vendedorId}`, 'Consultar tienda por ID (COMPRADOR)', gid.status, gid.ok);
  }

  // PUT /vendedores/:id (editar tienda — VENDEDOR)
  if (vendedorId) {
    const up = await req('PUT', `/vendedores/${vendedorId}`, {
      nombreTienda: `Tienda QA ${TS} Editada`,
      descripcion: 'Editada por prueba QA',
      region: 'Arequipa',
      direccion: 'Av Editada 111'
    }, tokens['VENDEDOR']);
    log('VENDEDOR', 'PUT', `/vendedores/${vendedorId}`, 'Editar tienda (VENDEDOR)', up.status, up.ok);
  }

  return vendedorId;
}

// ============================================================================
// FASE 5: TEST PRODUCTOS (CRUD)
// ============================================================================

async function testProductos(tokens, catId, vendedorId) {
  console.log('\n══════════════════════════════════════════════');
  console.log('  FASE 5: PRODUCTOS (CRUD — VENDEDOR)');
  console.log('══════════════════════════════════════════════\n');

  let productoId = null;

  if (!catId) {
    console.log('  ⚠️  No hay categoría disponible, se intentará con categoriaId=1');
    catId = 1;
  }

  // POST /productos (VENDEDOR)
  const cr = await req('POST', '/productos', {
    nombre: `Producto QA ${TS}`,
    descripcion: 'Producto de prueba automatizado',
    sku: `SKU-QA-${TS}`,
    precio: 99.99,
    stock: 50,
    peso: 1.5,
    categoriaId: catId
  }, tokens['VENDEDOR']);
  log('PRODUCTO', 'POST', '/productos', 'Crear producto (VENDEDOR)', cr.status, cr.ok,
    cr.ok ? `id: ${cr.data?.id}` : JSON.stringify(cr.data).slice(0, 150));
  if (cr.ok) productoId = cr.data.id;

  // POST /productos (COMPRADOR — debe fallar)
  const crc = await req('POST', '/productos', {
    nombre: 'Producto Ilegal', descripcion: 'No debería crearse',
    sku: 'ILEGAL-1', precio: 10, stock: 5, peso: 0.5, categoriaId: catId
  }, tokens['COMPRADOR']);
  log('PRODUCTO', 'POST', '/productos', 'Crear producto (COMPRADOR → debe fallar)', crc.status, !crc.ok,
    `Esperado 403: ${crc.status}`);

  // GET /productos (listar activos — cualquier usuario)
  const ls = await req('GET', '/productos', null, tokens['COMPRADOR']);
  log('PRODUCTO', 'GET', '/productos', 'Listar productos activos', ls.status, ls.ok,
    ls.ok ? `Total: ${Array.isArray(ls.data) ? ls.data.length : '?'}` : '');

  // GET /productos/:id
  if (productoId) {
    const gid = await req('GET', `/productos/${productoId}`, null, tokens['COMPRADOR']);
    log('PRODUCTO', 'GET', `/productos/${productoId}`, 'Consultar producto por ID', gid.status, gid.ok,
      gid.ok ? `nombre: ${gid.data?.nombre}` : '');
  }

  // GET /productos/buscar?nombre=...
  const busq = await req('GET', `/productos/buscar?nombre=QA`, null, tokens['COMPRADOR']);
  log('PRODUCTO', 'GET', '/productos/buscar?nombre=QA', 'Búsqueda por nombre', busq.status, busq.ok,
    busq.ok ? `Resultados: ${Array.isArray(busq.data) ? busq.data.length : '?'}` : '');

  // GET /productos/buscar?categoriaId=...
  if (catId) {
    const busqCat = await req('GET', `/productos/buscar?categoriaId=${catId}`, null, tokens['COMPRADOR']);
    log('PRODUCTO', 'GET', `/productos/buscar?categoriaId=${catId}`, 'Búsqueda por categoría', busqCat.status, busqCat.ok);
  }

  // PUT /productos/:id (VENDEDOR)
  if (productoId) {
    const up = await req('PUT', `/productos/${productoId}`, {
      nombre: `Producto QA ${TS} Editado`,
      descripcion: 'Descripción editada',
      sku: `SKU-QA-${TS}`,
      precio: 149.99,
      stock: 100,
      peso: 2.0,
      categoriaId: catId
    }, tokens['VENDEDOR']);
    log('PRODUCTO', 'PUT', `/productos/${productoId}`, 'Editar producto (VENDEDOR)', up.status, up.ok);
  }

  // POST /productos/:id/imagenes (agregar imagen)
  if (productoId) {
    const params = new URLSearchParams({ url: 'https://example.com/img.png', principal: 'true', orden: '1' });
    const imgr = await reqForm('POST', `/productos/${productoId}/imagenes?${params.toString()}`, '', tokens['VENDEDOR']);
    log('PRODUCTO', 'POST', `/productos/${productoId}/imagenes`, 'Agregar imagen al producto', imgr.status, imgr.ok,
      imgr.ok ? `imagenId: ${imgr.data?.id}` : JSON.stringify(imgr.data).slice(0, 120));
  }

  // Favoritos
  if (productoId) {
    // Agregar favorito
    const fav = await req('POST', `/productos/favoritos/${productoId}`, null, tokens['COMPRADOR']);
    log('PRODUCTO', 'POST', `/productos/favoritos/${productoId}`, 'Agregar producto a favoritos (COMPRADOR)', fav.status, fav.ok);

    // Listar favoritos
    const favls = await req('GET', '/productos/favoritos', null, tokens['COMPRADOR']);
    log('PRODUCTO', 'GET', '/productos/favoritos', 'Listar favoritos (COMPRADOR)', favls.status, favls.ok,
      favls.ok ? `Total: ${Array.isArray(favls.data) ? favls.data.length : '?'}` : '');

    // Eliminar favorito
    const delfav = await req('DELETE', `/productos/favoritos/${productoId}`, null, tokens['COMPRADOR']);
    log('PRODUCTO', 'DELETE', `/productos/favoritos/${productoId}`, 'Eliminar favorito (COMPRADOR)', delfav.status, delfav.ok);
  }

  return productoId;
}

// ============================================================================
// FASE 6: TEST INVENTARIO
// ============================================================================

async function testInventario(tokens, productoId) {
  console.log('\n══════════════════════════════════════════════');
  console.log('  FASE 6: INVENTARIO (VENDEDOR)');
  console.log('══════════════════════════════════════════════\n');

  if (!productoId) {
    console.log('  ⚠️  No hay producto disponible. Saltando pruebas de inventario.');
    log('INVENTARIO', '-', '-', 'Sin producto — SALTEADO', 'N/A', false, 'Producto no creado previamente');
    return;
  }

  // GET /inventarios/productos/:id (consultar stock)
  const gs = await req('GET', `/inventarios/productos/${productoId}`, null, tokens['VENDEDOR']);
  log('INVENTARIO', 'GET', `/inventarios/productos/${productoId}`, 'Consultar stock (VENDEDOR)', gs.status, gs.ok,
    gs.ok ? `stockActual: ${gs.data?.stockActual}, stockMinimo: ${gs.data?.stockMinimo}` : JSON.stringify(gs.data).slice(0, 120));

  // PUT /inventarios/productos/:id/stock-minimo (actualizar stock mínimo)
  const sm = await req('PUT', `/inventarios/productos/${productoId}/stock-minimo`, {
    stockMinimo: 5
  }, tokens['VENDEDOR']);
  log('INVENTARIO', 'PUT', `/inventarios/productos/${productoId}/stock-minimo`, 'Actualizar stock mínimo', sm.status, sm.ok,
    sm.ok ? `stockMinimo: ${sm.data?.stockMinimo}` : JSON.stringify(sm.data).slice(0, 120));

  // POST /inventarios/productos/:id/movimientos (ENTRADA)
  const mov1 = await req('POST', `/inventarios/productos/${productoId}/movimientos`, {
    tipoMovimiento: 'ENTRADA', cantidad: 20, observacion: 'Entrada de prueba QA'
  }, tokens['VENDEDOR']);
  log('INVENTARIO', 'POST', `/inventarios/productos/${productoId}/movimientos`, 'Movimiento ENTRADA', mov1.status, mov1.ok,
    mov1.ok ? `stockActual: ${mov1.data?.stockActual}` : JSON.stringify(mov1.data).slice(0, 120));

  // POST /inventarios/productos/:id/movimientos (SALIDA)
  const mov2 = await req('POST', `/inventarios/productos/${productoId}/movimientos`, {
    tipoMovimiento: 'SALIDA', cantidad: 5, observacion: 'Salida de prueba QA'
  }, tokens['VENDEDOR']);
  log('INVENTARIO', 'POST', `/inventarios/productos/${productoId}/movimientos`, 'Movimiento SALIDA', mov2.status, mov2.ok,
    mov2.ok ? `stockActual: ${mov2.data?.stockActual}` : JSON.stringify(mov2.data).slice(0, 120));

  // POST /inventarios/productos/:id/movimientos (AJUSTE)
  const mov3 = await req('POST', `/inventarios/productos/${productoId}/movimientos`, {
    tipoMovimiento: 'AJUSTE', cantidad: 10, observacion: 'Ajuste de prueba QA'
  }, tokens['VENDEDOR']);
  log('INVENTARIO', 'POST', `/inventarios/productos/${productoId}/movimientos`, 'Movimiento AJUSTE', mov3.status, mov3.ok,
    mov3.ok ? `stockActual: ${mov3.data?.stockActual}` : JSON.stringify(mov3.data).slice(0, 120));

  // GET /inventarios/productos/:id/movimientos (historial)
  const hist = await req('GET', `/inventarios/productos/${productoId}/movimientos`, null, tokens['VENDEDOR']);
  log('INVENTARIO', 'GET', `/inventarios/productos/${productoId}/movimientos`, 'Historial movimientos', hist.status, hist.ok,
    hist.ok ? `Total movimientos: ${Array.isArray(hist.data) ? hist.data.length : '?'}` : '');

  // Intento de acceso cruzado: COMPRADOR no debería acceder al inventario
  const gsComp = await req('GET', `/inventarios/productos/${productoId}`, null, tokens['COMPRADOR']);
  log('INVENTARIO', 'GET', `/inventarios/productos/${productoId}`, 'Consultar stock (COMPRADOR → debe fallar)', gsComp.status, !gsComp.ok,
    `Esperado 403: ${gsComp.status}`);
}

// ============================================================================
// FASE 7: TEST PEDIDOS
// ============================================================================

async function testPedidos(tokens, productoId, vendedorId) {
  console.log('\n══════════════════════════════════════════════');
  console.log('  FASE 7: PEDIDOS (COMPRADOR / VENDEDOR)');
  console.log('══════════════════════════════════════════════\n');

  let pedidoId = null;

  if (!productoId || !vendedorId) {
    console.log('  ⚠️  Sin producto/vendedor — saltando pedidos.');
    log('PEDIDO', '-', '-', 'Sin producto/vendedor — SALTEADO', 'N/A', false, '');
    return null;
  }

  // POST /pedidos (COMPRADOR crea pedido)
  const cr = await req('POST', '/pedidos', {
    vendedorId: vendedorId,
    costoEnvio: 10.00,
    detalles: [{ productoId: productoId, cantidad: 2 }]
  }, tokens['COMPRADOR']);
  log('PEDIDO', 'POST', '/pedidos', 'Crear pedido (COMPRADOR)', cr.status, cr.ok,
    cr.ok ? `pedidoId: ${cr.data?.id}, total: ${cr.data?.total}` : JSON.stringify(cr.data).slice(0, 150));
  if (cr.ok) pedidoId = cr.data.id;

  // GET /pedidos/mis-pedidos (COMPRADOR)
  const mp = await req('GET', '/pedidos/mis-pedidos', null, tokens['COMPRADOR']);
  log('PEDIDO', 'GET', '/pedidos/mis-pedidos', 'Listar mis pedidos (COMPRADOR)', mp.status, mp.ok,
    mp.ok ? `Total: ${Array.isArray(mp.data) ? mp.data.length : '?'}` : '');

  // GET /pedidos/:id (COMPRADOR consulta su pedido)
  if (pedidoId) {
    const gid = await req('GET', `/pedidos/${pedidoId}`, null, tokens['COMPRADOR']);
    log('PEDIDO', 'GET', `/pedidos/${pedidoId}`, 'Consultar pedido por ID (COMPRADOR)', gid.status, gid.ok,
      gid.ok ? `estado: ${gid.data?.estado}` : '');
  }

  // GET /pedidos/tienda (VENDEDOR lista pedidos de su tienda)
  const pt = await req('GET', '/pedidos/tienda', null, tokens['VENDEDOR']);
  log('PEDIDO', 'GET', '/pedidos/tienda', 'Listar pedidos de tienda (VENDEDOR)', pt.status, pt.ok,
    pt.ok ? `Total: ${Array.isArray(pt.data) ? pt.data.length : '?'}` : '');

  // PUT /pedidos/:id/estado (VENDEDOR actualiza estado → PAGADO)
  // Creamos un pedido temporal para no alterar el estado de pedidoId (que se usará en FASE 8 para testear el flujo de pago)
  const tempOrderReq = await req('POST', '/pedidos', {
    vendedorId: vendedorId,
    costoEnvio: 3.00,
    detalles: [{ productoId: productoId, cantidad: 1 }]
  }, tokens['COMPRADOR']);
  const tempOrderId = tempOrderReq.ok ? tempOrderReq.data.id : null;

  if (tempOrderId) {
    const ue = await req('PUT', `/pedidos/${tempOrderId}/estado`, {
      estado: 'PAGADO'
    }, tokens['VENDEDOR']);
    log('PEDIDO', 'PUT', `/pedidos/${tempOrderId}/estado`, 'Actualizar estado a PAGADO (VENDEDOR)', ue.status, ue.ok,
      ue.ok ? `nuevoEstado: ${ue.data?.estado}` : JSON.stringify(ue.data).slice(0, 120));
  }

  // Crear segundo pedido para probar cancelación
  const cr2 = await req('POST', '/pedidos', {
    vendedorId: vendedorId,
    costoEnvio: 5.00,
    detalles: [{ productoId: productoId, cantidad: 1 }]
  }, tokens['COMPRADOR']);
  let pedido2Id = cr2.ok ? cr2.data.id : null;

  // PUT /pedidos/:id/cancelar (COMPRADOR cancela)
  if (pedido2Id) {
    const ca = await req('PUT', `/pedidos/${pedido2Id}/cancelar`, null, tokens['COMPRADOR']);
    log('PEDIDO', 'PUT', `/pedidos/${pedido2Id}/cancelar`, 'Cancelar pedido (COMPRADOR)', ca.status, ca.ok,
      ca.ok ? `estado: ${ca.data?.estado}` : JSON.stringify(ca.data).slice(0, 120));
  }

  return pedidoId;
}

// ============================================================================
// FASE 8: TEST PAGOS
// ============================================================================

async function testPagos(tokens, pedidoId) {
  console.log('\n══════════════════════════════════════════════');
  console.log('  FASE 8: PAGOS');
  console.log('══════════════════════════════════════════════\n');

  if (!pedidoId) {
    console.log('  ⚠️  Sin pedido disponible — saltando pagos.');
    log('PAGO', '-', '-', 'Sin pedido — SALTEADO', 'N/A', false, '');
    return;
  }

  // POST /pagos (COMPRADOR procesa pago con VISA)
  const pg = await req('POST', '/pagos', {
    pedidoId: pedidoId,
    metodoPago: 'VISA',
    numeroTarjeta: '4111111111111111',
    cvv: '123',
    fechaExpiracion: '12/2028'
  }, tokens['COMPRADOR']);
  log('PAGO', 'POST', '/pagos', 'Procesar pago VISA (COMPRADOR)', pg.status, pg.ok,
    pg.ok ? `pagoId: ${pg.data?.id}, estado: ${pg.data?.estadoPago}` : JSON.stringify(pg.data).slice(0, 150));

  const pagoId = pg.ok ? pg.data.id : null;

  // GET /pagos/:id (consultar pago)
  if (pagoId) {
    const gp = await req('GET', `/pagos/${pagoId}`, null, tokens['COMPRADOR']);
    log('PAGO', 'GET', `/pagos/${pagoId}`, 'Consultar pago por ID', gp.status, gp.ok,
      gp.ok ? `monto: ${gp.data?.monto}, metodo: ${gp.data?.metodoPago}` : '');
  }
}

// ============================================================================
// FASE 9: TEST CHAT / CONVERSACIONES
// ============================================================================

async function testChat(tokens, vendedorId) {
  console.log('\n══════════════════════════════════════════════');
  console.log('  FASE 9: CHAT / CONVERSACIONES');
  console.log('══════════════════════════════════════════════\n');

  let convId = null;

  if (!vendedorId) {
    console.log('  ⚠️  Sin vendedor — saltando chat.');
    log('CHAT', '-', '-', 'Sin vendedor — SALTEADO', 'N/A', false, '');
    return;
  }

  // POST /chat/conversaciones (COMPRADOR inicia conversación con vendedor)
  const cr = await req('POST', '/chat/conversaciones', {
    vendedorId: vendedorId
  }, tokens['COMPRADOR']);
  log('CHAT', 'POST', '/chat/conversaciones', 'Crear conversación (COMPRADOR)', cr.status, cr.ok,
    cr.ok ? `convId: ${cr.data?.id}` : JSON.stringify(cr.data).slice(0, 120));
  if (cr.ok) convId = cr.data.id;

  // GET /chat/conversaciones (listar conversaciones — COMPRADOR)
  const ls = await req('GET', '/chat/conversaciones', null, tokens['COMPRADOR']);
  log('CHAT', 'GET', '/chat/conversaciones', 'Listar conversaciones (COMPRADOR)', ls.status, ls.ok,
    ls.ok ? `Total: ${Array.isArray(ls.data) ? ls.data.length : '?'}` : '');

  // GET /chat/conversaciones (listar conversaciones — VENDEDOR)
  const lsv = await req('GET', '/chat/conversaciones', null, tokens['VENDEDOR']);
  log('CHAT', 'GET', '/chat/conversaciones', 'Listar conversaciones (VENDEDOR)', lsv.status, lsv.ok,
    lsv.ok ? `Total: ${Array.isArray(lsv.data) ? lsv.data.length : '?'}` : '');

  if (convId) {
    // POST /chat/conversaciones/:id/mensajes (COMPRADOR envía mensaje)
    const msg1 = await req('POST', `/chat/conversaciones/${convId}/mensajes`, {
      contenido: '¡Hola! Estoy interesado en un producto. ¿Está disponible?'
    }, tokens['COMPRADOR']);
    log('CHAT', 'POST', `/chat/conversaciones/${convId}/mensajes`, 'Enviar mensaje (COMPRADOR)', msg1.status, msg1.ok,
      msg1.ok ? `msgId: ${msg1.data?.id}` : JSON.stringify(msg1.data).slice(0, 120));

    // POST /chat/conversaciones/:id/mensajes (VENDEDOR responde)
    const msg2 = await req('POST', `/chat/conversaciones/${convId}/mensajes`, {
      contenido: '¡Hola! Sí, el producto está disponible. ¿Cuántas unidades necesitas?'
    }, tokens['VENDEDOR']);
    log('CHAT', 'POST', `/chat/conversaciones/${convId}/mensajes`, 'Enviar mensaje (VENDEDOR)', msg2.status, msg2.ok);

    // GET /chat/conversaciones/:id/mensajes (historial)
    const hist = await req('GET', `/chat/conversaciones/${convId}/mensajes`, null, tokens['COMPRADOR']);
    log('CHAT', 'GET', `/chat/conversaciones/${convId}/mensajes`, 'Obtener historial de mensajes', hist.status, hist.ok,
      hist.ok ? `Total mensajes: ${Array.isArray(hist.data) ? hist.data.length : '?'}` : '');
  }
}

// ============================================================================
// FASE 10: TEST NOTIFICACIONES
// ============================================================================

async function testNotificaciones(tokens) {
  console.log('\n══════════════════════════════════════════════');
  console.log('  FASE 10: NOTIFICACIONES');
  console.log('══════════════════════════════════════════════\n');

  // GET /notificaciones (historial del COMPRADOR)
  const ls = await req('GET', '/notificaciones', null, tokens['COMPRADOR']);
  log('NOTIFICACIÓN', 'GET', '/notificaciones', 'Historial notificaciones (COMPRADOR)', ls.status, ls.ok,
    ls.ok ? `Total: ${Array.isArray(ls.data) ? ls.data.length : '?'}` : '');

  // GET /notificaciones (historial del VENDEDOR)
  const lsv = await req('GET', '/notificaciones', null, tokens['VENDEDOR']);
  log('NOTIFICACIÓN', 'GET', '/notificaciones', 'Historial notificaciones (VENDEDOR)', lsv.status, lsv.ok,
    lsv.ok ? `Total: ${Array.isArray(lsv.data) ? lsv.data.length : '?'}` : '');

  // POST /notificaciones/test (crear notificación de prueba)
  const profile = await req('GET', '/auth/profile', null, tokens['COMPRADOR']);
  const userId = profile.ok ? profile.data?.id : 1;
  const params = new URLSearchParams({
    usuarioId: String(userId), titulo: 'Test QA', mensaje: 'Notificación de prueba QA', tipo: 'SISTEMA'
  });
  const nt = await reqForm('POST', `/notificaciones/test?${params.toString()}`, '', tokens['COMPRADOR']);
  log('NOTIFICACIÓN', 'POST', '/notificaciones/test', 'Crear notificación test', nt.status, nt.ok,
    nt.ok ? `id: ${nt.data?.id}` : JSON.stringify(nt.data).slice(0, 120));

  const notiId = nt.ok ? nt.data.id : null;

  // PUT /notificaciones/:id/leer
  if (notiId) {
    const mr = await req('PUT', `/notificaciones/${notiId}/leer`, null, tokens['COMPRADOR']);
    log('NOTIFICACIÓN', 'PUT', `/notificaciones/${notiId}/leer`, 'Marcar como leída', mr.status, mr.ok);
  }
}

// ============================================================================
// FASE 11: TEST LOGS (ADMIN)
// ============================================================================

async function testLogs(tokens) {
  console.log('\n══════════════════════════════════════════════');
  console.log('  FASE 11: LOGS / AUDITORÍA (ADMIN)');
  console.log('══════════════════════════════════════════════\n');

  // GET /logs (ADMIN)
  const ls = await req('GET', '/logs', null, tokens['ADMIN']);
  log('LOG', 'GET', '/logs', 'Listar logs (ADMIN)', ls.status, ls.ok,
    ls.ok ? `Total: ${Array.isArray(ls.data) ? ls.data.length : '?'}` : JSON.stringify(ls.data).slice(0, 120));

  // GET /logs (VENDEDOR — debe fallar)
  const lsv = await req('GET', '/logs', null, tokens['VENDEDOR']);
  log('LOG', 'GET', '/logs', 'Listar logs (VENDEDOR → debe fallar)', lsv.status, !lsv.ok,
    `Esperado 403: ${lsv.status}`);

  // GET /logs/filtrar?nivel=INFO
  const fi = await req('GET', '/logs/filtrar?nivel=INFO', null, tokens['ADMIN']);
  log('LOG', 'GET', '/logs/filtrar?nivel=INFO', 'Filtrar logs por nivel INFO (ADMIN)', fi.status, fi.ok,
    fi.ok ? `Total: ${Array.isArray(fi.data) ? fi.data.length : '?'}` : '');

  // GET /logs/filtrar?modulo=AUTH
  const fm = await req('GET', '/logs/filtrar?modulo=AUTH', null, tokens['ADMIN']);
  log('LOG', 'GET', '/logs/filtrar?modulo=AUTH', 'Filtrar logs por módulo AUTH (ADMIN)', fm.status, fm.ok);
}

// ============================================================================
// FASE 12: TEST EXPORTACIÓN / IMPORTACIÓN
// ============================================================================

async function testExportImport(tokens) {
  console.log('\n══════════════════════════════════════════════');
  console.log('  FASE 12: EXPORTACIÓN / IMPORTACIÓN');
  console.log('══════════════════════════════════════════════\n');

  // POST /exportar?formato=JSON (VENDEDOR)
  const params1 = new URLSearchParams({ formato: 'JSON' });
  const ej = await reqForm('POST', `/exportar?${params1.toString()}`, '', tokens['VENDEDOR']);
  log('EXPORTACIÓN', 'POST', '/exportar?formato=JSON', 'Exportar catálogo JSON (VENDEDOR)', ej.status, ej.ok,
    ej.ok ? `id: ${ej.data?.id}, estado: ${ej.data?.estado}` : JSON.stringify(ej.data).slice(0, 120));

  // POST /exportar?formato=XML (ADMIN)
  const params2 = new URLSearchParams({ formato: 'XML' });
  const ex = await reqForm('POST', `/exportar?${params2.toString()}`, '', tokens['ADMIN']);
  log('EXPORTACIÓN', 'POST', '/exportar?formato=XML', 'Exportar catálogo XML (ADMIN)', ex.status, ex.ok,
    ex.ok ? `id: ${ex.data?.id}, estado: ${ex.data?.estado}` : JSON.stringify(ex.data).slice(0, 120));

  // POST /exportar (COMPRADOR — debe fallar)
  const params3 = new URLSearchParams({ formato: 'JSON' });
  const ec = await reqForm('POST', `/exportar?${params3.toString()}`, '', tokens['COMPRADOR']);
  log('EXPORTACIÓN', 'POST', '/exportar?formato=JSON', 'Exportar catálogo (COMPRADOR → debe fallar)', ec.status, !ec.ok,
    `Esperado 403: ${ec.status}`);

  // POST /importar (sin archivo — debe dar error de validación)
  const imp = await req('POST', '/importar', {}, tokens['VENDEDOR']);
  log('IMPORTACIÓN', 'POST', '/importar', 'Importar sin archivo (error esperado)', imp.status, !imp.ok,
    `Error esperado: ${imp.status}`);
}

// ============================================================================
// FASE 13: TEST DESACTIVACIÓN DE PRODUCTO
// ============================================================================

async function testDesactivarProducto(tokens, productoId) {
  console.log('\n══════════════════════════════════════════════');
  console.log('  FASE 13: DESACTIVAR PRODUCTO');
  console.log('══════════════════════════════════════════════\n');

  if (!productoId) {
    log('PRODUCTO', 'DELETE', '/productos/:id', 'Sin producto — SALTEADO', 'N/A', false, '');
    return;
  }

  const del = await req('DELETE', `/productos/${productoId}`, null, tokens['VENDEDOR']);
  log('PRODUCTO', 'DELETE', `/productos/${productoId}`, 'Desactivar producto (VENDEDOR)', del.status,
    del.status === 204 || del.ok);

  // Intentar desactivar con COMPRADOR — debe fallar
  const delc = await req('DELETE', `/productos/${productoId}`, null, tokens['COMPRADOR']);
  log('PRODUCTO', 'DELETE', `/productos/${productoId}`, 'Desactivar producto (COMPRADOR → debe fallar)', delc.status, !delc.ok,
    `Esperado 403: ${delc.status}`);
}

// ============================================================================
// GENERADOR DE REPORTE
// ============================================================================

function generarReporte() {
  console.log('\n');
  console.log('╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════╗');
  console.log('║                           REPORTE FINAL QA — MultiMarket                                                  ║');
  console.log('╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════╝');
  console.log('');

  const ok = results.filter(r => r.estado === '✅ OK').length;
  const fail = results.filter(r => r.estado === '❌ FALLO').length;
  const total = results.length;

  console.log(`  Total pruebas: ${total}  |  ✅ Exitosas: ${ok}  |  ❌ Fallidas: ${fail}  |  Tasa de éxito: ${((ok / total) * 100).toFixed(1)}%`);
  console.log('');

  // Tabla agrupada por módulo
  const modulos = [...new Set(results.map(r => r.modulo))];
  for (const mod of modulos) {
    console.log(`  ── ${mod} ${'─'.repeat(80 - mod.length)}`);
    const rows = results.filter(r => r.modulo === mod);
    for (const r of rows) {
      const line = `    ${r.estado}  ${r.metodo.padEnd(7)} ${r.endpoint.padEnd(48)} ${r.desc}`;
      console.log(line);
      if (r.detalle) console.log(`           └─ ${r.detalle}`);
    }
    console.log('');
  }

  // JSON output para el reporte Markdown
  return { total, ok, fail, results };
}

// ============================================================================
// MAIN
// ============================================================================

async function main() {
  console.log('');
  console.log('╔══════════════════════════════════════════════════════════════════════════════════════════════════════════════╗');
  console.log('║     MultiMarket — Script QA Exhaustivo de Endpoints y Funcionalidades                                     ║');
  console.log('║     Fecha: ' + new Date().toISOString() + '                                                               ║');
  console.log('╚══════════════════════════════════════════════════════════════════════════════════════════════════════════════╝');

  // Verificar conectividad
  try {
    const ping = await req('POST', '/auth/login', { correo: 'x', password: 'x' });
    console.log(`\n  ✅ Backend accesible en ${BASE} (respuesta: ${ping.status})\n`);
  } catch {
    console.log(`\n  ❌ Backend NO accesible en ${BASE}. Abortando.\n`);
    process.exit(1);
  }

  // Ejecutar todas las fases
  const { tokens } = await registrarUsuarios();

  if (!tokens['ADMIN'] || !tokens['VENDEDOR'] || !tokens['COMPRADOR']) {
    console.log('\n  ❌ No se pudieron obtener tokens para los 3 roles. Abortando pruebas avanzadas.');
    console.log('     Tokens disponibles:', Object.keys(tokens).join(', '));
    generarReporte();
    process.exit(1);
  }

  await testAuth(tokens);
  const catId = await testCategorias(tokens);
  const vendedorId = await testVendedores(tokens);
  const productoId = await testProductos(tokens, catId, vendedorId);
  await testInventario(tokens, productoId);
  const pedidoId = await testPedidos(tokens, productoId, vendedorId);
  await testPagos(tokens, pedidoId);
  await testChat(tokens, vendedorId);
  await testNotificaciones(tokens);
  await testLogs(tokens);
  await testExportImport(tokens);
  await testDesactivarProducto(tokens, productoId);

  const report = generarReporte();

  // Guardar JSON de resultados
  const fs = await import('fs');
  fs.writeFileSync('qa_results.json', JSON.stringify(report, null, 2), 'utf-8');
  console.log('\n  📄 Resultados guardados en qa_results.json\n');
}

main().catch(err => {
  console.error('Error fatal:', err);
  process.exit(1);
});
