// Dashboard JavaScript - Simplified for new layout

// Configuration
const WS_URL = 'ws://localhost:8081/ws';
const API_BASE = '/api';

// Chart instance - only overview chart now
let overviewChart = null;

// Chart data storage
const chartData = {
    labels: [],
    temperature: [],
    humidity: [],
    light: []
};

// State management
const dashboardState = {
    devices: {
        1: { name: 'Đèn phòng khách', type: 'led', state: false },
        2: { name: 'Quạt trần', type: 'fan', state: false },
        3: { name: 'Điều hòa', type: 'ac', state: false }
    },
    sensorData: {
        temperature: null,
        humidity: null,
        lightLevel: null,
        lastUpdate: null
    },
    systemStatus: {
        online: false,
        uptime: 0
    },
    websocket: null,
    chartTimeRange: '6h'
};

// Chart configuration for overview only
const overviewChartConfig = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: {
            display: false // Using custom legend
        },
        tooltip: {
            mode: 'index',
            intersect: false,
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            titleColor: '#374151',
            bodyColor: '#374151',
            borderColor: '#e5e7eb',
            borderWidth: 1,
            cornerRadius: 8,
            displayColors: true,
            titleFont: {
                size: 14,
                weight: '600'
            },
            bodyFont: {
                size: 13
            },
            padding: 12,
            boxPadding: 6
        }
    },
    scales: {
        x: {
            display: true,
            grid: {
                color: '#f3f4f6',
                borderColor: '#e5e7eb',
                borderWidth: 1
            },
            ticks: {
                color: '#9ca3af',
                font: {
                    size: 12,
                    weight: '500'
                },
                maxTicksLimit: 8,
                padding: 8
            },
            border: {
                display: true,
                color: '#e5e7eb'
            }
        },
        y: {
            display: true,
            grid: {
                color: '#f3f4f6',
                borderColor: '#e5e7eb',
                borderWidth: 1
            },
            ticks: {
                color: '#9ca3af',
                font: {
                    size: 12,
                    weight: '500'
                },
                padding: 8,
                callback: function(value) {
                    return value;
                }
            },
            border: {
                display: true,
                color: '#e5e7eb'
            },
            min: 0,
            max: 100
        }
    },
    interaction: {
        mode: 'index',
        intersect: false
    },
    elements: {
        point: {
            radius: 3,
            hoverRadius: 6,
            borderWidth: 2,
            hoverBorderWidth: 3
        },
        line: {
            tension: 0.4,
            borderWidth: 3,
            fill: false
        }
    },
    animation: {
        duration: 1000,
        easing: 'easeInOutQuart'
    }
};

// Initialize Dashboard
document.addEventListener('DOMContentLoaded', () => {
    console.log('🏠 Dashboard initializing...');

    // Set initial loading state
    const statusCard = document.querySelector('.status-card');
    if (statusCard) {
        statusCard.classList.add('loading');
    }

    // Initialize components
    initializeOverviewChart();
    initializeWebSocket();
    loadInitialData();
    setupEventListeners();
    startDataRefresh();

    console.log('✅ Dashboard initialized');
});

// Initialize Only Overview Chart
function initializeOverviewChart() {
    console.log('📊 Initializing clean overview chart...');

    const overviewCtx = document.getElementById('overviewChart');
    if (overviewCtx) {
        overviewChart = new Chart(overviewCtx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [
                    {
                        label: 'Nhiệt độ (°C)',
                        data: [],
                        borderColor: '#ef4444',
                        backgroundColor: 'rgba(239, 68, 68, 0.1)',
                        pointBackgroundColor: '#ef4444',
                        pointBorderColor: '#ffffff',
                        fill: false,
                        tension: 0.4
                    },
                    {
                        label: 'Độ ẩm (%)',
                        data: [],
                        borderColor: '#3b82f6',
                        backgroundColor: 'rgba(59, 130, 246, 0.1)',
                        pointBackgroundColor: '#3b82f6',
                        pointBorderColor: '#ffffff',
                        fill: false,
                        tension: 0.4
                    },
                    {
                        label: 'Ánh sáng (units/10)',
                        data: [],
                        borderColor: '#f59e0b',
                        backgroundColor: 'rgba(245, 158, 11, 0.1)',
                        pointBackgroundColor: '#f59e0b',
                        pointBorderColor: '#ffffff',
                        fill: false,
                        tension: 0.4
                    }
                ]
            },
            options: overviewChartConfig
        });
    }

    console.log('✅ Clean overview chart initialized');
}

