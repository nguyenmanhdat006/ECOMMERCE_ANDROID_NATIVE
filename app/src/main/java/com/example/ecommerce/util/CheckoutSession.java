package com.example.ecommerce.util;

import com.example.ecommerce.model.Address;

import java.util.ArrayList;
import java.util.List;

/**
 * Dữ liệu tạm giữa các màn hình trong luồng mua hàng (chỉ sống trong bộ nhớ):
 *   - danh sách sản phẩm sắp mua (từ giỏ hàng hoặc từ nút "Mua ngay")
 *   - địa chỉ giao hàng đang chọn
 * Màn hình Giỏ hàng / Chi tiết sản phẩm điền vào, màn hình Thanh toán đọc ra.
 */
public final class CheckoutSession {

    /** Một dòng hàng sắp mua. */
    public static class Item {
        public String variantId;
        public String productName;
        public String imageUrl;
        public String variantText;   // ví dụ "M, Trắng"
        public double price;
        public int quantity;
        /** Id dòng trong giỏ hàng; null nếu mua ngay (không đi qua giỏ). */
        public String cartItemId;
    }

    private static final List<Item> ITEMS = new ArrayList<>();
    private static Address selectedAddress;

    private CheckoutSession() {
    }

    public static void setItems(List<Item> newItems) {
        ITEMS.clear();
        ITEMS.addAll(newItems);
    }

    public static List<Item> getItems() {
        return ITEMS;
    }

    public static double getTotal() {
        double total = 0;
        for (Item item : ITEMS) {
            total += item.price * item.quantity;
        }
        return total;
    }

    public static Address getSelectedAddress() {
        return selectedAddress;
    }

    public static void setSelectedAddress(Address address) {
        selectedAddress = address;
    }

    /** Xóa sạch sau khi đặt hàng xong hoặc khi đăng xuất. */
    public static void clear() {
        ITEMS.clear();
        selectedAddress = null;
    }
}
