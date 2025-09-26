// FIXED data-sensor-fixed.js - sửa order của variable declarations:

const API_BASE = '/api/sensor-data';
const DEVICE_NAMES = {
    'Auto Detect': 'Tự động',
    'ID': 'ID',
    'Temperature (°C)': 'Nhiệt độ (°C)',
    'Humidity (%)': 'Độ ẩm (%)',
    'Light Level': 'Cường độ ánh sáng'
};

const state = {
    currentPage: 0,
    pageSize: 10,
    totalPages: 1,
    totalElements: 0,
    search: '',
    searchType: 'Auto Detect',
    timeFilter: '',
    loading: false
};

// FIXED: Declare all variables INSIDE DOMContentLoaded to avoid initialization order issues
document.addEventListener('DOMContentLoaded', () => {
    console.log('📊 Data Sensor page loaded');

    // FIXED: DOM Elements declared INSIDE DOMContentLoaded
    const searchInput = document.getElementById('search');
    const searchTypeSelect = document.getElementById('searchType');
    const timeFilter = document.getElementById('timeFilter');
    const searchBtn = document.getElementById('searchBtn');
    const clearBtn = document.getElementById('clearBtn');
    const tableBody = document.querySelector('tbody');
    const pagination = document.querySelector('.pagination');
    const rangeText = document.querySelector('.range-text');
    const pageSizeSelect = document.getElementById('pageSizeSelect');

    // Validate required elements
    const requiredElements = [
        {element: searchInput, name: 'search input'},
        {element: searchTypeSelect, name: 'search type select'},
        {element: timeFilter, name: 'time filter input'},
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
                    <td colspan="5" style="text-align: center; padding: 20px; color: #ef4444;">
                        Lỗi: Thiếu elements trong HTML: ${missingElements.join(', ')}
                    </td>
                </tr>
            `;
        }
        return;
    }

    // =================== FETCH FUNCTION ===================

    async function fetchSensorData(page = 0, size = 10, search = '', searchType = 'Auto Detect', timeFilter = '') {
        try {
            state.loading = true;
            updateTableLoading();

            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString(),
                searchType: searchType
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
            state.searchType = searchType;
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

    // =================== UI UPDATE FUNCTIONS ===================

    function updateRangeText(data) {
        if (!rangeText) {
            console.warn('⚠️ Range text element not found, skipping update');
            return;
        }

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
            if (state.timeFilter) {
                if (state.timeFilter.includes('-')) {
                    filterInfo.push(`khoảng thời gian: "${state.timeFilter}"`);
                } else if (state.timeFilter.includes('/')) {
                    filterInfo.push(`ngày: "${state.timeFilter}"`);
                } else {
                    filterInfo.push(`thời gian: "${state.timeFilter}"`);
                }
            }
            if (state.searchType !== 'Auto Detect') {
                filterInfo.push(`loại: ${DEVICE_NAMES[state.searchType] || state.searchType}`);
            }

            if (filterInfo.length > 0) {
                rangeText.textContent += ` (${filterInfo.join(', ')})`;
            }
        }
    }
    function formatTemperature(temp) {
        if (temp === null || temp === undefined) return '<span class="text-gray-400">N/A</span>';
        const tempClass = temp >= 20 && temp <= 30 ? 'temp-value normal' : 'temp-value';
        return `<span class="${tempClass}">${temp}°C</span>`;
    }
    function formatHumidity(humidity) {
        if (humidity === null || humidity === undefined) return '<span class="text-gray-400">N/A</span>';
        return `<span class="humidity-value">${humidity}%</span>`;
    }

    function formatLight(light) {
        if (light === null || light === undefined) return '<span class="text-gray-400">N/A</span>';
        const lightClass = light > 80 ? 'light-value bright' : 'light-value';
        return `<span class="${lightClass}">${light} units</span>`;
    }

    function formatTime(timestamp) {
        if (!timestamp) return '<span class="text-gray-400">N/A</span>';
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
    function buildTable(data) {
        if (!tableBody) {
            console.error('❌ Table body not found');
            return;
        }

        if (data.content && data.content.length > 0) {
            tableBody.innerHTML = data.content.map(row => `
            <tr>
                <td><strong>${row.id}</strong></td>
                <td>${formatTemperature(row.temperature)}</td>
                <td>${formatHumidity(row.humidity)}</td>
                <td>${formatLight(row.lightLevel)}</td>
                <td class="time-cell">${formatTime(row.createdAt)}</td>
            </tr>
        `).join('');
        } else {
            let noDataMessage = 'Không có dữ liệu';
            if (state.search) {
                noDataMessage = `Không tìm thấy kết quả cho "${state.search}"`;
            }
            if (state.timeFilter) {
                if (state.timeFilter.includes('-')) {
                    noDataMessage = `Không có dữ liệu trong khoảng thời gian "${state.timeFilter}"`;
                } else if (state.timeFilter.includes('/')) {
                    noDataMessage = `Không có dữ liệu vào ngày "${state.timeFilter}"`;
                } else {
                    noDataMessage = `Không có dữ liệu vào thời gian "${state.timeFilter}"`;
                }
            }

            tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="empty-row">
                    ${noDataMessage}
                </td>
            </tr>
        `;
        }

        updateRangeText(data);
        buildPagination(data);
    }

    function buildPagination(data) {
        if (!pagination) {
            console.warn('⚠️ Pagination element not found');
            return;
        }

        if (data.totalPages <= 1) {
            pagination.innerHTML = '';
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

    function updateTableLoading() {
        if (state.loading && tableBody) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="5" style="text-align: center; padding: 20px;">
                        <div style="display: inline-flex; align-items: center; gap: 8px;">
                            <div style="width: 16px; height: 16px; border: 2px solid #ddd; border-top: 2px solid #4f46e5; border-radius: 50%; animation: spin 1s linear infinite;"></div>
                            Đang tải dữ liệu...
                        </div>
                    </td>
                </tr>
            `;
        }
    }

    // =================== UTILITY FUNCTIONS ===================

    function formatTime(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    }

    // =================== ACTION FUNCTIONS ===================

    function performSearch() {
        const searchValue = searchInput ? searchInput.value.trim() : '';
        const searchTypeValue = searchTypeSelect ? searchTypeSelect.value : 'Auto Detect';
        const timeFilterValue = timeFilter ? timeFilter.value.trim() : '';

        state.search = searchValue;
        state.searchType = searchTypeValue;
        state.timeFilter = timeFilterValue;
        state.currentPage = 0;

        console.log(`🔍 Performing search: "${searchValue}", type: ${searchTypeValue}, time: "${timeFilterValue}"`);
        loadData(0, true);
    }

    function clearSearch() {
        if (searchInput) searchInput.value = '';
        if (searchTypeSelect) searchTypeSelect.value = 'Auto Detect';
        if (timeFilter) timeFilter.value = '';

        state.search = '';
        state.searchType = 'Auto Detect';
        state.timeFilter = '';
        state.currentPage = 0;

        console.log('🧹 Search cleared');
        loadData(0, true);
    }

    // Make loadData available globally for pagination buttons
    window.loadData = async function(page = 0, updateUrl = false) {
        try {
            const data = await fetchSensorData(page, state.pageSize, state.search, state.searchType, state.timeFilter);
            buildTable(data);

            if (updateUrl) {
                updateURL();
            }
        } catch (error) {
            console.error('❌ Load data error:', error);
            if (tableBody) {
                tableBody.innerHTML = `
                    <tr>
                        <td colspan="5" style="text-align: center; padding: 20px; color: #ef4444;">
                            Lỗi: ${error.message}
                        </td>
                    </tr>
                `;
            }
        }
    };

    // =================== URL MANAGEMENT ===================

    function updateURL() {
        const params = new URLSearchParams();
        params.set('page', state.currentPage.toString());
        params.set('size', state.pageSize.toString());

        if (state.search) params.set('search', state.search);
        if (state.searchType !== 'Auto Detect') params.set('searchType', state.searchType);
        if (state.timeFilter) params.set('timeFilter', state.timeFilter);

        const newUrl = `${window.location.pathname}?${params.toString()}`;
        window.history.pushState({}, '', newUrl);
    }

    function loadFromURL() {
        const params = new URLSearchParams(window.location.search);
        state.currentPage = parseInt(params.get('page') || '0');
        state.pageSize = parseInt(params.get('size') || '10');
        state.search = params.get('search') || '';
        state.searchType = params.get('searchType') || 'Auto Detect';
        state.timeFilter = params.get('timeFilter') || '';

        // Update form elements
        if (pageSizeSelect) pageSizeSelect.value = state.pageSize;
        if (searchInput) searchInput.value = state.search;
        if (searchTypeSelect) searchTypeSelect.value = state.searchType;
        if (timeFilter) timeFilter.value = state.timeFilter;
    }

    // =================== EVENT LISTENERS ===================

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

        // Page size change
        if (pageSizeSelect) {
            pageSizeSelect.addEventListener('change', (e) => {
                state.pageSize = parseInt(e.target.value);
                state.currentPage = 0;
                loadData(0, true);
            });
        }

        // Auto-clear
        [searchInput, timeFilter].forEach(input => {
            if (input) {
                input.addEventListener('input', () => {
                    if (searchInput && timeFilter &&
                        searchInput.value.trim() === '' && timeFilter.value.trim() === '' &&
                        state.searchType === 'Auto Detect' &&
                        (state.search !== '' || state.timeFilter !== '' || state.searchType !== 'Auto Detect')) {
                        clearSearch();
                    }
                });
            }
        });
    }

    // =================== INITIALIZATION ===================

    loadFromURL();
    initEvents();
    loadData();

    // Add CSS for loading animation
    if (!document.getElementById('spin-animation')) {
        const style = document.createElement('style');
        style.id = 'spin-animation';
        style.textContent = `
            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
        `;
        document.head.appendChild(style);
    }
});
