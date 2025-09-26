const API_BASE = '/api/led-events';

const DEVICE_NAMES = {
    1: "Đèn",
    2: "Quạt",
    3: "Điều hòa"
};

const DEVICE_ICONS = {
    1: "💡",
    2: "🌀",
    3: "❄️"
};

const state = {
    currentPage: 0,
    pageSize: 10,
    totalPages: 1,
    totalElements: 0,
    deviceFilter: 'all',
    timeFilter: '',
    loading: false
};

// Get DOM elements
const tableBody = document.getElementById('tableBody');
const pagination = document.getElementById('pagination');
const pageSizeSelect = document.getElementById('pageSize');
const rangeText = document.getElementById('rangeText');
const deviceFilter = document.getElementById('deviceFilter');
const timeFilter = document.getElementById('timeFilter');
const searchBtn = document.getElementById('searchBtn');
const clearBtn = document.getElementById('clearBtn');

// URL Management
function updateURL() {
    const params = new URLSearchParams();
    params.set('page', state.currentPage.toString());
    params.set('size', state.pageSize.toString());

    if (state.deviceFilter !== 'all') {
        params.set('deviceFilter', state.deviceFilter);
    }
    if (state.timeFilter) {
        params.set('timeFilter', state.timeFilter);
    }

    const newUrl = `${window.location.pathname}?${params.toString()}`;
    window.history.pushState({}, '', newUrl);
}

function loadFromURL() {
    const params = new URLSearchParams(window.location.search);
    state.currentPage = parseInt(params.get('page') || '0');
    state.pageSize = parseInt(params.get('size') || '10');
    state.deviceFilter = params.get('deviceFilter') || 'all';
    state.timeFilter = params.get('timeFilter') || '';

    // Update form elements
    pageSizeSelect.value = state.pageSize;
    deviceFilter.value = state.deviceFilter;
    timeFilter.value = state.timeFilter;
}

// Format timestamp
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

// Create device chip
function createDeviceChip(ledNumber) {
    const deviceName = DEVICE_NAMES[ledNumber] || `LED ${ledNumber}`;
    const deviceIcon = DEVICE_ICONS[ledNumber] || '💡';
    return `<span class="device-chip device-${ledNumber}">${deviceIcon} ${deviceName}</span>`;
}

// Create status badge
function createStatusBadge(state) {
    const isOn = state === 'ON' || state === true;
    return `<span class="badge ${isOn ? 'badge-on' : 'badge-off'}">${isOn ? 'Bật' : 'Tắt'}</span>`;
}

// Fetch paginated data from backend - SIMPLIFIED (no general search)
async function fetchLedEvents(page = 0, size = 10, deviceFilter = 'all', timeFilter = '') {
    try {
        state.loading = true;
        updateTableLoading();

        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
            deviceFilter: deviceFilter
        });

        // Only send timeFilter, no general search
        if (timeFilter && timeFilter.trim()) {
            params.append('timeFilter', timeFilter.trim());
        }

        console.log(`Fetching: page=${page}, size=${size}, device=${deviceFilter}, time=${timeFilter}`);

        const response = await fetch(`${API_BASE}/paginated?${params}`, {
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();

        // Update state
        state.currentPage = data.currentPage;
        state.totalPages = data.totalPages;
        state.totalElements = data.totalElements;
        state.pageSize = data.size;
        state.deviceFilter = deviceFilter;
        state.timeFilter = timeFilter;

        console.log(`Loaded ${data.content.length} records from backend (Total: ${data.totalElements})`);
        return data;
    } catch (error) {
        console.error('Fetch error:', error);
        throw error;
    } finally {
        state.loading = false;
    }
}