// WebSocket Connection (same as before)
function initializeWebSocket() {
    try {
        dashboardState.websocket = new WebSocket(WS_URL);

        dashboardState.websocket.onopen = function(event) {
            console.log('🔌 WebSocket connected');
            updateSystemStatus(true);
        };

        dashboardState.websocket.onmessage = function(event) {
            try {
                const data = JSON.parse(event.data);
                handleWebSocketMessage(data);
            } catch (error) {
                console.error('❌ WebSocket message parse error:', error);
            }
        };

        dashboardState.websocket.onclose = function(event) {
            console.log('🔌 WebSocket disconnected');
            updateSystemStatus(false);
            setTimeout(initializeWebSocket, 3000);
        };

        dashboardState.websocket.onerror = function(error) {
            console.error('❌ WebSocket error:', error);
            updateSystemStatus(false);
        };

    } catch (error) {
        console.error('❌ WebSocket connection failed:', error);
        updateSystemStatus(false);
    }
}

// Handle WebSocket Messages
function handleWebSocketMessage(data) {
    console.log('📨 WebSocket message received:', data);

    if (data.type === 'sensor_data') {
        updateSensorData(data.data);
        addDataToChart(data.data);
    } else if (data.type === 'device_status') {
        updateDeviceStatus(data.deviceId, data.state);
    } else if (data.type === 'system_status') {
        updateSystemStatus(data.online, data.uptime);
    }
}

// Load Initial Data
async function loadInitialData() {
    try {
        await loadCurrentSensorData();
        await loadDeviceStates();
        await loadChartData();

        console.log('✅ Initial data loaded');

    } catch (error) {
        console.error('❌ Failed to load initial data:', error);
        showNotification('Không thể tải dữ liệu ban đầu', 'error');
    }
}
function showChartLoading() {
    const container = document.querySelector('.chart-container-simple');
    if (container) {
        container.innerHTML = `
            <div class="chart-loading-simple">
                <div class="loading-spinner-simple"></div>
                <span>Đang tải dữ liệu biểu đồ...</span>
            </div>
        `;
    }
}
// Load Chart Data
async function loadChartData() {
    try {
        showChartLoading();

        const timeRange = dashboardState.chartTimeRange;
        const response = await fetch(`${API_BASE}/sensor-data/chart-data?range=${timeRange}`);

        if (!response.ok) {
            const fallbackResponse = await fetch(`${API_BASE}/sensor-data/recent`);
            if (!fallbackResponse.ok) throw new Error('Failed to fetch data');

            const data = await fallbackResponse.json();

            // Restore chart canvas if it was replaced by loading
            const container = document.querySelector('.chart-container-simple');
            if (container && !document.getElementById('overviewChart')) {
                container.innerHTML = '<canvas id="overviewChart"></canvas>';
                initializeOverviewChart();
            }

            processChartData(data.slice(-50));
            return;
        }

        const data = await response.json();

        // Restore chart canvas if it was replaced by loading
        const container = document.querySelector('.chart-container-simple');
        if (container && !document.getElementById('overviewChart')) {
            container.innerHTML = '<canvas id="overviewChart"></canvas>';
            initializeOverviewChart();
        }

        processChartData(data);

    } catch (error) {
        console.error('❌ Error loading chart data:', error);

        try {
            const response = await fetch(`${API_BASE}/sensor-data/recent`);
            if (response.ok) {
                const data = await response.json();

                // Restore chart canvas if it was replaced by loading
                const container = document.querySelector('.chart-container-simple');
                if (container && !document.getElementById('overviewChart')) {
                    container.innerHTML = '<canvas id="overviewChart"></canvas>';
                    initializeOverviewChart();
                }

                processChartData(data.slice(-30));
            } else {
                showNoDataChart();
            }
        } catch (fallbackError) {
            console.error('❌ Fallback data loading failed:', fallbackError);
            showNoDataChart();
        }
    }
}

