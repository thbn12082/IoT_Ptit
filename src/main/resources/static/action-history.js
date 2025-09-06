const API_BASE = '/api/led-events';

const DEVICE_NAME = {
    1: 'Đèn',
    2: 'Quạt',
    3: 'Điều hòa'
};

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
const deviceFilter = document.getElementById('deviceFilter');
const timeFilter = document.getElementById('timeFilter');
const searchBtn = document.getElementById('searchBtn');

function toBadge(stateOn) {
    const isOn = stateOn === true || stateOn === 'ON';
    return `<span class="badge ${isOn ? 'badge-on' : 'badge-off'}">${isOn ? 'Bật' : 'Tắt'}</span>`;
}

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
    // Backend LedEvent fields: id, ledNumber (Integer), state ("ON"|"OFF"), createdAt (ISO string)
    return {
        id: row.id,
        ledNumber: row.ledNumber,
        deviceName: DEVICE_NAME[row.ledNumber] || `LED ${row.ledNumber}`,
        state: row.state,
        createdAt: row.createdAt,
    };
}

async function fetchRecent() {
    const res = await fetch(`${API_BASE}/recent`, { headers: { 'Accept': 'application/json' } });
    if (!res.ok) throw new Error(`Fetch failed ${res.status}`);
    const data = await res.json();
    return Array.isArray(data) ? data.map(normalizeRow) : [];
}

function applyFilter() {
    const deviceVal = deviceFilter.value; // all | 1 | 2 | 3
    const timeQuery = (timeFilter.value || '').trim();
    let rows = state.allRows.slice();

    if (deviceVal !== 'all') {
        const ledNum = Number(deviceVal);
        rows = rows.filter(r => r.ledNumber === ledNum);
    }

    if (timeQuery) {
        const q = timeQuery.toLowerCase();
        rows = rows.filter(r => formatTime(r.createdAt).toLowerCase().includes(q));
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

    if (pageRows.length === 0) {
        tableBody.innerHTML = '<tr><td class="muted" colspan="4">Không có dữ liệu</td></tr>';
    } else {
        tableBody.innerHTML = pageRows.map(r => `
            <tr>
                <td>${r.id}</td>
                <td><span class="device-chip">${r.deviceName}</span></td>
                <td>${toBadge(r.state === 'ON')}</td>
                <td>${formatTime(r.createdAt)}</td>
            </tr>
        `).join('');
    }

    if (total === 0) {
        rangeText.textContent = 'Hiển thị 0 kết quả';
    } else {
        rangeText.textContent = `Hiển thị ${startIdx + 1}-${endIdx} trong tổng số ${total} kết quả`;
    }

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

    searchBtn.addEventListener('click', applyFilter);
    timeFilter.addEventListener('keydown', (e) => { if (e.key === 'Enter') applyFilter(); });
    deviceFilter.addEventListener('change', applyFilter);
}

async function init() {
    try {
        state.pageSize = Number(pageSizeSelect.value);
        initEvents();
        const rows = await fetchRecent();
        state.allRows = rows;
        state.filteredRows = rows.slice();
        build();
    } catch (err) {
        console.error(err);
        tableBody.innerHTML = `<tr><td class="muted" colspan="4">Lỗi tải dữ liệu: ${err.message}</td></tr>`;
    }
}

init();