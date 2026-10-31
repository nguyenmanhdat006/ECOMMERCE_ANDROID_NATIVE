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
 * Màn Thanh toán: chọn địa chỉ mặc định và đặt hàng (COD).
 * Đặt xong thì xóa các dòng đã mua khỏi giỏ hàng (nếu mua từ giỏ).
 */
public class CheckoutViewModel extends ViewModel {

    private final AddressRepository addressRepository = new AddressRepository();
    private final OrderRepository orderRepository = new OrderRepository();
    private final CartRepository cartRepository = new CartRepository();

    private final MutableLiveData<Resource<Order>> orderResult = new MutableLiveData<>();

    public LiveData<Resource<Order>> getOrderResult() { return orderResult; }

    public void clearOrderResult() { orderResult.setValue(null); }

    /** Nếu chưa chọn địa chỉ nào thì lấy địa chỉ đầu tiên của người dùng làm mặc định. */
    public void selectDefaultAddress(final Runnable onDone) {
        if (CheckoutSession.getSelectedAddress() != null) {
            onDone.run();
            return;
        }
        addressRepository.getAddresses(new RepositoryCallback<List<Address>>() {
            @Override
            public void onSuccess(List<Address> data) {
                if (!data.isEmpty()) {
                    CheckoutSession.setSelectedAddress(data.get(0));
                }
                onDone.run();
            }

            @Override
            public void onError(String message) {
                onDone.run();
            }
        });
    }

    /** Gửi đơn hàng lên máy chủ: chỉ thanh toán khi nhận hàng. */
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

    /** Xóa khỏi giỏ hàng những dòng vừa đặt (bỏ qua kết quả, không ảnh hưởng đơn đã tạo). */
    private void removePurchasedItemsFromCart() {
        for (CheckoutSession.Item item : CheckoutSession.getItems()) {
            if (item.cartItemId != null) {
                cartRepository.deleteItem(item.cartItemId, new RepositoryCallback<Void>() {
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