// Process Chart Data
function processChartData(data) {
    if (!data || data.length === 0) {
        showNoDataChart();
        return;
    }

    // Clear existing data
    chartData.labels = [];
    chartData.temperature = [];
    chartData.humidity = [];
    chartData.light = [];

    // Process data points
    data.forEach(point => {
        const time = new Date(point.createdAt);
        const timeStr = time.toLocaleTimeString('vi-VN', {
            hour: '2-digit',
            minute: '2-digit'
        });

        chartData.labels.push(timeStr);
        chartData.temperature.push(point.temperature);
        chartData.humidity.push(point.humidity);
        chartData.light.push(point.lightLevel);
    });

    updateOverviewChart();
}


// Update Overview Chart

function updateOverviewChart() {
    if (overviewChart) {
        overviewChart.data.labels = [...chartData.labels];
        overviewChart.data.datasets[0].data = [...chartData.temperature];
        overviewChart.data.datasets[1].data = [...chartData.humidity];
        // Scale light data to fit with temp/humidity range (divide by 10)
        overviewChart.data.datasets[2].data = chartData.light.map(val => val / 10);

        overviewChart.update('active');
    }
}
// Add new data point to chart
function addDataToChart(newData) {
    const time = new Date(newData.timestamp || Date.now());
    const timeStr = time.toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit'
    });

    // Add new data point
    chartData.labels.push(timeStr);
    chartData.temperature.push(newData.temperature);
    chartData.humidity.push(newData.humidity);
    chartData.light.push(newData.lightLevel);

    // Keep only last 50 points for performance
    if (chartData.labels.length > 50) {
        chartData.labels.shift();
        chartData.temperature.shift();
        chartData.humidity.shift();
        chartData.light.shift();
    }

    updateOverviewChart();
}

// Show no data state for chart
function showNoDataChart() {
    console.log('📈 Showing no data state for chart');

    const container = document.querySelector('.chart-container-simple');
    if (container && !document.getElementById('overviewChart')) {
        container.innerHTML = `
            <div class="chart-loading-simple">
                <span>📊</span>
                <span>Chưa có dữ liệu để hiển thị</span>
            </div>
        `;
        return;
    }

    if (overviewChart) {
        overviewChart.data.labels = [];
        overviewChart.data.datasets.forEach(dataset => {
            dataset.data = [];
        });
        overviewChart.update('none');
    }
}
function testCircleVisual() {
    console.log('🎯 Visual Circle Test:');
    console.log('Expected vs Actual:');

    // Temperature: 25°C out of 50°C = 50%
    const tempValue = 25;
    const tempMax = 50;
    const tempExpected = 50;
    updateCircleProgress('tempProgress', tempValue, tempMax);
    console.log(`🌡️  Temperature: ${tempValue}°C/${tempMax}°C`);
    console.log(`   Expected: 50% | Calculated: ${(tempValue/tempMax*100)}%`);

    // Humidity: 60% out of 100% = 60%
    const humidityValue = 60;
    const humidityMax = 100;
    updateCircleProgress('humidityProgress', humidityValue, humidityMax);
    console.log(`💧 Humidity: ${humidityValue}%/${humidityMax}%`);
    console.log(`   Expected: 60% | Calculated: ${humidityValue}%`);

    // Light: 100 units out of 120 = 83.3%
    const lightValue = 100;
    const lightMax = 120;
    updateCircleProgress('lightProgress', lightValue, lightMax);
    console.log(`☀️  Light: ${lightValue}/${lightMax} units`);
    console.log(`   Expected: 83.3% | Calculated: ${(lightValue/lightMax*100).toFixed(1)}%`);
}

