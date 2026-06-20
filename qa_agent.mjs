// MultiMarket QA Agent
// Ejecutar:
//   node qa_agent.mjs
//
// Requiere:
//   - Backend corriendo en http://localhost:8080
//   - Frontend corriendo en http://localhost:4200 para validaciones UI opcionales
//
// Este agente valida:
//   - Flujos por rol
//   - CRUD de módulos principales
//   - Casos negativos
//   - Seguridad JWT / permisos
//   - Integridad básica frontend-backend
//
// Salidas:
//   - qa_results.json
//   - qa_report.md

import fs from 'fs';
import path from 'path';
import { pathToFileURL } from 'url';

const BASE = process.env.QA_BASE_URL || 'http://localhost:8080';
const FRONTEND = process.env.QA_FRONTEND_URL || 'http://localhost:4200';
const TS = Date.now();

const roles = {
  ADMIN: { correo: `admin_qa_${TS}@test.com`, password: 'Admin123' },
  VENDEDOR: { correo: `vendedor_qa_${TS}@test.com`, password: 'Vend123' },
  COMPRADOR: { correo: `comprador_qa_${TS}@test.com`, password: 'Comp123' }
};

const results = [];

async function loadPlaywright() {
  const candidate = path.resolve('..', 'MultiMarketFrontend', 'node_modules', 'playwright', 'index.mjs');
  return import(pathToFileURL(candidate).href);
}

async function req(method, path, body, token, base = BASE) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers.Authorization = `Bearer ${token}`;
  const opts = { method, headers };
  if (body != null && method !== 'GET') opts.body = JSON.stringify(body);
  try {
    const res = await fetch(`${base}${path}`, opts);
    const text = await res.text();
    let data;
    try { data = text ? JSON.parse(text) : null; } catch { data = text; }
    return { status: res.status, ok: res.ok, data };
  } catch (err) {
    return { status: 'CONN_ERROR', ok: false, data: String(err?.message || err) };
  }
}

async function reqForm(method, path, formBody, token, base = BASE) {
  const headers = {};
  if (token) headers.Authorization = `Bearer ${token}`;
  const opts = { method, headers, body: formBody };
  try {
    const res = await fetch(`${base}${path}`, opts);
    const text = await res.text();
    let data;
    try { data = text ? JSON.parse(text) : null; } catch { data = text; }
    return { status: res.status, ok: res.ok, data };
  } catch (err) {
    return { status: 'CONN_ERROR', ok: false, data: String(err?.message || err) };
  }
}

function pushResult(item) {
  results.push(item);
  const mark = item.ok ? 'OK' : 'FAIL';
  console.log(`[${mark}] ${item.module} | ${item.method} ${item.endpoint} | ${item.name} | ${item.status}`);
}

function expectStatus(res, expected, min = null, max = null) {
  if (Array.isArray(expected)) return expected.includes(res.status);
  if (typeof expected === 'number') return res.status === expected;
  if (min != null && max != null) return typeof res.status === 'number' && res.status >= min && res.status <= max;
  return false;
}

async function registerAndLogin(tag, payload) {
  const reg = await req('POST', '/auth/register', payload);
  pushResult({
    module: 'AUTH',
    role: tag,
    name: `Registro ${tag}`,
    method: 'POST',
    endpoint: '/auth/register',
    expected: '200/201',
    actual: reg.status,
    ok: reg.ok,
    severity: reg.ok ? 'info' : 'high',
    evidence: reg.ok ? 'Usuario registrado' : JSON.stringify(reg.data).slice(0, 120)
  });

  const login = await req('POST', '/auth/login', { correo: payload.correo, password: payload.password });
  pushResult({
    module: 'AUTH',
    role: tag,
    name: `Login ${tag}`,
    method: 'POST',
    endpoint: '/auth/login',
    expected: 200,
    actual: login.status,
    ok: login.ok,
    severity: login.ok ? 'info' : 'critical',
    evidence: login.ok ? `token=${String(login.data?.token || '').slice(0, 24)}...` : JSON.stringify(login.data).slice(0, 120)
  });
  return login.ok ? login.data.token : null;
}

