/* MathLit Admin Panel — shared utilities */

const API = '/admin/api';

// ── Auth helpers ──────────────────────────────────────────────────────────────

function getToken() {
  return localStorage.getItem('admin_token');
}

function authHeaders() {
  return {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + getToken()
  };
}

function requireAuth() {
  if (!getToken()) {
    window.location.href = '/admin/login.html';
    return false;
  }
  return true;
}

async function logout() {
  try {
    await fetch(API + '/auth/logout', { method: 'POST', headers: authHeaders() });
  } catch (_) {}
  localStorage.removeItem('admin_token');
  window.location.href = '/admin/login.html';
}

// ── Generic fetch wrapper ─────────────────────────────────────────────────────

async function apiFetch(path, options = {}) {
  const res = await fetch(API + path, {
    ...options,
    headers: { ...authHeaders(), ...(options.headers || {}) }
  });
  if (res.status === 401) {
    localStorage.removeItem('admin_token');
    window.location.href = '/admin/login.html';
    return null;
  }
  const text = await res.text();
  try { return { ok: res.ok, status: res.status, data: JSON.parse(text) }; }
  catch { return { ok: res.ok, status: res.status, data: text }; }
}

// ── Sidebar active state ──────────────────────────────────────────────────────

function markActiveNav() {
  const path = window.location.pathname.split('/').pop();
  document.querySelectorAll('.nav-item').forEach(el => {
    if (el.getAttribute('href') === path) el.classList.add('active');
  });
}

// ── Alert helpers ─────────────────────────────────────────────────────────────

function showAlert(el, type, msg) {
  el.className = 'alert ' + type;
  el.textContent = msg;
  el.style.display = 'block';
  if (type === 'success') setTimeout(() => { el.style.display = 'none'; }, 4000);
}

// ── Tab switching ─────────────────────────────────────────────────────────────

function initTabs(containerSelector) {
  document.querySelectorAll(containerSelector + ' .tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const target = btn.dataset.tab;
      document.querySelectorAll(containerSelector + ' .tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll(containerSelector + ' .tab-content').forEach(c => c.classList.remove('active'));
      btn.classList.add('active');
      document.getElementById(target).classList.add('active');
    });
  });
}

// ── Section → Category mapping ────────────────────────────────────────────────

const SECTION_CATEGORIES = {
  'ARITHMETIC':    ['SIMPLIFICATION','RATIO','AVERAGE','PROFIT_LOSS','TIME_WORK','SPEED_DISTANCE'],
  'ALGEBRA':       ['LINEAR_EQ','QUADRATIC','POLYNOMIALS','INDICES','SEQUENCES'],
  'GEOMETRY':      ['AREA','PERIMETER','VOLUME','TRIANGLES','CIRCLES'],
  'TRIGONOMETRY':  ['TRIGONOMETRY'],
  'STATISTICS':    ['MEAN','MEDIAN','MODE','PROBABILITY','PERMUTATION','COMBINATION'],
  'COMPETITIVE':   []
};

function populateCategoryDropdown(sectionVal, selectEl) {
  const cats = SECTION_CATEGORIES[sectionVal] || [];
  selectEl.innerHTML = '<option value="">— select category —</option>';
  cats.forEach(c => {
    const o = document.createElement('option');
    o.value = c; o.textContent = c;
    selectEl.appendChild(o);
  });
}

// ── Sidebar HTML ─────────────────────────────────────────────────────────────

function renderSidebar() {
  const page = window.location.pathname.split('/').pop() || 'questions.html';
  return `
  <div class="sidebar">
    <div class="sidebar-logo">
      <span>Math<em>Lit</em></span>
      <small>Admin Panel</small>
    </div>
    <nav>
      <a href="questions.html" class="nav-item ${page==='questions.html'?'active':''}">
        <span class="icon">➕</span><span>Questions</span>
      </a>
      <a href="categories.html" class="nav-item ${page==='categories.html'?'active':''}">
        <span class="icon">🏆</span><span>Competitive</span>
      </a>
      <a href="bulk.html" class="nav-item ${page==='bulk.html'?'active':''}">
        <span class="icon">📤</span><span>Bulk Upload</span>
      </a>
    </nav>
    <div class="nav-logout">
      <button onclick="logout()" class="nav-item" style="color:var(--red)">
        <span class="icon">🚪</span><span>Logout</span>
      </button>
    </div>
  </div>`;
}