function testCircleProgress() {
    console.log('🧪 Testing Circle Progress Calculations:');

    // Test Temperature: 25°C out of 50°C max
    const temp25 = 25;
    const tempMax = 50;
    const tempPercent = (temp25 / tempMax) * 100;
    console.log(`🌡️ Temperature: ${temp25}°C / ${tempMax}°C = ${tempPercent}% (Expected: 50%)`);

    // Test different temperature values
    const testTemps = [0, 10, 25, 35, 50];
    testTemps.forEach(temp => {
        const percent = (temp / 50) * 100;
        console.log(`  ${temp}°C -> ${percent}% fill`);
    });

    // Test Humidity: 47% out of 100% max
    const humidity47 = 47;
    const humidityMax = 100;
    const humidityPercent = (humidity47 / humidityMax) * 100;
    console.log(`💧 Humidity: ${humidity47}% / ${humidityMax}% = ${humidityPercent}% (Expected: 47%)`);

    // Test Light: 100 out of 120 max
    const light100 = 100;
    const lightMax = 120;
    const lightPercent = (light100 / lightMax) * 100;
    console.log(`☀️ Light: ${light100} / ${lightMax} = ${lightPercent.toFixed(1)}% (Expected: 83.3%)`);
}
// Load Current Sensor Data
async function loadCurrentSensorData() {
    try {
        const response = await fetch(`${API_BASE}/sensor-data/recent`);
        if (!response.ok) throw new Error('Failed to fetch sensor data');

        const data = await response.json();
        if (data && data.length > 0) {
            const latest = data[0];
            updateSensorData({
                temperature: latest.temperature,
                humidity: latest.humidity,
                lightLevel: latest.lightLevel,
                timestamp: latest.createdAt
            });
        }
    } catch (error) {
        console.error('❌ Error loading sensor data:', error);
    }
}

// Load Device States
async function loadDeviceStates() {
    try {
        const response = await fetch(`${API_BASE}/led-events/latest-states`);
        if (!response.ok) throw new Error('Failed to fetch device states');

        const states = await response.json();

        Object.keys(dashboardState.devices).forEach(deviceId => {
            const state = states[deviceId];
            if (state !== undefined) {
                updateDeviceStatus(parseInt(deviceId), state);
            }
        });

    } catch (error) {
        console.error('❌ Error loading device states:', error);
        Object.keys(dashboardState.devices).forEach(deviceId => {
            updateDeviceStatus(parseInt(deviceId), false);
        });
    }
}

// Update Sensor Data
function updateSensorData(data) {
    console.log('📊 Updating sensor data:', data);

    if (data.temperature !== undefined) {
        dashboardState.sensorData.temperature = data.temperature;
        console.log(`🌡️ Temperature updated: ${data.temperature}°C (${(data.temperature/50*100).toFixed(1)}% of 50°C scale)`);
    }

    if (data.humidity !== undefined) {
        dashboardState.sensorData.humidity = data.humidity;
        console.log(`💧 Humidity updated: ${data.humidity}% (${data.humidity}% of 100% scale)`);
    }

    if (data.lightLevel !== undefined) {
        dashboardState.sensorData.lightLevel = data.lightLevel;
        console.log(`☀️ Light updated: ${data.lightLevel} units (${(data.lightLevel/120*100).toFixed(1)}% of 120 scale)`);
    }

    updateSensorUI();
}

// Update Sensor UI
function updateSensorUI() {
    const { temperature, humidity, lightLevel } = dashboardState.sensorData;

    // Temperature
    const tempElement = document.getElementById('currentTemp');
    const tempStatusElement = document.getElementById('tempStatus');
    if (tempElement && temperature !== null) {
        tempElement.textContent = `${temperature}°C`;
        tempStatusElement.textContent = getTemperatureStatus(temperature);
    }

    // Humidity
    const humidityElement = document.getElementById('currentHumidity');
    const humidityStatusElement = document.getElementById('humidityStatus');
    if (humidityElement && humidity !== null) {
        humidityElement.textContent = `${humidity}%`;
        humidityStatusElement.textContent = getHumidityStatus(humidity);
    }

    // Light Level
    const lightElement = document.getElementById('currentLight');
    const lightStatusElement = document.getElementById('lightStatus');
    if (lightElement && lightLevel !== null) {
        lightElement.textContent = `${lightLevel} units`;
        lightStatusElement.textContent = getLightStatus(lightLevel);
    }
}

// Status Helper Functions
function getTemperatureStatus(temp) {
    if (temp < 20) return 'Lạnh';
    if (temp > 30) return 'Nóng';
    return 'Bình thường';
}

function getHumidityStatus(humidity) {
    if (humidity < 40) return 'Khô';
    if (humidity > 70) return 'Ẩm ướt';
    return 'Bình thường';
}

function getLightStatus(light) {
    if (light < 30) return 'Tối';
    if (light > 80) return 'Sáng';
    return 'Bình thường';
}