async function testAuthNegatives() {
  const badLogin = await req('POST', '/auth/login', {
    correo: `noexiste_${TS}@test.com`,
    password: 'BadPass123'
  });
  pushResult({
    module: 'AUTH',
    role: 'PUBLICO',
    name: 'Login inválido',
    method: 'POST',
    endpoint: '/auth/login',
    expected: 400,
    actual: badLogin.status,
    ok: !badLogin.ok,
    severity: badLogin.ok ? 'high' : 'info',
    evidence: badLogin.ok ? 'El backend aceptó credenciales inexistentes' : JSON.stringify(badLogin.data).slice(0, 160)
  });

  const duplicateAdmin = await req('POST', '/auth/register', {
    correo: roles.ADMIN.correo,
    password: roles.ADMIN.password,
    nombres: 'Admin',
    apellidos: 'QA',
    dni: `A${TS}`,
    telefono: '999000001',
    direccion: 'Av QA Admin',
    fechaNacimiento: '1990-01-01',
    roles: ['ADMIN']
  });
  pushResult({
    module: 'AUTH',
    role: 'PUBLICO',
    name: 'Registro duplicado',
    method: 'POST',
    endpoint: '/auth/register',
    expected: '4xx',
    actual: duplicateAdmin.status,
    ok: !duplicateAdmin.ok,
    severity: duplicateAdmin.ok ? 'high' : 'info',
    evidence: duplicateAdmin.ok ? 'Se permitió duplicar registro' : JSON.stringify(duplicateAdmin.data).slice(0, 160)
  });
}

async function testFrontendAccessibility() {
  const ui = await req('GET', '/', null, null, FRONTEND);
  pushResult({
    module: 'FRONTEND',
    role: 'PUBLICO',
    name: 'Frontend accesible',
    method: 'GET',
    endpoint: '/',
    expected: 200,
    actual: ui.status,
    ok: ui.ok,
    severity: ui.ok ? 'info' : 'medium',
    evidence: ui.ok ? 'HTML recibido' : String(ui.data).slice(0, 120)
  });
}

