package com.example.ecommerce.repository;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.service.ApiService;
import com.example.ecommerce.service.RetrofitClient;
import com.example.ecommerce.util.Constants;

import java.util.List;

/** Đơn hàng của người đang đăng nhập (chỉ thanh toán khi nhận hàng COD). */
public class OrderRepository extends BaseRepository {

    private final ApiService api = Constants.USE_MOCK_DATA ? null : RetrofitClient.getApiService();

    public void createOrder(Order.Request request, RepositoryCallback<Order> callback) {
        if (Constants.USE_MOCK_DATA) {
            Order order = MockStore.createOrder(request);
            if (order == null) {
                MockDataSource.error(callback, "Một số sản phẩm không đủ hàng.");
            } else {
                MockDataSource.success(callback, order);
            }
            return;
        }
        enqueue(api.createOrder(request), callback);
    }

    public void getOrders(RepositoryCallback<List<Order>> callback) {
        if (Constants.USE_MOCK_DATA) {
            MockDataSource.success(callback, MockStore.getOrders());
            return;
        }
        enqueue(api.getMyOrders(), callback);
    }

    public void getOrder(String orderId, RepositoryCallback<Order> callback) {
        if (Constants.USE_MOCK_DATA) {
            Order order = MockStore.getOrder(orderId);
            if (order == null) {
                MockDataSource.error(callback, "Không tìm thấy dữ liệu.");
            } else {
                MockDataSource.success(callback, order);
            }
            return;
        }
        enqueue(api.getOrder(orderId), callback);
    }

    /** Các sản phẩm đã mua (đơn hoàn thành) mà chưa đánh giá. */
    public void getUnreviewedItems(RepositoryCallback<List<Order.OrderItem>> callback) {
        if (Constants.USE_MOCK_DATA) {
            MockDataSource.success(callback, MockStore.getUnreviewedItems());
            return;
        }
        enqueue(api.getUnreviewedItems(), callback);
    }
}