// Updated selectors for perfect horizontal layout
// Updated selectors for right column layout
function updateDeviceStatus(deviceId, state) {
    if (dashboardState.devices[deviceId]) {
        dashboardState.devices[deviceId].state = state;

        // Updated selector for right status badges
        const statusElement = document.getElementById(`device${deviceId}Status`);
        const statusBadge = statusElement?.querySelector('.status-badge-right');

        if (statusBadge) {
            statusBadge.className = `status-badge-right ${state ? 'on' : 'off'}`;
            statusBadge.textContent = state ? 'BẬT' : 'TẮT';
        }

        // Updated selector for right device items
        const deviceItem = document.querySelector(`[data-device="${deviceId}"]`);
        if (deviceItem) {
            deviceItem.classList.toggle('device-on', state);
        }
    }
}



// Update System Status
function updateSystemStatus(online, uptime = null) {
    dashboardState.systemStatus.online = online;
    if (uptime !== null) {
        dashboardState.systemStatus.uptime = uptime;
    }

    // Update status card
    const statusCard = document.querySelector('.status-card');
    const statusText = document.getElementById('systemStatusText');
    const statusDot = document.getElementById('systemStatusDot');

    if (statusCard && statusText && statusDot) {
        // Remove all status classes
        statusCard.classList.remove('online', 'offline', 'loading');
        statusDot.classList.remove('online', 'offline');

        if (online) {
            statusCard.classList.add('online');
            statusDot.classList.add('online');
            statusText.textContent = 'Hoạt động';
        } else {
            statusCard.classList.add('offline');
            statusDot.classList.add('offline');
            statusText.textContent = 'Offline';
        }
    }
}
function updateCircleProgressReverse(circleId, percentage, maxValue = 100) {
    const circle = document.getElementById(circleId);
    if (!circle) return;

    const normalizedPercentage = Math.min(Math.max((percentage / maxValue) * 100, 0), 100);
    const circumference = 188.4;

    // Reverse calculation for different visual effect
    const progress = (normalizedPercentage / 100) * circumference;
    const offset = progress; // Different offset calculation

    circle.style.strokeDasharray = circumference;
    circle.style.strokeDashoffset = offset;

    console.log(`🔧 Reverse ${circleId}: ${normalizedPercentage}% -> offset: ${offset}`);
}
window.testCircles = function() {
    console.log('🧪 Manual Circle Test:');

    // Test temperature at different values
    console.log('Testing Temperature Circle:');
    [0, 12.5, 25, 37.5, 50].forEach(temp => {
        updateCircleProgress('tempProgress', temp, 50);
        console.log(`${temp}°C -> ${temp/50*100}% fill`);
    });

    // Test humidity at different values
    setTimeout(() => {
        console.log('Testing Humidity Circle:');
        [0, 25, 47, 75, 100].forEach(humidity => {
            updateCircleProgress('humidityProgress', humidity, 100);
            console.log(`${humidity}% -> ${humidity}% fill`);
        });
    }, 2000);

    // Test light at different values
    setTimeout(() => {
        console.log('Testing Light Circle:');
        [0, 30, 60, 100, 120].forEach(light => {
            updateCircleProgress('lightProgress', light, 120);
            console.log(`${light} units -> ${(light/120*100).toFixed(1)}% fill`);
        });
    }, 4000);
};
if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    setTimeout(() => {
        testCircleProgress();
    }, 2000);
}
window.adjustCircle = function(circleId, value, max) {
    console.log(`🎛️ Manual adjustment: ${circleId}`);
    updateCircleProgress(circleId, value, max);

    // Show current state
    const circle = document.getElementById(circleId);
    if (circle) {
        console.log(`Current dasharray: ${circle.style.strokeDasharray}`);
        console.log(`Current dashoffset: ${circle.style.strokeDashoffset}`);
    }
};
// Device Control Function
window.controlDevice = async function(deviceId, state) {
    console.log(`🎛️ Controlling device ${deviceId}: ${state ? 'ON' : 'OFF'}`);

    // Updated selector for device compact items
    const deviceCompact = document.querySelector(`[data-device="${deviceId}"]`);
    if (deviceCompact) {
        deviceCompact.classList.add('updating');
    }

    try {
        const response = await fetch(`${API_BASE}/led-control/${deviceId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ state: state })
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: Control request failed`);
        }

        updateDeviceStatus(deviceId, state);

        const deviceName = dashboardState.devices[deviceId]?.name || `LED ${deviceId}`;
        showNotification(`${deviceName} đã ${state ? 'bật' : 'tắt'}`, 'success');

        console.log(`✅ Device ${deviceId} controlled successfully`);

    } catch (error) {
        console.error(`❌ Device control failed:`, error);
        updateDeviceStatus(deviceId, !state);
        showNotification('Không thể điều khiển thiết bị', 'error');

    } finally {
        if (deviceCompact) {
            deviceCompact.classList.remove('updating');
        }
    }
};