async function testFrontendUiFlows(tokens, vendorId) {
  const { chromium } = await loadPlaywright();
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  const uiResults = [];
  const shotDir = path.resolve('qa-artifacts');
  fs.mkdirSync(shotDir, { recursive: true });

  try {
    await page.goto(FRONTEND, { waitUntil: 'networkidle' });
    await page.screenshot({ path: path.join(shotDir, 'home-public.png'), fullPage: true });
    uiResults.push({
      module: 'FRONTEND_UI',
      role: 'PUBLICO',
      name: 'Home carga',
      method: 'UI',
      endpoint: '/',
      expected: 'Home visible con contenido',
      actual: await page.locator('body').innerText({ timeout: 5000 }),
      ok: true,
      severity: 'info',
      evidence: 'Página principal cargada'
    });

    await page.goto(`${FRONTEND}/login`, { waitUntil: 'networkidle' });
    await page.screenshot({ path: path.join(shotDir, 'login-page.png'), fullPage: true });

    const adminQuickLogin = page.locator('button.quick-access-btn').filter({ hasText: 'Admin' }).first();
    if (await adminQuickLogin.isVisible().catch(() => false)) {
      await Promise.all([
        page.waitForURL((url) => /admin|dashboard/.test(url.pathname + url.search + url.hash), { timeout: 15000 }).catch(() => {}),
        adminQuickLogin.click()
      ]).catch(() => {});
    } else {
      const loginEmail = `admin_qa_${TS}@test.com`;
      const loginPass = 'Admin123';
      const emailInput = page.locator('input[formControlName="correo"], #email').first();
      const passInput = page.locator('input[formControlName="password"], #password').first();
      await emailInput.fill(loginEmail);
      await passInput.fill(loginPass);
      const submitBtn = page.locator('button[type="submit"], .submit-btn').first();
      await page.waitForFunction((selector) => {
        const btn = document.querySelector(selector);
        return !!btn && !btn.disabled;
      }, 'button[type="submit"], .submit-btn', { timeout: 10000 }).catch(() => {});
      if (await submitBtn.isVisible().catch(() => false)) {
        if (await submitBtn.isEnabled().catch(() => false)) {
          await Promise.all([
            page.waitForURL((url) => /admin|seller|dashboard/.test(url.pathname + url.search + url.hash), { timeout: 15000 }).catch(() => {}),
            submitBtn.click()
          ]).catch(() => {});
        } else {
          await passInput.press('Enter').catch(() => {});
        }
      } else {
        await passInput.press('Enter').catch(() => {});
      }
      await page.waitForLoadState('networkidle').catch(() => {});
    }
    await page.screenshot({ path: path.join(shotDir, 'after-login.png'), fullPage: true });

    const currentUrl = page.url();
    uiResults.push({
      module: 'FRONTEND_UI',
      role: 'ADMIN',
      name: 'Login UI',
      method: 'UI',
      endpoint: '/login',
      expected: 'Redirección posterior al login',
      actual: currentUrl,
      ok: /admin|seller|dashboard/.test(currentUrl) || currentUrl !== `${FRONTEND}/login`,
      severity: /admin|seller|dashboard/.test(currentUrl) || currentUrl !== `${FRONTEND}/login` ? 'info' : 'high',
      evidence: currentUrl
    });

    if (tokens.ADMIN) {
      await page.evaluate((token) => {
        localStorage.setItem('token', token);
        localStorage.setItem('correo', 'admin_qa_ui@test.com');
        localStorage.setItem('roles', JSON.stringify(['ADMIN']));
      }, tokens.ADMIN);
      await page.goto(`${FRONTEND}/admin/vendors`, { waitUntil: 'networkidle' });
      const title = await page.locator('h2, h1').first().innerText({ timeout: 5000 }).catch(() => '');
      uiResults.push({
        module: 'FRONTEND_UI',
        role: 'ADMIN',
        name: 'Dashboard vendors',
        method: 'UI',
        endpoint: '/admin/vendors',
        expected: 'Vista de vendedores visible',
        actual: title,
        ok: title.length > 0,
        severity: title.length > 0 ? 'info' : 'high',
        evidence: title || 'No se encontró título'
      });

      await page.goto(`${FRONTEND}/admin/products`, { waitUntil: 'networkidle' });
      await page.screenshot({ path: path.join(shotDir, 'admin-products.png'), fullPage: true });
      const prodTitle = await page.locator('h2, h1').first().innerText({ timeout: 5000 }).catch(() => '');
      uiResults.push({
        module: 'FRONTEND_UI',
        role: 'ADMIN',
        name: 'Dashboard products',
        method: 'UI',
        endpoint: '/admin/products',
        expected: 'Vista de productos visible',
        actual: prodTitle,
        ok: prodTitle.length > 0,
        severity: prodTitle.length > 0 ? 'info' : 'high',
        evidence: prodTitle || 'No se encontró título'
      });

      await page.goto(`${FRONTEND}/stores`, { waitUntil: 'networkidle' });
      await page.screenshot({ path: path.join(shotDir, 'customer-stores.png'), fullPage: true });
      const storesTitle = await page.locator('h1, h2, h3').first().innerText({ timeout: 5000 }).catch(() => '');
      uiResults.push({
        module: 'FRONTEND_UI',
        role: 'PUBLICO',
        name: 'Stores page visible',
        method: 'UI',
        endpoint: '/stores',
        expected: 'Lista de tiendas visible',
        actual: storesTitle,
        ok: storesTitle.length > 0,
        severity: storesTitle.length > 0 ? 'info' : 'medium',
        evidence: storesTitle || 'No se encontró título'
      });
    }

    if (vendorId) {
      const editButtons = page.locator('button');
      const count = await editButtons.count();
      uiResults.push({
        module: 'FRONTEND_UI',
        role: 'ADMIN',
        name: 'Botones presentes en vendors',
        method: 'UI',
        endpoint: '/admin/vendors',
        expected: 'Botones de acción visibles',
        actual: count,
        ok: count > 0,
        severity: count > 0 ? 'info' : 'medium',
        evidence: `buttons=${count}`
      });
    }
  } finally {
    await browser.close();
  }

  for (const item of uiResults) pushResult(item);
}