// Update table with loading state
function updateTableLoading() {
    if (state.loading) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="4" class="muted" style="text-align: center; padding: 20px;">
                    <i class="fa fa-spinner fa-spin"></i> Đang tải...
                </td>
            </tr>
        `;
    }
}

// Build table and pagination
// CẬP NHẬT action-history-simplified.js - update buildTable function:

function buildTable(data) {
    // Update table content
    if (data.content && data.content.length > 0) {
        tableBody.innerHTML = data.content.map(row => `
            <tr>
                <td>${row.id}</td>
                <td>${createDeviceChip(row.ledNumber)}</td>
                <td>${createStatusBadge(row.state)}</td>
                <td>${formatTime(row.createdAt)}</td>
            </tr>
        `).join('');
    } else {
        let noDataMessage = 'Không có dữ liệu';
        if (state.deviceFilter !== 'all') {
            noDataMessage = `Không có hoạt động của ${DEVICE_NAMES[state.deviceFilter]}`;
        }
        if (state.timeFilter) {
            // Enhanced message for different filter types
            if (state.timeFilter.includes('-')) {
                noDataMessage = `Không có hoạt động trong khoảng thời gian "${state.timeFilter}"`;
            } else if (state.timeFilter.includes('/')) {
                noDataMessage = `Không có hoạt động vào ngày "${state.timeFilter}"`;
            } else {
                noDataMessage = `Không có hoạt động vào thời gian "${state.timeFilter}"`;
            }
        }
        if (state.deviceFilter !== 'all' && state.timeFilter) {
            const deviceName = DEVICE_NAMES[state.deviceFilter];
            if (state.timeFilter.includes('-')) {
                noDataMessage = `Không có hoạt động của ${deviceName} trong khoảng "${state.timeFilter}"`;
            } else if (state.timeFilter.includes('/')) {
                noDataMessage = `Không có hoạt động của ${deviceName} vào ngày "${state.timeFilter}"`;
            } else {
                noDataMessage = `Không có hoạt động của ${deviceName} vào thời gian "${state.timeFilter}"`;
            }
        }

        tableBody.innerHTML = `
            <tr>
                <td colspan="4" class="muted" style="text-align: center; padding: 20px;">
                    ${noDataMessage}
                </td>
            </tr>
        `;
    }

    // Update range text with enhanced filter info
    if (data.totalElements === 0) {
        rangeText.textContent = 'Hiển thị 0 kết quả';
    } else {
        const start = (data.currentPage * data.size) + 1;
        const end = Math.min((data.currentPage + 1) * data.size, data.totalElements);
        rangeText.textContent = `Hiển thị ${start}-${end} trong tổng số ${data.totalElements} kết quả`;

        // Add enhanced filter info
        let filterInfo = [];
        if (state.deviceFilter !== 'all') {
            filterInfo.push(`thiết bị: ${DEVICE_NAMES[state.deviceFilter]}`);
        }
        if (state.timeFilter) {
            if (state.timeFilter.includes('-')) {
                filterInfo.push(`khoảng thời gian: "${state.timeFilter}"`);
            } else if (state.timeFilter.includes('/')) {
                filterInfo.push(`ngày: "${state.timeFilter}"`);
            } else {
                filterInfo.push(`thời gian: "${state.timeFilter}"`);
            }
        }

        if (filterInfo.length > 0) {
            rangeText.textContent += ` (${filterInfo.join(', ')})`;
        }
    }

    // Build pagination (same as before)
    buildPagination(data);
}


// Build pagination controls (same as data-sensor)
function buildPagination(data) {
    pagination.innerHTML = '';

    if (data.totalPages <= 1) return;

    // Create pagination container
    const paginationContainer = document.createElement('nav');
    paginationContainer.setAttribute('aria-label', 'Page navigation');

    const ul = document.createElement('ul');
    ul.className = 'pagination justify-content-center';

    const addBtn = (content, page, disabled = false, active = false, isIcon = false) => {
        const li = document.createElement('li');
        li.className = `page-item ${active ? 'active' : ''} ${disabled ? 'disabled' : ''}`;

        const a = document.createElement('a');
        a.className = 'page-link';
        a.href = '#';

        if (isIcon) {
            a.innerHTML = content;
        } else {
            a.textContent = content;
        }

        if (!disabled) {
            a.addEventListener('click', (e) => {
                e.preventDefault();
                if (state.currentPage !== page) {
                    loadData(page, true);
                }
            });
        }

        li.appendChild(a);
        ul.appendChild(li);
    };

    // Previous button
    addBtn('<i class="fas fa-chevron-left"></i>', Math.max(0, state.currentPage - 1), data.first, false, true);

    // Page numbers logic
    const windowSize = 5;
    let startPage = Math.max(0, state.currentPage - 2);
    let endPage = Math.min(data.totalPages - 1, startPage + windowSize - 1);

    if (endPage - startPage < windowSize - 1) {
        startPage = Math.max(0, endPage - windowSize + 1);
    }

    // First page + ellipsis
    if (startPage > 0) {
        addBtn('1', 0);
        if (startPage > 1) {
            const li = document.createElement('li');
            li.className = 'page-item disabled';
            li.innerHTML = '<span class="page-link">...</span>';
            ul.appendChild(li);
        }
    }

    // Page numbers
    for (let p = startPage; p <= endPage; p++) {
        addBtn((p + 1).toString(), p, false, p === state.currentPage);
    }

    // Last page + ellipsis
    if (endPage < data.totalPages - 1) {
        if (endPage < data.totalPages - 2) {
            const li = document.createElement('li');
            li.className = 'page-item disabled';
            li.innerHTML = '<span class="page-link">...</span>';
            ul.appendChild(li);
        }
        addBtn(data.totalPages.toString(), data.totalPages - 1);
    }

    // Next button
    addBtn('<i class="fas fa-chevron-right"></i>', Math.min(data.totalPages - 1, state.currentPage + 1), data.last, false, true);

    paginationContainer.appendChild(ul);
    pagination.appendChild(paginationContainer);
}

// Load data function - SIMPLIFIED
async function loadData(page = 0, updateUrl = false) {
    try {
        const data = await fetchLedEvents(page, state.pageSize, state.deviceFilter, state.timeFilter);
        buildTable(data);

        if (updateUrl) {
            updateURL();
        }
    } catch (error) {
        console.error('Load data error:', error);
        tableBody.innerHTML = `
            <tr>
                <td colspan="4" class="muted" style="text-align: center; padding: 20px; color: red;">
                    <i class="fa fa-exclamation-triangle"></i> Lỗi tải dữ liệu: ${error.message}
                </td>
            </tr>
        `;

        pagination.innerHTML = '';
        rangeText.textContent = 'Lỗi tải dữ liệu';
    }
}

// Search function - SIMPLIFIED (only device and time)
function performSearch() {
    const deviceValue = deviceFilter.value;
    const timeValue = timeFilter.value.trim();

    state.deviceFilter = deviceValue;
    state.timeFilter = timeValue;
    state.currentPage = 0; // Reset to first page

    console.log(`Performing search: device: ${deviceValue}, time: "${timeValue}"`);
    loadData(0, true);
}

// Clear search function - SIMPLIFIED
function clearSearch() {
    timeFilter.value = '';
    deviceFilter.value = 'all';
    state.deviceFilter = 'all';
    state.timeFilter = '';
    state.currentPage = 0;

    console.log('Search cleared');
    loadData(0, true);
}

// Initialize event listeners - SIMPLIFIED
function initEvents() {
    // Page size change
    pageSizeSelect.addEventListener('change', () => {
        const newSize = Number(pageSizeSelect.value);
        console.log(`Page size changed to: ${newSize}`);
        state.pageSize = newSize;
        state.currentPage = 0;
        loadData(0, true);
    });

    // Search button
    searchBtn.addEventListener('click', performSearch);

    // Clear button
    clearBtn.addEventListener('click', clearSearch);

    // Search on Enter key in time input only
    timeFilter.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            performSearch();
        }
    });

    // Auto-search when device filter changes
    deviceFilter.addEventListener('change', performSearch);

    // Clear search when time input is empty and device is 'all'
    timeFilter.addEventListener('input', (e) => {
        if (e.target.value.trim() === '' && deviceFilter.value === 'all' &&
            (state.timeFilter !== '' || state.deviceFilter !== 'all')) {
            clearSearch();
        }
    });

    // Handle browser back/forward buttons
    window.addEventListener('popstate', () => {
        loadFromURL();
        loadData(state.currentPage, false);
    });
}

// Initialize the page
async function init() {
    try {
        console.log('Initializing action-history page...');

        loadFromURL();
        initEvents();
        await loadData(state.currentPage, false);

        console.log('Action-history page initialized successfully');
    } catch (error) {
        console.error('Initialization error:', error);
        tableBody.innerHTML = `
            <tr>
                <td colspan="4" class="muted" style="text-align: center; padding: 20px; color: red;">
                    <i class="fa fa-exclamation-triangle"></i> Lỗi khởi tạo: ${error.message}
                </td>
            </tr>
        `;
    }
}

// Start the application when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM loaded, starting application...');
    init();
});