// Event Listeners
function setupEventListeners() {
    // Refresh button
    const refreshBtn = document.getElementById('refreshBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', () => {
            console.log('🔄 Manual refresh triggered');

            // Add loading state
            refreshBtn.style.pointerEvents = 'none';
            refreshBtn.style.opacity = '0.7';

            // Enhanced rotation animation
            const icon = refreshBtn.querySelector('.refresh-icon-modern');
            if (icon) {
                icon.style.transform = 'rotate(720deg)';
            }

            // Load data
            loadInitialData().finally(() => {
                // Reset button state
                setTimeout(() => {
                    refreshBtn.style.pointerEvents = 'auto';
                    refreshBtn.style.opacity = '1';
                    if (icon) {
                        icon.style.transform = 'rotate(0deg)';
                    }
                }, 1000);
            });
        });
    }
    }

    // Chart time range selector
    const chartTimeRange = document.getElementById('chartTimeRange');
    if (chartTimeRange) {
        chartTimeRange.addEventListener('change', (e) => {
            dashboardState.chartTimeRange = e.target.value;
            console.log(`📊 Chart time range changed to: ${e.target.value}`);
            loadChartData();
        });
    }


// Auto Refresh
function startDataRefresh() {
    // Refresh sensor data every 30 seconds
    setInterval(loadCurrentSensorData, 30000);

    // Refresh chart data every 5 minutes
    setInterval(loadChartData, 300000);
}

// Notification System
function showNotification(message, type = 'info') {
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notification => notification.remove());

    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.classList.add('show');
    }, 100);

    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
}
// Updated dashboard.js with circular visualization

// Function to update circular progress
function updateCircleProgress(circleId, percentage, maxValue = 100) {
    const circle = document.getElementById(circleId);
    if (!circle) {
        console.warn(`❌ Circle not found: ${circleId}`);
        return;
    }

    // Calculate percentage (0-100)
    const normalizedPercentage = Math.min(Math.max((percentage / maxValue) * 100, 0), 100);

    // Circle circumference for radius 30 = 2 * PI * 30 ≈ 188.4
    const circumference = 188.4;
    const progress = (normalizedPercentage / 100) * circumference;
    const offset = circumference - progress;

    // Apply progress
    circle.style.strokeDasharray = circumference;
    circle.style.strokeDashoffset = offset;

    // Debug logging
    console.log(`🔧 ${circleId}: ${percentage}/${maxValue} = ${normalizedPercentage.toFixed(1)}% -> offset: ${offset.toFixed(1)}`);

    // Visual indicator in console
    const fillBar = '█'.repeat(Math.round(normalizedPercentage / 5)) + '░'.repeat(20 - Math.round(normalizedPercentage / 5));
    console.log(`   Visual: [${fillBar}] ${normalizedPercentage.toFixed(1)}%`);
}

// Update Sensor UI with circular visualization
function updateSensorUI() {
    const { temperature, humidity, lightLevel } = dashboardState.sensorData;

    // Temperature (0-50°C range) - 25°C = 50% of circle
    const tempElement = document.getElementById('currentTemp');
    if (tempElement && temperature !== null) {
        tempElement.textContent = Math.round(temperature);
        // Use 50°C as max range for temperature circle
        updateCircleProgress('tempProgress', temperature, 50);

        console.log(`🌡️ Temperature: ${temperature}°C -> ${(temperature/50*100).toFixed(1)}% of circle`);
    }

    // Humidity (0-100% range) - Direct percentage
    const humidityElement = document.getElementById('currentHumidity');
    if (humidityElement && humidity !== null) {
        humidityElement.textContent = Math.round(humidity);
        // Use 100% as max range for humidity circle
        updateCircleProgress('humidityProgress', humidity, 100);

        console.log(`💧 Humidity: ${humidity}% -> ${humidity}% of circle`);
    }

    // Light Level (0-120 range)
    const lightElement = document.getElementById('currentLight');
    if (lightElement && lightLevel !== null) {
        lightElement.textContent = Math.round(lightLevel);
        // Use 120 as max range for light circle
        updateCircleProgress('lightProgress', lightLevel, 120);

        console.log(`☀️ Light: ${lightLevel} units -> ${(lightLevel/120*100).toFixed(1)}% of circle`);
    }
}


