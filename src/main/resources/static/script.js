// Initialize WebSocket connection
let stompClient = null;
const socket = new SockJS('/ws');
stompClient = Stomp.over(socket);

// Common chart options
const commonChartOptions = {
    responsive: true,
    animation: {
        duration: 1000,
        easing: 'easeInOutQuart'
    },
    scales: {
        x: {
            display: true,
            grid: {
                display: false
            },
            title: {
                display: true,
                text: 'Time',
                font: {
                    size: 14,
                    weight: 'bold'
                }
            }
        },
        y: {
            display: true,
            grid: {
                color: 'rgba(0,0,0,0.05)'
            },
            title: {
                display: true,
                text: 'Value',
                font: {
                    size: 14,
                    weight: 'bold'
                }
            }
        }
    },
    plugins: {
        legend: {
            display: false
        },
        tooltip: {
            backgroundColor: 'rgba(255,255,255,0.9)',
            titleColor: '#1a2035',
            bodyColor: '#666',
            borderColor: 'rgba(0,0,0,0.1)',
            borderWidth: 1,
            padding: 12,
            displayColors: false
        }
    },
    interaction: {
        intersect: false,
        mode: 'index'
    }
};

// Temperature Chart Configuration
const temperatureConfig = {
    type: 'line',
    data: {
        labels: [],
        datasets: [{
            label: 'Temperature (°C)',
            borderColor: '#ff6b6b',
            backgroundColor: 'rgba(255, 107, 107, 0.1)',
            data: [],
            fill: true,
            tension: 0.4,
            borderWidth: 2,
            pointRadius: 4,
            pointBackgroundColor: '#ff6b6b'
        }]
    },
    options: {
        ...commonChartOptions,
        scales: {
            ...commonChartOptions.scales,
            y: {
                ...commonChartOptions.scales.y,
                title: {
                    ...commonChartOptions.scales.y.title,
                    text: 'Nhiệt độ (°C)'
                }
            }
        }
    }
};

// Humidity Chart Configuration
const humidityConfig = {
    type: 'line',
    data: {
        labels: [],
        datasets: [{
            label: 'Humidity (%)',
            borderColor: '#4dabf7',
            backgroundColor: 'rgba(77, 171, 247, 0.1)',
            data: [],
            fill: true,
            tension: 0.4,
            borderWidth: 2,
            pointRadius: 4,
            pointBackgroundColor: '#4dabf7'
        }]
    },
    options: {
        ...commonChartOptions,
        scales: {
            ...commonChartOptions.scales,
            y: {
                ...commonChartOptions.scales.y,
                title: {
                    ...commonChartOptions.scales.y.title,
                    text: 'Độ ẩm (%)'
                }
            }
        }
    }
};

const lightConfig = {
    type: 'line',
    data: {
        labels: [],
        datasets: [{
            label: 'Light (lux)',
            borderColor: '#ffd43b',
            backgroundColor: 'rgba(255, 212, 59, 0.1)',
            data: [],
            fill: true,
            tension: 0.4,
            borderWidth: 2,
            pointRadius: 4,
            pointBackgroundColor: '#ffd43b'
        }]
    },
    options: {
        ...commonChartOptions,
        scales: {
            ...commonChartOptions.scales,
            y: {
                ...commonChartOptions.scales.y,
                title: {
                    ...commonChartOptions.scales.y.title,
                    text: 'ÁNh sáng (lux)'
                }
            }
        }
    }
};

// Initialize charts
const temperatureChart = new Chart(
    document.getElementById('temperatureChart'),
    temperatureConfig
);

const humidityChart = new Chart(
    document.getElementById('humidityChart'),
    humidityConfig
);

const lightChart = new Chart(
    document.getElementById('lightChart'),
    lightConfig
);

// Connect to WebSocket
stompClient.connect({}, function (frame) {
    console.log('Connected to WebSocket');

    // Subscribe to sensor data topic
    stompClient.subscribe('/topic/sensor-data', function (message) {
        const data = JSON.parse(message.body);
        updateDashboard(data);
    });

    // Initialize LED controls
    initializeLEDControls();
});

function updateLEDButtonStatus(ledText, isOn) {
    // Find button by text content
    const buttons = document.querySelectorAll('.device-btn');
    let targetButton = null;

    buttons.forEach(button => {
        if (button.textContent.trim() === ledText) {
            targetButton = button;
        }
    });

    if (targetButton) {
        // Remove all LED state classes
        targetButton.classList.remove('led-on', 'led-off', 'led-connecting');

        // Add appropriate state class
        if (isOn) {
            targetButton.classList.add('led-on');
        } else {
            targetButton.classList.add('led-off');
        }
    }
}

function setLEDButtonConnecting(ledText) {
    const buttons = document.querySelectorAll('.device-btn');
    let targetButton = null;

    buttons.forEach(button => {
        if (button.textContent.trim() === ledText) {
            targetButton = button;
        }
    });

    if (targetButton) {
        targetButton.classList.remove('led-on', 'led-off');
        targetButton.classList.add('led-connecting');
    }
}

