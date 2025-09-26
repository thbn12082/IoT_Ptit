const API_BASE = '/api/sensors';

const state = {
    currentPage: 0,
    pageSize: 10,
    totalPages: 1,
    totalElements: 0,
    search: '',
    searchType: 'auto',
    loading: false
};

const tableBody = document.getElementById('tableBody');
const pagination = document.getElementById('pagination');
const pageSizeSelect = document.getElementById('pageSize');
const rangeText = document.getElementById('rangeText');
const searchInput = document.getElementById('search');
const searchBtn = document.getElementById('searchBtn');

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

// Fetch paginated data from backend
async function fetchSensorData(page = 0, size = 10, search = '', searchType = 'auto') {
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

        console.log(`Fetching: page=${page}, size=${size}, search="${search}", type=${searchType}`);

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
        state.search = search;
        state.searchType = searchType;

        console.log(`Loaded ${data.content.length} records from backend (Total: ${data.totalElements})`);
        return data;
    } catch (error) {
        console.error('Fetch error:', error);
        throw error;
    } finally {
        state.loading = false;
    }
}

// Add search type selector
function addSearchTypeSelector() {
    const searchContainer = document.querySelector('.search-container');
    if (!searchContainer) {
        console.warn('Search container not found, creating one');
        return;
    }

    // Check if selector already exists
    if (document.getElementById('searchType')) {
        return;
    }

    const searchTypeSelect = document.createElement('select');
    searchTypeSelect.id = 'searchType';
    searchTypeSelect.className = 'form-select';
    searchTypeSelect.innerHTML = `
        <option value="auto">Auto Detect</option>
        <option value="id">ID</option>
        <option value="temperature">Temperature (°C)</option>
        <option value="humidity">Humidity (%)</option>
        <option value="light">Light Level</option>
        <option value="temp_range">Temp Range (min-max)</option>
        <option value="humidity_range">Humidity Range (min-max)</option>
    `;

    searchContainer.appendChild(searchTypeSelect);

    searchTypeSelect.addEventListener('change', () => {
        state.searchType = searchTypeSelect.value;
        console.log(`Search type changed to: ${state.searchType}`);
        if (state.search) {
            performSearch();
        }
    });
} // FIX: Đóng function này

// Add search examples
function addSearchExamples() {
    const searchContainer = document.querySelector('.search-container');
    if (!searchContainer) return;

    // Check if examples already exist
    if (document.querySelector('.search-examples')) {
        return;
    }

    const examples = document.createElement('div');
    examples.className = 'search-examples';
    examples.style.marginTop = '5px';
    examples.innerHTML = `
        <small style="color: #666;">
            Examples: "23" (exact temp), "50-70" (humidity range), "123" (ID contains)
        </small>
    `;

    searchContainer.appendChild(examples);
}

// Update table with loading state
function updateTableLoading() {
    if (state.loading) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="muted" style="text-align: center; padding: 20px;">
                    <i class="fa fa-spinner fa-spin"></i> Đang tải...
                </td>
            </tr>
        `;
    }
}

// Build table and pagination
function buildTable(data) {
    // Update table content
    if (data.content && data.content.length > 0) {
        tableBody.innerHTML = data.content.map(row => `
            <tr>
                <td>${row.id}</td>
                <td>${row.temperature?.toFixed(1) || 'N/A'}°C</td>
                <td>${row.humidity?.toFixed(1) || 'N/A'}%</td>
                <td>${row.lightLevel || 'N/A'} units</td>
                <td>${formatTime(row.createdAt)}</td>
            </tr>
        `).join('');
    } else {
        tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="muted" style="text-align: center; padding: 20px;">
                    ${state.search ? `Không tìm thấy kết quả cho "${state.search}"` : 'Không có dữ liệu'}
                </td>
            </tr>
        `;
    }

    // Update range text
    if (data.totalElements === 0) {
        rangeText.textContent = state.search ?
            `Không tìm thấy kết quả cho "${state.search}"` :
            'Hiển thị 0 kết quả';
    } else {
        const start = (data.currentPage * data.size) + 1;
        const end = Math.min((data.currentPage + 1) * data.size, data.totalElements);
        rangeText.textContent = `Hiển thị ${start}-${end} trong tổng số ${data.totalElements} kết quả`;

        if (state.search) {
            rangeText.textContent += ` (tìm kiếm: "${state.search}")`;
        }
    }

    // Build pagination
    buildPagination(data);
}

