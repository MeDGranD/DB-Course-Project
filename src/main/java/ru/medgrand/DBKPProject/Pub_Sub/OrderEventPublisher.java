package ru.medgrand.DBKPProject.Pub_Sub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.medgrand.DBKPProject.Models.Order;
import ru.medgrand.DBKPProject.RedisConfig;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class OrderEventPublisher {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void publishNewOrder(Order order) {
        try {
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("orderId", order.getOrder_id());
            messagePayload.put("userId", order.getUser().getUser_id());
            messagePayload.put("totalPrice", order.getTotal_price());
            messagePayload.put("status", order.getHistory().isEmpty() ? "unknown" : order.getHistory().getLast().getStatus());
            messagePayload.put("timestamp", LocalDateTime.now().toString());

            String message = objectMapper.writeValueAsString(messagePayload);
            stringRedisTemplate.convertAndSend(RedisConfig.NEW_ORDER_TOPIC, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void publishOrderStatusUpdate(Order order, String oldStatus, String newStatus) {
        try {
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("orderId", order.getOrder_id());
            messagePayload.put("newStatus", newStatus);
            messagePayload.put("oldStatus", oldStatus);
            messagePayload.put("userId", order.getUser().getUser_id());
            messagePayload.put("timestamp", LocalDateTime.now().toString());

            String message = objectMapper.writeValueAsString(messagePayload);
            stringRedisTemplate.convertAndSend(RedisConfig.ORDER_STATUS_UPDATE_TOPIC, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
