package com.example.ecommerce.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.RepositoryCallback;
import com.example.ecommerce.util.Resource;

import java.util.List;

/**
 * Quản lý trạng thái giỏ hàng cá nhân (hiển thị, thay đổi số lượng, xóa và thêm mới).
 */
public class CartViewModel extends ViewModel {

    private final CartRepository repository = new CartRepository();

    private final MutableLiveData<Resource<List<CartItem>>> cart = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<Resource<List<CartItem>>> getCart() { return cart; }

    public LiveData<String> getMessage() { return message; }

    public void clearMessage() { message.setValue(null); }

    /** Tải lại toàn bộ dữ liệu giỏ hàng. */
    public void load() {
        cart.setValue(Resource.<List<CartItem>>loading());
        fetchCart();
    }

    /** Cập nhật giỏ hàng không kèm hiệu ứng tải. */
    public void reloadQuietly() {
        fetchCart();
    }

    public void changeQuantity(CartItem item, int newQuantity) {
        repository.updateQuantity(item.id, item.productVariant.getId(), newQuantity, new ActionCallback(null));
    }

    public void remove(CartItem item) {
        repository.deleteItem(item.id, new ActionCallback(null));
    }

    /** Thêm sản phẩm vào giỏ kèm thông báo mặc định. */
    public void addToCart(String variantId, int quantity) {
        repository.addToCart(variantId, quantity, new ActionCallback("Đã thêm vào giỏ hàng"));
    }

    /** Thêm sản phẩm vào giỏ kèm xử lý phản hồi tùy chỉnh. */
    public void addToCart(String variantId, int quantity, RepositoryCallback<Void> callback) {
        repository.addToCart(variantId, quantity, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                reloadQuietly();
                callback.onSuccess(data);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    private void fetchCart() {
        repository.getCart(new RepositoryCallback<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> data) {
                cart.setValue(Resource.success(data));
            }

            @Override
            public void onError(String errorMessage) {
                cart.setValue(Resource.<List<CartItem>>error(errorMessage));
            }
        });
    }

    /** Xử lý phản hồi kết quả và cập nhật trạng thái thông điệp. */
    private class ActionCallback implements RepositoryCallback<Void> {
        private final String msgSuccess;

        ActionCallback(String msgSuccess) {
            this.msgSuccess = msgSuccess;
        }

        @Override
        public void onSuccess(Void data) {
            if (msgSuccess != null) {
                message.setValue(msgSuccess);
            }
            reloadQuietly();
        }

        @Override
        public void onError(String errorMessage) {
            message.setValue(errorMessage);
            reloadQuietly();
        }
    }
}