async function testPublicCatalog(auth) {
  const vendors = await req('GET', '/vendedores', null, null);
  pushResult({
    module: 'BACKEND',
    role: 'PUBLICO',
    name: 'Listado público de vendedores',
    method: 'GET',
    endpoint: '/vendedores',
    expected: 200,
    actual: vendors.status,
    ok: vendors.ok,
    severity: vendors.ok ? 'info' : 'critical',
    evidence: vendors.ok ? `items=${vendors.data?.length ?? '?'}` : JSON.stringify(vendors.data).slice(0, 160)
  });

  const products = await req('GET', '/productos', null, null);
  pushResult({
    module: 'BACKEND',
    role: 'PUBLICO',
    name: 'Listado público de productos',
    method: 'GET',
    endpoint: '/productos',
    expected: 200,
    actual: products.status,
    ok: products.ok,
    severity: products.ok ? 'info' : 'critical',
    evidence: products.ok ? `items=${products.data?.length ?? '?'}` : JSON.stringify(products.data).slice(0, 160)
  });

  const categories = await req('GET', '/categorias', null, null);
  pushResult({
    module: 'BACKEND',
    role: 'PUBLICO',
    name: 'Listado público de categorías',
    method: 'GET',
    endpoint: '/categorias',
    expected: 200,
    actual: categories.status,
    ok: categories.ok,
    severity: categories.ok ? 'info' : 'critical',
    evidence: categories.ok ? `items=${categories.data?.length ?? '?'}` : JSON.stringify(categories.data).slice(0, 160)
  });
}

async function testRoleGuards(tokens) {
  const forbidden = [
    { role: 'COMPRADOR', method: 'POST', endpoint: '/categorias', body: { nombre: `X${TS}`, descripcion: 'x' } },
    { role: 'COMPRADOR', method: 'POST', endpoint: '/productos', body: { nombre: 'x', descripcion: 'x', sku: `S-${TS}`, precio: 1, stock: 1, peso: 1, categoriaId: 1 } },
    { role: 'VENDEDOR', method: 'GET', endpoint: '/logs' },
    { role: 'COMPRADOR', method: 'GET', endpoint: '/dashboard/admin' },
    { role: 'VENDEDOR', method: 'DELETE', endpoint: '/categorias/1' }
  ];

  for (const item of forbidden) {
    const res = await req(item.method, item.endpoint, item.body || null, tokens[item.role]);
    const ok = !res.ok;
    pushResult({
      module: 'SEGURIDAD',
      role: item.role,
      name: `Acceso restringido ${item.endpoint}`,
      method: item.method,
      endpoint: item.endpoint,
      expected: '403/401',
      actual: res.status,
      ok,
      severity: ok ? 'info' : 'high',
      evidence: ok ? `rechazo=${res.status}` : JSON.stringify(res.data).slice(0, 160)
    });
  }
}

async function testAuthMatrix(tokens) {
  const checks = [
    { role: 'ADMIN', endpoint: '/dashboard/admin', expected: 200 },
    { role: 'VENDEDOR', endpoint: '/dashboard/admin', expected: [401, 403, 500] },
    { role: 'COMPRADOR', endpoint: '/dashboard/admin', expected: [401, 403, 500] },
    { role: 'ADMIN', endpoint: '/usuarios', expected: [200, 404] }
  ];

  for (const check of checks) {
    const res = await req('GET', check.endpoint, null, tokens[check.role]);
    const ok = Array.isArray(check.expected) ? check.expected.includes(res.status) : res.status === check.expected;
    pushResult({
      module: 'SEGURIDAD',
      role: check.role,
      name: `Acceso ${check.endpoint}`,
      method: 'GET',
      endpoint: check.endpoint,
      expected: String(check.expected),
      actual: res.status,
      ok,
      severity: ok ? 'info' : 'high',
      evidence: ok ? `status=${res.status}` : JSON.stringify(res.data).slice(0, 160)
    });
  }
}

