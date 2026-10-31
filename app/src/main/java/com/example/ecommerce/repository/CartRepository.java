package com.example.ecommerce.repository;

import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.User;
import com.example.ecommerce.service.ApiService;
import com.example.ecommerce.service.RetrofitClient;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.SessionManager;

import java.util.List;

/** Giỏ hàng của người đang đăng nhập. */
public class CartRepository extends BaseRepository {

    private final ApiService api = Constants.USE_MOCK_DATA ? null : RetrofitClient.getApiService();

    public void getCart(RepositoryCallback<List<CartItem>> callback) {
        if (Constants.USE_MOCK_DATA) {
            MockDataSource.success(callback, MockStore.getCart());
            return;
        }
        User user = SessionManager.getUser();
        enqueue(api.getCart(user.id), callback);
    }

    /** Thêm vào giỏ; nếu biến thể đã có trong giỏ thì cộng dồn số lượng. */
    public void addToCart(String variantId, int quantity, RepositoryCallback<Void> callback) {
        if (Constants.USE_MOCK_DATA) {
            replyMock(MockStore.addToCart(variantId, quantity), callback);
            return;
        }
        enqueue(api.addToCart(new CartItem.Request(variantId, quantity)), callback);
    }

    /** Đặt lại số lượng của một dòng giỏ hàng. */
    public void updateQuantity(String cartItemId, String variantId, int quantity, RepositoryCallback<Void> callback) {
        if (Constants.USE_MOCK_DATA) {
            replyMock(MockStore.updateCartItem(cartItemId, quantity), callback);
            return;
        }
        enqueue(api.updateCartItem(cartItemId, new CartItem.Request(variantId, quantity)), callback);
    }

    public void deleteItem(String cartItemId, RepositoryCallback<Void> callback) {
        if (Constants.USE_MOCK_DATA) {
            MockStore.deleteCartItem(cartItemId);
            MockDataSource.success(callback, null);
            return;
        }
        enqueue(api.deleteCartItem(cartItemId), callback);
    }

    /** error = null nghĩa là thành công. */
    private void replyMock(String error, RepositoryCallback<Void> callback) {
        if (error == null) {
            MockDataSource.success(callback, null);
        } else {
            MockDataSource.error(callback, error);
        }
    }
}