function updateDashboard(data) {
    // Cập nhật giá trị sensor và thanh progress
    updateSensorDisplay('temperature', data.temperature, 50);
    updateSensorDisplay('humidity', data.humidity, 100);
    updateSensorDisplay('light', data.light, 1000);

    // Update charts
    const currentTime = new Date().toLocaleTimeString();

    // Update temperature chart
    if (temperatureChart.data.labels.length > 30) {
        temperatureChart.data.labels.shift();
        temperatureChart.data.datasets[0].data.shift();
    }
    temperatureChart.data.labels.push(currentTime);
    temperatureChart.data.datasets[0].data.push(data.temperature);
    temperatureChart.update('none'); // Use 'none' mode for better performance

    // Update humidity chart
    if (humidityChart.data.labels.length > 30) {
        humidityChart.data.labels.shift();
        humidityChart.data.datasets[0].data.shift();
    }
    humidityChart.data.labels.push(currentTime);
    humidityChart.data.datasets[0].data.push(data.humidity);
    humidityChart.update('none');

    // Update light chart
    if (lightChart.data.labels.length > 30) {
        lightChart.data.labels.shift();
        lightChart.data.datasets[0].data.shift();
    }
    lightChart.data.labels.push(currentTime);
    lightChart.data.datasets[0].data.push(data.light);
    lightChart.update('none');
}

function updateSensorDisplay(type, value, maxValue) {
    // Cập nhật giá trị
    document.querySelector(`.sensor-circle.${type} .sensor-value`).textContent = type === 'light' ? Math.round(value) : value.toFixed(1);

    // Cập nhật progress circle
    const percentage = (value / maxValue) * 100;
    const sensorCircle = document.querySelector(`.sensor-circle.${type}`);
    sensorCircle.style.setProperty('--progress', `${Math.min(percentage, 100)}%`);
}


function initializeLEDControls() {
    const ledButtons = document.querySelectorAll('.device-btn');
    const ledStates = {
        'LED 1': false,
        'LED 2': false,
        'LED 3': false
    };

    console.log('Initializing LED controls...');

    // Subscribe to LED status updates from server
    stompClient.subscribe('/topic/led-status', function (message) {
        console.log('Received LED status update:', message.body);
        const status = JSON.parse(message.body);

        // Update the corresponding LED button based on the LED number
        const ledText = `LED ${status.ledNumber}`;
        updateLEDButtonStatus(ledText, status.stateOn);
        ledStates[ledText] = status.stateOn;
    });

    ledButtons.forEach(button => {
        const buttonText = button.textContent.trim();
        console.log('Setting up button:', buttonText);

        if (buttonText !== 'All LEDs') {
            button.addEventListener('click', function () {
                const currentState = ledStates[buttonText] || false;
                const newState = !currentState;

                console.log(`Clicking ${buttonText}: ${currentState} -> ${newState}`);

                // Show connecting state immediately
                setLEDButtonConnecting(buttonText);

                const message = {
                    deviceId: buttonText, // Send "LED 1", "LED 2", "LED 3"
                    state: newState
                };

                try {
                    // Send control command to server
                    stompClient.send("/app/led-control", {}, JSON.stringify(message));
                    console.log('Sent LED control message:', message);

                    // Update state optimistically after a short delay
                    setTimeout(() => {
                        updateLEDButtonStatus(buttonText, newState);
                        ledStates[buttonText] = newState;
                    }, 500);

                } catch (error) {
                    console.error('Failed to send LED control message:', error);
                    // Revert to previous state on error
                    updateLEDButtonStatus(buttonText, currentState);
                }
            });
        } else {
            // Handle All LEDs button
            button.addEventListener('click', function () {
                const allCurrentlyOn = Object.values(ledStates).every(state => state);
                const newState = !allCurrentlyOn;

                console.log(`Clicking All LEDs: all currently ${allCurrentlyOn ? 'ON' : 'OFF'} -> ${newState ? 'ON' : 'OFF'}`);

                // Show connecting state for all LEDs
                Object.keys(ledStates).forEach(ledText => {
                    setLEDButtonConnecting(ledText);
                });

                try {
                    // Send control command for all LEDs
                    const message = {
                        deviceId: 'all',
                        state: newState
                    };
                    stompClient.send("/app/led-control", {}, JSON.stringify(message));
                    console.log('Sent All LEDs control message:', message);

                    // Update all LED button states after a short delay
                    setTimeout(() => {
                        Object.keys(ledStates).forEach(ledText => {
                            updateLEDButtonStatus(ledText, newState);
                            ledStates[ledText] = newState;
                        });
                    }, 500);

                } catch (error) {
                    console.error('Failed to send All LEDs control message:', error);
                    // Revert to previous states on error
                    Object.keys(ledStates).forEach(ledText => {
                        updateLEDButtonStatus(ledText, ledStates[ledText]);
                    });
                }
            });
        }
    });

    // Initialize all LED buttons to OFF state
    Object.keys(ledStates).forEach(ledText => {
        updateLEDButtonStatus(ledText, false);
    });

    console.log('LED controls initialized with states:', ledStates);
}

function updateLEDStatus(ledId, isOn) {
    const statusElement = document.getElementById(`${ledId}-status`);
    const ledIcon = statusElement.parentElement.parentElement.querySelector('.led-icon');

    if (isOn) {
        statusElement.textContent = 'ON';
        statusElement.classList.add('on');
        statusElement.classList.remove('off');
        ledIcon.style.opacity = '1';
    } else {
        statusElement.textContent = 'OFF';
        statusElement.classList.add('off');
        statusElement.classList.remove('on');
        ledIcon.style.opacity = '0.5';
    }
}

// Handle WebSocket errors and reconnection
socket.onclose = function () {
    console.log('WebSocket connection closed');
    setTimeout(function () {
        console.log('Attempting to reconnect...');
        socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, function (frame) {
            console.log('Reconnected to WebSocket');
            stompClient.subscribe('/topic/sensor-data', function (message) {
                const data = JSON.parse(message.body);
                updateDashboard(data);
            });
        });
    }, 2000);
};