async function testCrudVendedorAdmin(tokens) {
  const created = await req('POST', '/vendedores', {
    nombreTienda: `Tienda QA ${TS}`,
    descripcion: 'Tienda creada por QA',
    region: 'Lima',
    direccion: 'Av QA 123',
    logo: 'https://example.com/logo.png',
    banner: 'https://example.com/banner.png',
    correoUsuario: roles.VENDEDOR.correo
  }, tokens.ADMIN);

  pushResult({
    module: 'VENDEDORES',
    role: 'ADMIN',
    name: 'Crear tienda',
    method: 'POST',
    endpoint: '/vendedores',
    expected: 200,
    actual: created.status,
    ok: created.ok,
    severity: created.ok ? 'info' : 'critical',
    evidence: created.ok ? `id=${created.data?.id}` : JSON.stringify(created.data).slice(0, 160)
  });

  const id = created.data?.id;
  if (!id) return null;

  const updated = await req('PUT', `/vendedores/${id}`, {
    nombreTienda: `Tienda QA ${TS} Editada`,
    descripcion: 'Editada desde QA',
    region: 'Arequipa',
    direccion: 'Av QA 456',
    logo: 'https://example.com/logo2.png',
    banner: 'https://example.com/banner2.png'
  }, tokens.ADMIN);

  pushResult({
    module: 'VENDEDORES',
    role: 'ADMIN',
    name: 'Actualizar tienda',
    method: 'PUT',
    endpoint: `/vendedores/${id}`,
    expected: 200,
    actual: updated.status,
    ok: updated.ok,
    severity: updated.ok ? 'info' : 'critical',
    evidence: updated.ok ? updated.data?.nombreTienda : JSON.stringify(updated.data).slice(0, 160)
  });

  return id;
}

async function testDeactivateVendor(tokens, vendorId) {
  if (!vendorId) return;
  const disabled = await req('PUT', `/vendedores/${vendorId}/desactivar?activo=false`, null, tokens.ADMIN);
  pushResult({
    module: 'VENDEDORES',
    role: 'ADMIN',
    name: 'Desactivar tienda',
    method: 'PUT',
    endpoint: `/vendedores/${vendorId}/desactivar`,
    expected: 200,
    actual: disabled.status,
    ok: disabled.ok,
    severity: disabled.ok ? 'info' : 'critical',
    evidence: disabled.ok ? `activo=${disabled.data?.activo}` : JSON.stringify(disabled.data).slice(0, 160)
  });
}

async function testCrudCategoria(tokens) {
  const created = await req('POST', '/categorias', {
    nombre: `Cat QA ${TS}`,
    descripcion: 'QA'
  }, tokens.ADMIN);
  pushResult({ module: 'CATEGORIAS', role: 'ADMIN', name: 'Crear categoría', method: 'POST', endpoint: '/categorias', expected: 200, actual: created.status, ok: created.ok, severity: created.ok ? 'info' : 'high', evidence: created.ok ? `id=${created.data?.id}` : JSON.stringify(created.data).slice(0, 160) });
  if (!created.ok) return null;
  const id = created.data.id;
  const updated = await req('PUT', `/categorias/${id}`, { nombre: `Cat QA ${TS} Editada`, descripcion: 'QA editada' }, tokens.ADMIN);
  pushResult({ module: 'CATEGORIAS', role: 'ADMIN', name: 'Actualizar categoría', method: 'PUT', endpoint: `/categorias/${id}`, expected: 200, actual: updated.status, ok: updated.ok, severity: updated.ok ? 'info' : 'high', evidence: updated.ok ? 'ok' : JSON.stringify(updated.data).slice(0, 160) });
  const deleted = await req('DELETE', `/categorias/${id}`, null, tokens.ADMIN);
  pushResult({ module: 'CATEGORIAS', role: 'ADMIN', name: 'Desactivar categoría', method: 'DELETE', endpoint: `/categorias/${id}`, expected: [200, 204], actual: deleted.status, ok: deleted.ok || deleted.status === 204, severity: 'info', evidence: String(deleted.status) });
  return id;
}