// Initialize circular progress on load
document.addEventListener('DOMContentLoaded', () => {
    console.log('🏠 Dashboard initializing...');

    // Set initial loading state
    setTimeout(() => {
        // Set exact values như trong image
        updateCircleProgress('tempProgress', 25, 50);      // 25°C = 50%
        updateCircleProgress('humidityProgress', 60, 100); // 60% = 60%
        updateCircleProgress('lightProgress', 100, 120);   // 100 units = 83.3%

        // Run visual test
        testCircleVisual();

        // Provide manual test instructions
        console.log('🧪 Manual Testing Available:');
        console.log('  adjustCircle("tempProgress", 25, 50)');
        console.log('  adjustCircle("humidityProgress", 60, 100)');
        console.log('  adjustCircle("lightProgress", 100, 120)');

    }, 1000);

    // Initialize components
    initializeOverviewChart();
    initializeWebSocket();
    loadInitialData();
    setupEventListeners();
    startDataRefresh();

    console.log('✅ Dashboard initialized');
});

// Handle WebSocket Messages with circular updates
function handleWebSocketMessage(data) {
    console.log('📨 WebSocket message received:', data);

    if (data.type === 'sensor_data') {
        updateSensorData(data.data);
        addDataToChart(data.data);

        // Update circular visualization với correct ranges
        updateSensorUI();
    } else if (data.type === 'device_status') {
        updateDeviceStatus(data.deviceId, data.state);
    } else if (data.type === 'system_status') {
        updateSystemStatus(data.online, data.uptime);
    }
}

// Rest of the JavaScript remains the same...
// Function to update individual circle progress
function updateCircleProgress(circleId, percentage, maxValue = 100) {
    const circle = document.getElementById(circleId);
    if (!circle) return;

    // Calculate percentage (0-100)
    const normalizedPercentage = Math.min(Math.max((percentage / maxValue) * 100, 0), 100);

    // Circle circumference for radius 35 = 2 * PI * 35 ≈ 220
    const circumference = 220;
    const offset = circumference - (normalizedPercentage / 100) * circumference;

    circle.style.strokeDashoffset = offset;
}

// Update Sensor UI with individual circle progress
function updateSensorUI() {
    const { temperature, humidity, lightLevel } = dashboardState.sensorData;

    // Temperature (0-50°C range)
    const tempElement = document.getElementById('currentTemp');
    if (tempElement && temperature !== null) {
        tempElement.textContent = Math.round(temperature);
        updateCircleProgress('tempProgress', temperature, 50);
    }

    // Humidity (0-100% range)
    const humidityElement = document.getElementById('currentHumidity');
    if (humidityElement && humidity !== null) {
        humidityElement.textContent = Math.round(humidity);
        updateCircleProgress('humidityProgress', humidity, 100);
    }

    // Light Level (0-120 range)
    const lightElement = document.getElementById('currentLight');
    if (lightElement && lightLevel !== null) {
        lightElement.textContent = Math.round(lightLevel);
        updateCircleProgress('lightProgress', lightLevel, 120);
    }
}

// Initialize with default progress
document.addEventListener('DOMContentLoaded', () => {
    console.log('🏠 Dashboard initializing...');

    // Set initial loading state
    const statusCard = document.querySelector('.status-card');
    if (statusCard) {
        statusCard.classList.add('loading');
    }

    // Initialize circle progress with default values
    updateCircleProgress('tempProgress', 0, 50);
    updateCircleProgress('humidityProgress', 0, 100);
    updateCircleProgress('lightProgress', 0, 120);

    // Initialize components
    initializeOverviewChart();
    initializeWebSocket();
    loadInitialData();
    setupEventListeners();
    startDataRefresh();

    console.log('✅ Dashboard initialized');
});

// Rest of JavaScript remains the same...

// Error Handling
window.addEventListener('error', (event) => {
    console.error('❌ Global error:', event.error);
});

window.addEventListener('unhandledrejection', (event) => {
    console.error('❌ Unhandled promise rejection:', event.reason);
});