// Build pagination controls
function buildPagination(data) {
    pagination.innerHTML = '';

    if (data.totalPages <= 1) return;

    const addBtn = (label, page, disabled = false, active = false) => {
        const btn = document.createElement('button');
        btn.className = `page-btn ${active ? 'active' : ''}`;
        btn.textContent = label;
        btn.disabled = disabled;
        btn.addEventListener('click', () => {
            if (state.currentPage !== page) {
                loadData(page);
            }
        });
        pagination.appendChild(btn);
    };

    // Previous button
    addBtn('‹', Math.max(0, state.currentPage - 1), data.first);

    // Page numbers
    const windowSize = 5;
    const startPage = Math.max(0, state.currentPage - 2);
    const endPage = Math.min(data.totalPages - 1, startPage + windowSize - 1);

    if (startPage > 0) {
        addBtn('1', 0);
        if (startPage > 1) {
            const span = document.createElement('span');
            span.textContent = '...';
            span.className = 'page-ellipsis';
            pagination.appendChild(span);
        }
    }

    for (let p = startPage; p <= endPage; p++) {
        addBtn((p + 1).toString(), p, false, p === state.currentPage);
    }

    if (endPage < data.totalPages - 1) {
        if (endPage < data.totalPages - 2) {
            const span = document.createElement('span');
            span.textContent = '...';
            span.className = 'page-ellipsis';
            pagination.appendChild(span);
        }
        addBtn(data.totalPages.toString(), data.totalPages - 1);
    }

    // Next button
    addBtn('›', Math.min(data.totalPages - 1, state.currentPage + 1), data.last);
}

// Load data function - FIX: Thêm searchType parameter
async function loadData(page = 0) {
    try {
        const data = await fetchSensorData(page, state.pageSize, state.search, state.searchType);
        buildTable(data);
    } catch (error) {
        console.error('Load data error:', error);
        tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="muted" style="text-align: center; padding: 20px; color: red;">
                    <i class="fa fa-exclamation-triangle"></i> Lỗi tải dữ liệu: ${error.message}
                </td>
            </tr>
        `;

        // Clear pagination on error
        pagination.innerHTML = '';
        rangeText.textContent = 'Lỗi tải dữ liệu';
    }
}

// Search function - FIX: Cập nhật searchType từ selector
function performSearch() {
    const searchValue = searchInput.value.trim();
    const searchTypeSelect = document.getElementById('searchType');
    const searchType = searchTypeSelect ? searchTypeSelect.value : 'auto';

    state.search = searchValue;
    state.searchType = searchType;

    console.log(`Performing search: "${searchValue}" with type: ${searchType}`);
    loadData(0); // Reset to first page when searching
}

// Clear search function
function clearSearch() {
    searchInput.value = '';
    state.search = '';
    state.searchType = 'auto';

    const searchTypeSelect = document.getElementById('searchType');
    if (searchTypeSelect) {
        searchTypeSelect.value = 'auto';
    }

    console.log('Search cleared');
    loadData(0);
}

// Initialize event listeners
function initEvents() {
    // Page size change
    pageSizeSelect.addEventListener('change', () => {
        const newSize = Number(pageSizeSelect.value);
        console.log(`Page size changed to: ${newSize}`);
        state.pageSize = newSize;
        loadData(0); // Reset to first page
    });

    // Search button
    searchBtn.addEventListener('click', performSearch);

    // Search on Enter key
    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            performSearch();
        }
    });

    // Clear search when input is empty
    searchInput.addEventListener('input', (e) => {
        if (e.target.value.trim() === '' && state.search !== '') {
            clearSearch();
        }
    });

    // Add clear button
    const clearBtn = document.createElement('button');
    clearBtn.textContent = 'Clear';
    clearBtn.className = 'btn btn-secondary';
    clearBtn.style.marginLeft = '5px';
    clearBtn.addEventListener('click', clearSearch);

    if (searchBtn.parentNode) {
        searchBtn.parentNode.insertBefore(clearBtn, searchBtn.nextSibling);
    }
}

// Initialize the page
async function init() {
    try {
        console.log('Initializing data-sensor page...');

        // Set initial page size
        state.pageSize = Number(pageSizeSelect.value);

        // Add UI components
        addSearchTypeSelector();
        addSearchExamples();

        // Initialize events
        initEvents();

        // Load initial data
        await loadData(0);

        console.log('Data-sensor page initialized successfully');
    } catch (error) {
        console.error('Initialization error:', error);
        tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="muted" style="text-align: center; padding: 20px; color: red;">
                    <i class="fa fa-exclamation-triangle"></i> Lỗi khởi tạo: ${error.message}
                </td>
            </tr>
        `;
    }
}

// Start the application
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM loaded, starting application...');
    init();
});

// Export functions for debugging (optional)
window.sensorDataDebug = {
    state,
    loadData,
    performSearch,
    clearSearch,
    fetchSensorData
};
