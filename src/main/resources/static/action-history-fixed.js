// Action History JavaScript - FIXED device names

const API_BASE = '/api/led-events';

// FIXED: Only 3 devices with proper Vietnamese names
const DEVICE_NAMES = {
    'all': 'Tất cả',
    '1': 'Đèn (LED 1)',
    '2': 'Quạt (LED 2)',
    '3': 'Điều hòa (LED 3)'
};

// State object
const state = {
    currentPage: 0,
    pageSize: 10,
    totalPages: 1,
    totalElements: 0,
    search: '',
    deviceFilter: 'all',
    timeFilter: '',
    loading: false
};

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    console.log('📊 Action History page loaded');

    // Get DOM elements
    const searchInput = document.getElementById('search');
    const deviceFilter = document.getElementById('deviceFilter');
    const timeFilter = document.getElementById('timeFilter');
    const searchBtn = document.getElementById('searchBtn');
    const clearBtn = document.getElementById('clearBtn');
    const tableBody = document.getElementById('tableBody');
    const pagination = document.getElementById('pagination');
    const rangeText = document.querySelector('.range-text');
    const pageSizeSelect = document.getElementById('pageSizeSelect');

    // Validate required elements
    const requiredElements = [
        {element: searchInput, name: 'search input'},
        {element: deviceFilter, name: 'device filter'},
        {element: timeFilter, name: 'time filter'},
        {element: searchBtn, name: 'search button'},
        {element: clearBtn, name: 'clear button'},
        {element: tableBody, name: 'table body'}
    ];

    const missingElements = requiredElements
        .filter(item => !item.element)
        .map(item => item.name);

    if (missingElements.length > 0) {
        console.error(`❌ Missing required elements: ${missingElements.join(', ')}`);
        if (tableBody) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="4" class="error-state action-history">
                        Lỗi: Thiếu elements trong HTML: ${missingElements.join(', ')}
                    </td>
                </tr>
            `;
        }
        return;
    }

    // Fetch function
    async function fetchLedEvents(page = 0, size = 10, search = '', deviceFilter = 'all', timeFilter = '') {
        try {
            state.loading = true;
            updateTableLoading();

            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
                deviceFilter: deviceFilter
            });

            if (search && search.trim()) {
                params.append('search', search.trim());
            }

            if (timeFilter && timeFilter.trim()) {
                params.append('timeFilter', timeFilter.trim());
            }

            console.log(`🔍 Fetching: ${API_BASE}/paginated?${params.toString()}`);

            const response = await fetch(`${API_BASE}/paginated?${params}`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();

            // Update state
            state.currentPage = data.currentPage;
            state.totalPages = data.totalPages;
            state.totalElements = data.totalElements;
            state.pageSize = data.size;
            state.search = search;
            state.deviceFilter = deviceFilter;
            state.timeFilter = timeFilter;

            console.log(`✅ Loaded ${data.content.length} records (Total: ${data.totalElements})`);
            return data;

        } catch (error) {
            console.error('❌ Fetch error:', error);
            throw error;
        } finally {
            state.loading = false;
        }
    }

    // Build table function
    function buildTable(data) {
        if (!tableBody) {
            console.error('❌ Table body not found');
            return;
        }

        if (data.content && data.content.length > 0) {
            tableBody.innerHTML = data.content.map(row => `
                <tr>
                    <td class="font-mono">${row.id}</td>
                    <td>${formatDevice(row.ledNumber)}</td>
                    <td>${formatAction(row.stateOn)}</td>
                    <td class="font-mono text-muted time-column">${formatTime(row.createdAt)}</td>
                </tr>
            `).join('');
        } else {
            let noDataMessage = getNoDataMessage();

            tableBody.innerHTML = `
                <tr>
                    <td colspan="4" class="empty-state action-history">
                        ${noDataMessage}
                    </td>
                </tr>
            `;
        }

        updateRangeText(data);
        buildPagination(data);
    }

    // FIXED: Formatting functions with correct device names and only 3 devices
    function formatDevice(ledNumber) {
        if (!ledNumber || ledNumber < 1 || ledNumber > 3) {
            return '<span class="text-muted">N/A</span>';
        }

        const deviceClass = getDeviceClass(ledNumber);
        const deviceIcon = getDeviceIcon(ledNumber);
        const deviceName = getDeviceShortName(ledNumber);

        return `
            <div class="device-badge ${deviceClass}">
                <span class="device-icon ${deviceIcon}"></span>
                ${deviceName}
            </div>
        `;
    }

    function formatAction(stateOn) {
        if (stateOn === null || stateOn === undefined) {
            return '<span class="text-muted">N/A</span>';
        }

        const actionClass = stateOn ? 'on bật' : 'off tắt';
        const actionText = stateOn ? 'Bật' : 'Tắt';
        const statusClass = stateOn ? 'on' : 'off';

        return `
            <div style="display: flex; align-items: center;">
                <span class="status-indicator ${statusClass}"></span>
                <span class="action-badge ${actionClass}">${actionText}</span>
            </div>
        `;
    }

    // FIXED: Device mapping functions - only 3 devices
    function getDeviceClass(ledNumber) {
        const deviceMap = {
            1: 'led-1',      // Đèn
            2: 'led-2',      // Quạt
            3: 'led-3'       // Điều hòa
        };
        return deviceMap[ledNumber] || 'led-1';
    }

    function getDeviceIcon(ledNumber) {
        const iconMap = {
            1: 'led',        // Đèn - LED icon
            2: 'fan',        // Quạt - Fan icon
            3: 'ac'          // Điều hòa - AC icon
        };
        return iconMap[ledNumber] || 'led';
    }

    function getDeviceShortName(ledNumber) {
        const nameMap = {
            1: 'Đèn',
            2: 'Quạt',
            3: 'Điều hòa'
        };
        return nameMap[ledNumber] || 'Unknown';
    }

    function formatTime(timestamp) {
        if (!timestamp) return '<span class="text-muted">N/A</span>';
        const date = new Date(timestamp);
        return date.toLocaleString('vi-VN', {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    }

    function getNoDataMessage() {
        if (state.search && state.timeFilter) {
            return `Không tìm thấy kết quả cho "${state.search}" và thời gian "${state.timeFilter}"`;
        } else if (state.search) {
            return `Không tìm thấy kết quả cho "${state.search}"`;
        } else if (state.deviceFilter !== 'all') {
            const deviceName = getDeviceShortName(parseInt(state.deviceFilter));
            return `Không có hoạt động cho ${deviceName}`;
        } else if (state.timeFilter) {
            if (state.timeFilter.includes('-')) {
                return `Không có hoạt động trong khoảng thời gian "${state.timeFilter}"`;
            } else if (state.timeFilter.includes('/')) {
                return `Không có hoạt động vào ngày "${state.timeFilter}"`;
            } else {
                return `Không có hoạt động vào thời gian "${state.timeFilter}"`;
            }
        } else {
            return 'Không có dữ liệu hoạt động';
        }
    }

    // Update range text function
    function updateRangeText(data) {
        if (!rangeText) return;

        if (data.totalElements === 0) {
            rangeText.textContent = 'Hiển thị 0 kết quả';
        } else {
            const start = (data.currentPage * data.size) + 1;
            const end = Math.min((data.currentPage + 1) * data.size, data.totalElements);
            rangeText.textContent = `Hiển thị ${start}-${end} trong tổng số ${data.totalElements} kết quả`;

            // Add filter info
            let filterInfo = [];
            if (state.search) {
                filterInfo.push(`tìm kiếm: "${state.search}"`);
            }
            if (state.deviceFilter !== 'all') {
                const deviceName = getDeviceShortName(parseInt(state.deviceFilter));
                filterInfo.push(`thiết bị: ${deviceName}`);
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
    }

    function updateTableLoading() {
        if (state.loading && tableBody) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="4" class="loading-cell">
                        <div class="loading-spinner"></div>
                        <span>Đang tải dữ liệu...</span>
                    </td>
                </tr>
            `;
        }
    }

    // Action functions
    function performSearch() {
        const searchValue = searchInput ? searchInput.value.trim() : '';
        const deviceValue = deviceFilter ? deviceFilter.value : 'all';
        const timeFilterValue = timeFilter ? timeFilter.value.trim() : '';

        state.search = searchValue;
        state.deviceFilter = deviceValue;
        state.timeFilter = timeFilterValue;
        state.currentPage = 0;

        console.log(`🔍 Performing search: "${searchValue}", device: ${deviceValue}, time: "${timeFilterValue}"`);
        loadData(0, true);
    }

    function clearSearch() {
        if (searchInput) searchInput.value = '';
        if (deviceFilter) deviceFilter.value = 'all';
        if (timeFilter) timeFilter.value = '';

        state.search = '';
        state.deviceFilter = 'all';
        state.timeFilter = '';
        state.currentPage = 0;

        console.log('🧹 Search cleared');
        loadData(0, true);
    }

    // Global loadData function for pagination
    window.loadData = async function(page = 0, updateUrl = false) {
        try {
            const data = await fetchLedEvents(page, state.pageSize, state.search, state.deviceFilter, state.timeFilter);
            buildTable(data);

            if (updateUrl) {
                updateURL();
            }
        } catch (error) {
            console.error('❌ Load data error:', error);
            if (tableBody) {
                tableBody.innerHTML = `
                    <tr>
                        <td colspan="4" class="error-state action-history">
                            Lỗi: ${error.message}
                        </td>
                    </tr>
                `;
            }
        }
    };

    // URL management
    function updateURL() {
        const params = new URLSearchParams();
        params.set('page', state.currentPage.toString());
        params.set('size', state.pageSize.toString());

        if (state.search) params.set('search', state.search);
        if (state.deviceFilter !== 'all') params.set('deviceFilter', state.deviceFilter);
        if (state.timeFilter) params.set('timeFilter', state.timeFilter);

        const newUrl = `${window.location.pathname}?${params.toString()}`;
        window.history.pushState({}, '', newUrl);
    }

    function loadFromURL() {
        const params = new URLSearchParams(window.location.search);
        state.currentPage = parseInt(params.get('page') || '0');
        state.pageSize = parseInt(params.get('size') || '10');
        state.search = params.get('search') || '';
        state.deviceFilter = params.get('deviceFilter') || 'all';
        state.timeFilter = params.get('timeFilter') || '';

        // Update form elements
        if (pageSizeSelect) pageSizeSelect.value = state.pageSize;
        if (searchInput) searchInput.value = state.search;
        if (deviceFilter) deviceFilter.value = state.deviceFilter;
        if (timeFilter) timeFilter.value = state.timeFilter;
    }

    function buildPagination(data) {
        if (!pagination || data.totalPages <= 1) {
            if (pagination) pagination.innerHTML = '';
            return;
        }

        const maxVisible = 7;
        const current = data.currentPage;
        const total = data.totalPages;

        let pages = [];

        if (total <= maxVisible) {
            pages = Array.from({length: total}, (_, i) => i);
        } else {
            let start = Math.max(0, current - Math.floor(maxVisible / 2));
            let end = Math.min(total, start + maxVisible);

            if (end - start < maxVisible) {
                start = Math.max(0, end - maxVisible);
            }

            pages = Array.from({length: end - start}, (_, i) => start + i);
        }

        const buttons = [];

        if (current > 0) {
            buttons.push(`<button class="page-btn" onclick="loadData(${current - 1}, true)">&lt;</button>`);
        }

        pages.forEach(page => {
            const isActive = page === current ? 'active' : '';
            buttons.push(`<button class="page-btn ${isActive}" onclick="loadData(${page}, true)">${page + 1}</button>`);
        });

        if (current < total - 1) {
            buttons.push(`<button class="page-btn" onclick="loadData(${current + 1}, true)">&gt;</button>`);
        }

        pagination.innerHTML = buttons.join('');
    }

    // Event listeners
    function initEvents() {
        // Search events
        if (searchBtn) searchBtn.addEventListener('click', performSearch);
        if (clearBtn) clearBtn.addEventListener('click', clearSearch);

        // Enter key for search
        [searchInput, timeFilter].forEach(input => {
            if (input) {
                input.addEventListener('keydown', (e) => {
                    if (e.key === 'Enter') {
                        e.preventDefault();
                        performSearch();
                    }
                });
            }
        });

        // Device filter change
        if (deviceFilter) {
            deviceFilter.addEventListener('change', performSearch);
        }

        // Page size change
        if (pageSizeSelect) {
            pageSizeSelect.addEventListener('change', (e) => {
                state.pageSize = parseInt(e.target.value);
                state.currentPage = 0;
                loadData(0, true);
            });
        }
    }

    // Initialize
    loadFromURL();
    initEvents();
    loadData();
});
