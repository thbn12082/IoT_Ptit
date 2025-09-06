/* Data Sensor page logic: fetch real data from backend and render a table
   Endpoints available (from backend):
   - GET /api/sensors (all)
   - GET /api/sensors/latest (top 10)
   - GET /api/sensors/recent (24h)
   - GET /api/sensors/range?start=...&end=...
*/

const API_BASE = '/api/sensors';

const state = {
    allRows: [],
    filteredRows: [],
    page: 1,
    pageSize: 10,
    totalPages: 1,
};

const tableBody = document.getElementById('tableBody');
const pagination = document.getElementById('pagination');
const pageSizeSelect = document.getElementById('pageSize');
const rangeText = document.getElementById('rangeText');
const filterSelect = document.getElementById('filter');
const searchInput = document.getElementById('search');
const searchBtn = document.getElementById('searchBtn');

function formatTime(isoString) {
    if (!isoString) return '';
    try {
        const d = new Date(isoString);
        const date = d.toLocaleDateString('vi-VN');
        const time = d.toLocaleTimeString('vi-VN', { hour12: false });
        return `${time} ${date}`;
    } catch (e) {
        return isoString;
    }
}

function normalizeRow(row) {
    // Backend entity fields: id, temperature (Double), humidity (Double), lightLevel (Integer), uptime (Integer), createdAt (LocalDateTime)
    return {
        id: row.id,
        temperature: row.temperature,
        humidity: row.humidity,
        lightLevel: row.lightLevel,
        createdAt: row.createdAt,
    };
}

async function fetchAll() {
    const res = await fetch(API_BASE, { headers: { 'Accept': 'application/json' } });
    if (!res.ok) throw new Error(`Fetch failed ${res.status}`);
    const data = await res.json();
    return Array.isArray(data) ? data.map(normalizeRow) : [];
}

function applyFilterAndSearch() {
    const q = (searchInput.value || '').trim().toLowerCase();
    const by = filterSelect.value; // all | temperature | humidity | lightLevel | createdAt
    let rows = state.allRows.slice();

    if (q) {
        rows = rows.filter(r => {
            const fields = by === 'all' ? [r.id, r.temperature, r.humidity, r.lightLevel, r.createdAt]
                : [r[by]];
            return fields.some(v => String(v ?? '').toLowerCase().includes(q));
        });
    }

    state.filteredRows = rows;
    state.page = 1;
    build();
}

function build() {
    const total = state.filteredRows.length;
    state.totalPages = Math.max(1, Math.ceil(total / state.pageSize));
    const startIdx = (state.page - 1) * state.pageSize;
    const endIdx = Math.min(total, startIdx + state.pageSize);
    const pageRows = state.filteredRows.slice(startIdx, endIdx);

    // Body
    if (pageRows.length === 0) {
        tableBody.innerHTML = '<tr><td class="muted" colspan="5">Không có dữ liệu</td></tr>';
    } else {
        tableBody.innerHTML = pageRows.map(r => `
            <tr>
                <td>${r.id}</td>
                <td>${Number(r.temperature).toFixed(0)}</td>
                <td>${Number(r.humidity).toFixed(0)}</td>
                <td>${r.lightLevel ?? ''}</td>
                <td><span class="status-dot"></span>${formatTime(r.createdAt)}</td>
            </tr>
        `).join('');
    }

    // Range text
    if (total === 0) {
        rangeText.textContent = 'Hiển thị 0 kết quả';
    } else {
        rangeText.textContent = `Hiển thị ${startIdx + 1}-${endIdx} trong tổng số ${total} kết quả`;
    }

    // Pagination
    pagination.innerHTML = '';
    const addBtn = (label, page, disabled = false, active = false) => {
        const btn = document.createElement('button');
        btn.className = 'page-btn' + (active ? ' active' : '');
        btn.textContent = label;
        btn.disabled = disabled;
        btn.addEventListener('click', () => {
            if (state.page !== page) {
                state.page = page;
                build();
            }
        });
        pagination.appendChild(btn);
    };

    addBtn('«', 1, state.page === 1);
    addBtn('‹', Math.max(1, state.page - 1), state.page === 1);

    // simple window of pages
    const windowSize = 5;
    const startPage = Math.max(1, state.page - 2);
    const endPage = Math.min(state.totalPages, startPage + windowSize - 1);
    for (let p = startPage; p <= endPage; p++) addBtn(String(p), p, false, p === state.page);

    addBtn('›', Math.min(state.totalPages, state.page + 1), state.page === state.totalPages);
    addBtn('»', state.totalPages, state.page === state.totalPages);
}

function initEvents() {
    pageSizeSelect.addEventListener('change', () => {
        state.pageSize = Number(pageSizeSelect.value);
        state.page = 1;
        build();
    });

    searchBtn.addEventListener('click', applyFilterAndSearch);
    searchInput.addEventListener('keydown', (e) => { if (e.key === 'Enter') applyFilterAndSearch(); });
}

async function init() {
    try {
        state.pageSize = Number(pageSizeSelect.value);
        initEvents();
        const rows = await fetchAll();
        state.allRows = rows;
        state.filteredRows = rows.slice();
        build();
    } catch (err) {
        console.error(err);
        tableBody.innerHTML = `<tr><td class="muted" colspan="5">Lỗi tải dữ liệu: ${err.message}</td></tr>`;
    }
}

init();

