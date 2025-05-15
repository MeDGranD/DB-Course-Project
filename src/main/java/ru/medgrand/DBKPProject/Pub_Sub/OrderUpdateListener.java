package ru.medgrand.DBKPProject.Pub_Sub;

import java.util.Map;

public interface OrderUpdateListener {
    void onNewOrderReceived(Map<String, Object> orderData);
    void onOrderStatusUpdated(Map<String, Object> statusUpdateData);
}