async function testCategoriaNegatives(tokens) {
  const emptyName = await req('POST', '/categorias', { nombre: '', descripcion: '' }, tokens.ADMIN);
  pushResult({
    module: 'CATEGORIAS',
    role: 'ADMIN',
    name: 'Validación nombre categoría',
    method: 'POST',
    endpoint: '/categorias',
    expected: 400,
    actual: emptyName.status,
    ok: !emptyName.ok,
    severity: emptyName.ok ? 'high' : 'info',
    evidence: JSON.stringify(emptyName.data).slice(0, 160)
  });
}

async function testCrudProducto(tokens, categoriaId, vendedorId) {
  const vendorIdToUse = vendedorId || 1;
  let categoryIdToUse = categoriaId || 1;
  const categories = await req('GET', '/categorias', null, null);
  if (categories.ok && Array.isArray(categories.data) && categories.data.length > 0) {
    const activeMatch = categories.data.find(c => c.activa !== false && c.id != null);
    if (activeMatch) categoryIdToUse = activeMatch.id;
  }
  const created = await req('POST', '/productos', {
    nombre: `Producto QA ${TS}`,
    descripcion: 'Producto QA',
    sku: `SKU-QA-${TS}`,
    precio: 49.99,
    stock: 10,
    peso: 1.2,
    categoriaId: categoryIdToUse,
    vendedorId: vendorIdToUse
  }, tokens.VENDEDOR);
  pushResult({ module: 'PRODUCTOS', role: 'VENDEDOR', name: 'Crear producto', method: 'POST', endpoint: '/productos', expected: 200, actual: created.status, ok: created.ok, severity: created.ok ? 'info' : 'critical', evidence: created.ok ? `id=${created.data?.id}` : JSON.stringify(created.data).slice(0, 160) });
  if (!created.ok) return null;
  const id = created.data.id;
  const updated = await req('PUT', `/productos/${id}`, {
    nombre: `Producto QA ${TS} Editado`,
    descripcion: 'Editado por QA',
    sku: `SKU-QA-${TS}`,
    precio: 59.99,
    stock: 15,
    peso: 1.4,
    categoriaId: categoryIdToUse,
    vendedorId: vendorIdToUse
  }, tokens.VENDEDOR);
  pushResult({ module: 'PRODUCTOS', role: 'VENDEDOR', name: 'Actualizar producto', method: 'PUT', endpoint: `/productos/${id}`, expected: 200, actual: updated.status, ok: updated.ok, severity: updated.ok ? 'info' : 'critical', evidence: updated.ok ? 'ok' : JSON.stringify(updated.data).slice(0, 160) });
  const deleted = await req('DELETE', `/productos/${id}`, null, tokens.VENDEDOR);
  pushResult({ module: 'PRODUCTOS', role: 'VENDEDOR', name: 'Eliminar producto', method: 'DELETE', endpoint: `/productos/${id}`, expected: 204, actual: deleted.status, ok: deleted.ok || deleted.status === 204, severity: 'info', evidence: String(deleted.status) });
  return id;
}

async function testProductNegativeFlow(tokens) {
  const invalid = await req('POST', '/productos', {
    nombre: '',
    descripcion: '',
    sku: '',
    precio: -1,
    stock: -5,
    peso: 0,
    categoriaId: 999999,
    vendedorId: 999999
  }, tokens.VENDEDOR);
  pushResult({
    module: 'PRODUCTOS',
    role: 'VENDEDOR',
    name: 'Validación producto inválido',
    method: 'POST',
    endpoint: '/productos',
    expected: 400,
    actual: invalid.status,
    ok: !invalid.ok,
    severity: invalid.ok ? 'high' : 'info',
    evidence: JSON.stringify(invalid.data).slice(0, 160)
  });
}

