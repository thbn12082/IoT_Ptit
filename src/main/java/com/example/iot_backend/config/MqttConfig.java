package com.example.iot_backend.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {
/// ///////////////////////////////////////////////////
/// phần thứ 1: inject các thông tin kết nối tới broker đã được khai báo từ file application.properties
    @Value("${mqtt.broker.url}")
    private String brokerUrl; // dịa chỉ mqtt broker: tcp://(ip máy tính hiện tại):1883

    @Value("${mqtt.broker.client-id}")
    private String clientId;

    @Value("${mqtt.broker.username}")
    private String username; // thebinh

    @Value("${mqtt.broker.password}")
    private String password; // 0281

//    @Value("${mqtt.topics.sensor-data}")
//    private String sensorDataTopic;
/// ///////////////////////////////////////////////////////

/// ///////////////////////////////////////////////////////////
// Phần thứ 2: tạo Mqtt client factory cung cấp các tùy chọn kết nối như:
//    setCleanSession(true): Bắt đầu session mới mỗi lần kết nối
//
//    setConnectionTimeout(30): Timeout 30 giây khi kết nối
//
//    setKeepAliveInterval(60): Gửi ping mỗi 60 giây để duy trì kết nối
//
//    setAutomaticReconnect(true): Tự động kết nối lại khi mất kết nối

@Bean
public MqttPahoClientFactory mqttClientFactory() {
    DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
    MqttConnectOptions options = new MqttConnectOptions();

    options.setServerURIs(new String[] { brokerUrl });
    options.setUserName(username);
    options.setPassword(password.toCharArray());
    options.setCleanSession(true);
    options.setConnectionTimeout(30);
    options.setKeepAliveInterval(60);
    options.setAutomaticReconnect(true);

    factory.setConnectionOptions(options);
    return factory;
}



/// //////////////////////////////////////////////////////////////////
/// // Phần thứ 3: Inbound Message Producer (Nhận Tin Nhắn)
///Chức năng: Subscribe (đăng ký) nhận tin nhắn từ các MQTT topics
///
/// Topics được Subscribe:
/// home/sensors: Nhận dữ liệu từ tất cả sensors (temperature, humidity, light)
///
/// home/devices/+/led/+/state: Nhận feedback trạng thái LED từ devices (+ là wildcard)
///
/// home/lamps/1, home/lamps/2, home/lamps/3: Nhận commands để điều khiển từng LED riêng biệt
///
/// Cấu hình:
/// QoS = 1: "At least once delivery" - đảm bảo tin nhắn được gửi ít nhất 1 lần
///
/// CompletionTimeout = 5000ms: Timeout cho việc xử lý message
///
    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                clientId + "_inbound",
                mqttClientFactory(),
//                nhận dữ liệu từ đèn và cảm biến
                "home/sensors",
                "home/devices/+/led/+/state",
//                nhận lệnh bật tắt đèn
                "home/lamps/1",
                "home/lamps/2",
                "home/lamps/3"
        );
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

// kênh tin nhắn cho phần thứ 3:
//Chức năng: Tạo channels để routing messages trong Spring Integration
//
//    mqttInputChannel: Channel nhận tin nhắn từ MQTT broker
//
//    mqttOutputChannel: Channel gửi tin nhắn lên MQTT broker
//
//    DirectChannel: Synchronous point-to-point channel

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }


    @Bean
    public MessageChannel mqttOutputChannel() {
        return new DirectChannel();
    }

/// /////////////////////////////////////////
/// gửi tin nhắn:
/// Chức năng: Xử lý việc publish (gửi) tin nhắn lên MQTT broker
///
/// @ServiceActivator: Kết nối với mqttOutputChannel
///
/// setAsync(true): Gửi tin nhắn bất đồng bộ
///
/// setDefaultTopic("home/lamps"): Topic mặc định để gửi lệnh điều khiển LED
    @Bean
    @ServiceActivator(inputChannel = "mqttOutputChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(
                clientId + "_outbound",
                mqttClientFactory());

        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("home/lamps");
        messageHandler.setDefaultQos(1);

        return messageHandler;
    }

}
// tổng kết:
// 1. Communication Bridge
//Class này đóng vai trò cầu nối giữa Spring Boot backend và các IoT devices (ESP32, Arduino) thông qua MQTT protocol.
//
//2. Data Flow Handling
//Inbound: Nhận dữ liệu sensor từ devices → Spring services → Database
//
//Outbound: Gửi lệnh điều khiển từ Spring services → Devices
//
//3. Topic Organization
//Tổ chức topics theo pattern rõ ràng:


//Luồng Hoạt Động Trong Hệ Thống
//Sensor Data Collection:
//IoT Device → Publish to "home/sensors" →
//inbound() → mqttInputChannel →
//MqttService.handleMessage() → SensorDataService → Database
//LED Control:
//Dashboard → REST API → LedEventService →
//mqttOutputChannel → mqttOutbound() →
//Publish to "home/lamps/X" → IoT Device
//Configuration này cho phép Spring Boot application có thể nhận dữ liệu sensor real-time và điều khiển các thiết bị từ xa một cách đáng tin cậy.

