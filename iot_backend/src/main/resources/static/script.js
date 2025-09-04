// Initialize WebSocket connection
let stompClient = null;
const socket = new SockJS('/ws');
stompClient = Stomp.over(socket);

// Chart configuration
const tempHumConfig = {
    type: 'line',
    data: {
        labels: [],
        datasets: [
            {
                label: 'Temperature (°C)',
                borderColor: '#ff6b6b',
                backgroundColor: 'rgba(255, 107, 107, 0.1)',
                data: [],
                fill: true,
                tension: 0.4,
                borderWidth: 2,
                pointRadius: 4,
                pointBackgroundColor: '#ff6b6b'
            },
            {
                label: 'Humidity (%)',
                borderColor: '#4dabf7',
                backgroundColor: 'rgba(77, 171, 247, 0.1)',
                data: [],
                fill: true,
                tension: 0.4,
                borderWidth: 2,
                pointRadius: 4,
                pointBackgroundColor: '#4dabf7'
            }
        ]
    },
    options: {
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
                labels: {
                    font: {
                        size: 12,
                        weight: 'bold'
                    },
                    usePointStyle: true,
                    padding: 20
                }
            },
            tooltip: {
                backgroundColor: 'rgba(255,255,255,0.9)',
                titleColor: '#1a2035',
                bodyColor: '#666',
                borderColor: 'rgba(0,0,0,0.1)',
                borderWidth: 1,
                padding: 12,
                displayColors: true,
                callbacks: {
                    label: function (context) {
                        return ' ' + context.dataset.label + ': ' + context.parsed.y;
                    }
                }
            }
        },
        interaction: {
            intersect: false,
            mode: 'index'
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
                    text: 'Light Intensity (lux)',
                    font: {
                        size: 14,
                        weight: 'bold'
                    }
                }
            }
        },
        plugins: {
            legend: {
                labels: {
                    font: {
                        size: 12,
                        weight: 'bold'
                    },
                    usePointStyle: true,
                    padding: 20
                }
            },
            tooltip: {
                backgroundColor: 'rgba(255,255,255,0.9)',
                titleColor: '#1a2035',
                bodyColor: '#666',
                borderColor: 'rgba(0,0,0,0.1)',
                borderWidth: 1,
                padding: 12,
                displayColors: true
            }
        },
        interaction: {
            intersect: false,
            mode: 'index'
        }
    }
};

// Initialize charts
const tempHumChart = new Chart(
    document.getElementById('tempHumChart'),
    tempHumConfig
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

function updateDashboard(data) {
    // Update gauge values
    document.getElementById('temperature-value').textContent = data.temperature.toFixed(1);
    document.getElementById('humidity-value').textContent = data.humidity.toFixed(1);
    document.getElementById('light-value').textContent = data.light.toFixed(0);

    // Update charts
    const currentTime = new Date().toLocaleTimeString();

    // Update temperature and humidity chart
    if (tempHumConfig.data.labels.length > 30) {
        tempHumConfig.data.labels.shift();
        tempHumConfig.data.datasets[0].data.shift();
        tempHumConfig.data.datasets[1].data.shift();
    }
    tempHumConfig.data.labels.push(currentTime);
    tempHumConfig.data.datasets[0].data.push(data.temperature);
    tempHumConfig.data.datasets[1].data.push(data.humidity);
    tempHumChart.update();

    // Update light chart
    if (lightConfig.data.labels.length > 30) {
        lightConfig.data.labels.shift();
        lightConfig.data.datasets[0].data.shift();
    }
    lightConfig.data.labels.push(currentTime);
    lightConfig.data.datasets[0].data.push(data.light);
    lightChart.update();
}

function initializeLEDControls() {
    const leds = ['led1', 'led2', 'led3'];

    leds.forEach(led => {
        const checkbox = document.getElementById(led);
        checkbox.addEventListener('change', function () {
            const message = {
                deviceId: led,
                state: this.checked
            };

            stompClient.send("/app/led-control", {}, JSON.stringify(message));
        });
    });
}

// Handle WebSocket errors and reconnection
socket.onclose = function () {
    console.log('WebSocket connection closed');
    setTimeout(function () {
        console.log('Attempting to reconnect...');
        connect();
    }, 2000);
};