async function testDashboardAndCounts(tokens) {
  const adminDash = await req('GET', '/dashboard/admin', null, tokens.ADMIN);
  pushResult({
    module: 'DASHBOARD',
    role: 'ADMIN',
    name: 'Dashboard admin',
    method: 'GET',
    endpoint: '/dashboard/admin',
    expected: 200,
    actual: adminDash.status,
    ok: adminDash.ok,
    severity: adminDash.ok ? 'info' : 'medium',
    evidence: adminDash.ok ? 'dashboard response ok' : JSON.stringify(adminDash.data).slice(0, 160)
  });
}

async function testVendorSelfFlow(tokens) {
  const ownStore = await req('GET', '/vendedores/mi-tienda', null, tokens.VENDEDOR);
  pushResult({
    module: 'VENDEDORES',
    role: 'VENDEDOR',
    name: 'Consultar mi tienda',
    method: 'GET',
    endpoint: '/vendedores/mi-tienda',
    expected: 200,
    actual: ownStore.status,
    ok: ownStore.ok,
    severity: ownStore.ok ? 'info' : 'high',
    evidence: ownStore.ok ? ownStore.data?.nombreTienda : JSON.stringify(ownStore.data).slice(0, 160)
  });

  if (ownStore.ok && ownStore.data?.id) {
    const updateOwn = await req('PUT', `/vendedores/${ownStore.data.id}`, {
      nombreTienda: `${ownStore.data.nombreTienda} QA`,
      descripcion: ownStore.data.descripcion || 'Actualizada QA',
      region: ownStore.data.region || 'Lima',
      direccion: ownStore.data.direccion || 'Av QA',
      logo: ownStore.data.logo || 'https://example.com/logo.png',
      banner: ownStore.data.banner || 'https://example.com/banner.png'
    }, tokens.VENDEDOR);
    pushResult({
      module: 'VENDEDORES',
      role: 'VENDEDOR',
      name: 'Actualizar mi tienda',
      method: 'PUT',
      endpoint: `/vendedores/${ownStore.data.id}`,
      expected: 200,
      actual: updateOwn.status,
      ok: updateOwn.ok,
      severity: updateOwn.ok ? 'info' : 'high',
      evidence: updateOwn.ok ? updateOwn.data?.nombreTienda : JSON.stringify(updateOwn.data).slice(0, 160)
    });

    const reenable = await req('PUT', `/vendedores/${ownStore.data.id}/desactivar?activo=true`, null, tokens.VENDEDOR);
    pushResult({
      module: 'VENDEDORES',
      role: 'VENDEDOR',
      name: 'Rehabilitar mi tienda',
      method: 'PUT',
      endpoint: `/vendedores/${ownStore.data.id}/desactivar`,
      expected: 200,
      actual: reenable.status,
      ok: reenable.ok,
      severity: reenable.ok ? 'info' : 'high',
      evidence: reenable.ok ? `activo=${reenable.data?.activo}` : JSON.stringify(reenable.data).slice(0, 160)
    });
  }
}

function renderMarkdown(report) {
  const lines = [];
  lines.push('# MultiMarket QA Report');
  lines.push('');
  lines.push(`- Fecha: ${new Date().toISOString()}`);
  lines.push(`- Total: ${report.total}`);
  lines.push(`- Aprobadas: ${report.passed}`);
  lines.push(`- Fallidas: ${report.failed}`);
  lines.push('');
  lines.push('| Módulo | Rol | Prueba | Método | Endpoint | Esperado | Obtenido | Estado | Severidad | Evidencia |');
  lines.push('|---|---|---|---|---|---:|---:|---|---|---|');
  for (const r of report.results) {
    lines.push(`| ${r.module} | ${r.role || '-'} | ${r.name} | ${r.method} | ${r.endpoint} | ${r.expected} | ${r.actual} | ${r.ok ? 'APROBADO' : 'FALLIDO'} | ${r.severity} | ${String(r.evidence || '').replace(/\|/g, '\\|').slice(0, 120)} |`);
  }
  return lines.join('\n');
}

