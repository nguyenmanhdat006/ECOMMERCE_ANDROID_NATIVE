package com.example.ecommerce.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.RepositoryCallback;
import com.example.ecommerce.util.Resource;

import java.util.List;

/**
 * Đọc đơn hàng: danh sách đơn, chi tiết một đơn, các sản phẩm chờ đánh giá.
 * Mỗi màn hình dùng một phần (mỗi màn có ViewModel riêng nên không lẫn dữ liệu).
 */
public class OrderViewModel extends ViewModel {

    private final OrderRepository repository = new OrderRepository();

    private final MutableLiveData<Resource<List<Order>>> orders = new MutableLiveData<>();
    private final MutableLiveData<Resource<Order>> order = new MutableLiveData<>();
    private final MutableLiveData<Resource<List<Order.OrderItem>>> unreviewed = new MutableLiveData<>();

    public LiveData<Resource<List<Order>>> getOrders() { return orders; }

    public LiveData<Resource<Order>> getOrder() { return order; }

    public LiveData<Resource<List<Order.OrderItem>>> getUnreviewed() { return unreviewed; }

    public void loadOrders() {
        orders.setValue(Resource.<List<Order>>loading());
        repository.getOrders(new RepositoryCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> data) {
                orders.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                orders.setValue(Resource.<List<Order>>error(message));
            }
        });
    }

    public void loadOrder(String orderId) {
        order.setValue(Resource.<Order>loading());
        repository.getOrder(orderId, new RepositoryCallback<Order>() {
            @Override
            public void onSuccess(Order data) {
                order.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                order.setValue(Resource.<Order>error(message));
            }
        });
    }

    public void loadUnreviewed() {
        unreviewed.setValue(Resource.<List<Order.OrderItem>>loading());
        repository.getUnreviewedItems(new RepositoryCallback<List<Order.OrderItem>>() {
            @Override
            public void onSuccess(List<Order.OrderItem> data) {
                unreviewed.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                unreviewed.setValue(Resource.<List<Order.OrderItem>>error(message));
            }
        });
    }
}
