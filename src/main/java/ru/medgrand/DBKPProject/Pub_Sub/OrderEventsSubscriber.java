package ru.medgrand.DBKPProject.Pub_Sub;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component("orderEventsSubscriber")
public class OrderEventsSubscriber {

    @Autowired
    private ObjectMapper objectMapper;

    private final Set<OrderUpdateListener> listeners = new HashSet<>();

    public void registerListener(OrderUpdateListener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(OrderUpdateListener listener) {
        listeners.remove(listener);
    }

    public void handleNewOrderMessage(String messageJson) {
        try {
            Map<String, Object> orderData = objectMapper.readValue(messageJson, new TypeReference<>() {});
            listeners.forEach(listener -> listener.onNewOrderReceived(orderData));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handleOrderStatusUpdateMessage(String messageJson) {
        try {
            Map<String, Object> statusUpdateData = objectMapper.readValue(messageJson, new TypeReference<>() {});
            listeners.forEach(listener -> listener.onOrderStatusUpdated(statusUpdateData));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