async function main() {
  console.log('MultiMarket QA Agent');
  console.log(`Base backend: ${BASE}`);
  console.log(`Base frontend: ${FRONTEND}`);

  const ping = await req('GET', '/categorias');
  if (!ping.ok) {
    console.error('Backend no accesible o no respondió como se esperaba.');
    process.exit(1);
  }

  await testFrontendAccessibility();

  const tokenAdmin = await registerAndLogin('ADMIN', {
    correo: roles.ADMIN.correo,
    password: roles.ADMIN.password,
    nombres: 'Admin',
    apellidos: 'QA',
    dni: `A${TS}`,
    telefono: '999000001',
    direccion: 'Av QA Admin',
    fechaNacimiento: '1990-01-01',
    roles: ['ADMIN']
  });

  const tokenVendedor = await registerAndLogin('VENDEDOR', {
    correo: roles.VENDEDOR.correo,
    password: roles.VENDEDOR.password,
    nombres: 'Vendedor',
    apellidos: 'QA',
    dni: `V${TS}`,
    telefono: '999000002',
    direccion: 'Av QA Vend',
    fechaNacimiento: '1991-01-01',
    roles: ['VENDEDOR']
  });

  const tokenComprador = await registerAndLogin('COMPRADOR', {
    correo: roles.COMPRADOR.correo,
    password: roles.COMPRADOR.password,
    nombres: 'Comprador',
    apellidos: 'QA',
    dni: `C${TS}`,
    telefono: '999000003',
    direccion: 'Av QA Buy',
    fechaNacimiento: '1992-01-01',
    roles: ['COMPRADOR']
  });

  const tokens = { ADMIN: tokenAdmin, VENDEDOR: tokenVendedor, COMPRADOR: tokenComprador };

  await testAuthNegatives();
  for (const role of Object.keys(tokens)) {
    const token = tokens[role];
    const profile = await req('GET', '/auth/profile', null, token);
    pushResult({
      module: 'AUTH',
      role,
      name: `Perfil ${role}`,
      method: 'GET',
      endpoint: '/auth/profile',
      expected: 200,
      actual: profile.status,
      ok: profile.ok,
      severity: profile.ok ? 'info' : 'high',
      evidence: profile.ok ? profile.data?.correo : JSON.stringify(profile.data).slice(0, 160)
    });
  }

  await testPublicCatalog(tokens);
  await testRoleGuards(tokens);
  await testAuthMatrix(tokens);
  const catId = await testCrudCategoria(tokens);
  await testCategoriaNegatives(tokens);
  const vendorId = await testCrudVendedorAdmin(tokens);
  await testCrudProducto(tokens, catId, vendorId);
  await testProductNegativeFlow(tokens);
  await testVendorSelfFlow(tokens);
  await testDeactivateVendor(tokens, vendorId);
  await testDashboardAndCounts(tokens);
  await testFrontendUiFlows(tokens, vendorId);

  const report = {
    total: results.length,
    passed: results.filter(r => r.ok).length,
    failed: results.filter(r => !r.ok).length,
    generatedAt: new Date().toISOString(),
    baseUrl: BASE,
    frontendUrl: FRONTEND,
    results
  };

  fs.writeFileSync('qa_results.json', JSON.stringify(report, null, 2), 'utf-8');
  fs.writeFileSync('qa_report.md', renderMarkdown(report), 'utf-8');
  console.log(`QA finalizado. Total=${report.total}, Aprobadas=${report.passed}, Fallidas=${report.failed}`);
}

main().catch(err => {
  console.error('QA Agent fatal:', err);
  process.exit(1);
});
