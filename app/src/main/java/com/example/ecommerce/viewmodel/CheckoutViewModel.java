package com.example.ecommerce.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecommerce.model.Address;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.repository.AddressRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.RepositoryCallback;
import com.example.ecommerce.util.CheckoutSession;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;

import java.util.List;

/**
 * Xử lý tiến trình đặt hàng và thanh toán (COD).
 */
public class CheckoutViewModel extends ViewModel {

    private final AddressRepository addressRepository = new AddressRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    private final CartRepository cartRepository = new CartRepository();

    private final MutableLiveData<Resource<Order>> orderResult = new MutableLiveData<>();

    public LiveData<Resource<Order>> getOrderResult() { return orderResult; }

    public void clearOrderResult() { orderResult.setValue(null); }

    /** Chọn địa chỉ nhận hàng ưu tiên cho phiên thanh toán. */
    public void selectDefaultAddress(final Runnable finishCallback) {
        if (CheckoutSession.getSelectedAddress() != null) {
            finishCallback.run();
            return;
        }
        addressRepository.getAddresses(new RepositoryCallback<List<Address>>() {
            @Override
            public void onSuccess(List<Address> data) {
                if (!data.isEmpty()) {
                    CheckoutSession.setSelectedAddress(data.get(0));
                }
                finishCallback.run();
            }

            @Override
            public void onError(String message) {
                finishCallback.run();
            }
        });
    }

    /** Tạo đơn hàng thanh toán sau khi nhận hàng. */
    public void placeOrder(Address address, String notes) {
        Order.Request request = new Order.Request();
        request.shippingAddress = address.toShippingText();
        request.paymentMethod = Constants.PAYMENT_COD;
        request.notes = notes;
        for (CheckoutSession.Item item : CheckoutSession.getItems()) {
            request.orderItems.add(new Order.Line(item.variantId, item.quantity));
        }

        orderResult.setValue(Resource.<Order>loading());
        orderRepository.createOrder(request, new RepositoryCallback<Order>() {
            @Override
            public void onSuccess(Order data) {
                removePurchasedItemsFromCart();
                orderResult.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                orderResult.setValue(Resource.<Order>error(message));
            }
        });
    }

    /** Xóa các sản phẩm đã đặt mua thành công khỏi giỏ hàng. */
    private void removePurchasedItemsFromCart() {
        for (CheckoutSession.Item sessionItem : CheckoutSession.getItems()) {
            if (sessionItem.cartItemId != null) {
                cartRepository.deleteItem(sessionItem.cartItemId, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                    }

                    @Override
                    public void onError(String message) {
                    }
                });
            }
        }
    }
}
