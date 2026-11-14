package com.example.ecommerce.util;

import com.example.ecommerce.model.Address;

import java.util.ArrayList;
import java.util.List;

/**
 * Lưu trữ phiên làm việc tạm thời phục vụ luồng mua hàng và thanh toán.
 */
public final class CheckoutSession {

    /** Đại diện mục sản phẩm chuẩn bị tiến hành thanh toán. */
    public static class Item {
        public String variantId;
        public String productName;
        public String imageUrl;
        public String variantText;
        public double price;
        public int quantity;
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

    /** Tính tổng chi phí phiên thanh toán. */
    public static double getTotal() {
        double sumTotal = 0;
        for (Item sessionLine : ITEMS) {
            sumTotal += sessionLine.price * sessionLine.quantity;
        }
        return sumTotal;
    }

    public static Address getSelectedAddress() {
        return selectedAddress;
    }

    public static void setSelectedAddress(Address address) {
        selectedAddress = address;
    }

    /** Reset trạng thái phiên thanh toán về ban đầu. */
    public static void clear() {
        ITEMS.clear();
        selectedAddress = null;
    }
}
