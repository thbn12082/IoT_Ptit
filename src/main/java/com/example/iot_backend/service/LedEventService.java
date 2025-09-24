package com.example.iot_backend.service;

import com.example.iot_backend.model.LedEvent;
import com.example.iot_backend.repository.LedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LedEventService {

    private final LedEventRepository repository;

    public LedEventService(LedEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public LedEvent save(int ledNumber, boolean stateOn) {
        LedEvent event = new LedEvent();
        event.setLedNumber(ledNumber);
        event.setStateOn(stateOn);
        event.setCreatedAt(LocalDateTime.now());
        return repository.save(event);
    }
//    Ghi lại mỗi lần LED được điều khiển

    public List<LedEvent> getRecentEvents() {
//dùng cái này nếu chỉ muốn lấy 50 cái gần nhất
//        return repository.findTop50ByOrderByCreatedAtDesc();

        // dùng cái này khi muốn lấy tất cả lịch sử hoạt ộng của đèn và thứ tự mơ nhất đến cũ nhất
        return repository.findAllByOrderByCreatedAtDesc();
    }
//lấy lịch sử hoạt động của tất cả led gần đây đén lâu rồi
    public List<LedEvent> getEventsByLed(int ledNumber) {
        return repository.findByLedNumberOrderByCreatedAtDesc(ledNumber);
    }
// lấy lịch sử của led được chỉ định
}
