package com.example.ecommerce.repository;

import com.example.ecommerce.model.Address;
import com.example.ecommerce.model.AuthModels;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductVariant;
import com.example.ecommerce.model.Review;
import com.example.ecommerce.model.User;
import com.example.ecommerce.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * "Backend giả" nằm trong bộ nhớ: tài khoản, giỏ hàng, địa chỉ, đơn hàng, đánh giá.
 * Khi USE_MOCK_DATA = true, các repository gọi vào đây thay vì gọi mạng, nên mọi luồng
 * (đăng ký, thêm giỏ, đặt hàng, đánh giá...) đều dùng thử được. Dữ liệu mất khi tắt app.
 *
 * Tài khoản có sẵn: lan@example.com / Test@12345
 * Mã xác thực email khi đăng ký tài khoản mới: 123456
 */
final class MockStore {

    static final String DEMO_EMAIL = "lan@example.com";
    static final String DEMO_PASSWORD = "Test@12345";
    static final String VERIFY_CODE = "123456";

    /** Một tài khoản giả: thông tin đăng nhập kèm đã kích hoạt chưa. */
    private static class MockUser {
        String password;
        boolean enabled;
        User user;
    }

    private static final List<MockUser> USERS = new ArrayList<>();
    private static final List<CartItem> CART = new ArrayList<>();
    private static final List<Address> ADDRESSES = new ArrayList<>();
    private static final List<Order> ORDERS = new ArrayList<>();
    /** Đánh giá của từng sản phẩm: khóa là id sản phẩm. */
    private static final Map<String, List<Review>> REVIEWS = new HashMap<>();

    private static boolean seeded = false;
    private static int idCounter = 1000;

    private MockStore() {
    }

    // ---------------------------------------------------------------- Tài khoản

    /** Trả về thông tin đăng nhập, hoặc null nếu sai email, sai mật khẩu hoặc chưa kích hoạt. */
    static AuthModels.LoginResponse login(String email, String password) {
        seedIfNeeded();
        for (MockUser u : USERS) {
            if (u.user.email.equalsIgnoreCase(email) && u.password.equals(password) && u.enabled) {
                AuthModels.LoginResponse response = new AuthModels.LoginResponse();
                response.token = "mock-token-" + u.user.id;
                response.user = u.user;
                return response;
            }
        }
        return null;
    }

    /** Trả về câu báo lỗi, hoặc null nếu đăng ký thành công. */
    static String register(AuthModels.RegisterRequest request) {
        seedIfNeeded();
        for (MockUser u : USERS) {
            if (u.user.email.equalsIgnoreCase(request.email)) {
                return "Email already exist!";
            }
        }
        MockUser mock = new MockUser();
        mock.password = request.password;
        mock.enabled = false; // phải nhập mã xác thực mới kích hoạt
        mock.user = newUser(request.email, request.firstName, request.lastName, request.phoneNumber);
        USERS.add(mock);
        return null;
    }

    /** Đúng mã thì kích hoạt tài khoản và trả về true. */
    static boolean verify(String email, String code) {
        seedIfNeeded();
        for (MockUser u : USERS) {
            if (u.user.email.equalsIgnoreCase(email) && VERIFY_CODE.equals(code)) {
                u.enabled = true;
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------- Giỏ hàng

    static List<CartItem> getCart() {
        seedIfNeeded();
        return new ArrayList<>(CART);
    }

    /** Thêm vào giỏ (cộng dồn nếu đã có). Trả về câu báo lỗi, hoặc null nếu thành công. */
    static String addToCart(String variantId, int quantity) {
        seedIfNeeded();
        Product product = MockDataSource.findProductByVariantId(variantId);
        ProductVariant variant = MockDataSource.findVariant(variantId);
        if (product == null || variant == null) {
            return "Không tìm thấy dữ liệu.";
        }
        for (CartItem item : CART) {
            if (item.productVariant.getId().equals(variantId)) {
                return setQuantity(item, item.quantity + quantity);
            }
        }
        if (quantity > variant.getStockQuantity()) {
            return "Số lượng vượt quá tồn kho (hiện còn " + variant.getStockQuantity() + ")";
        }
        CartItem item = new CartItem();
        item.id = nextId("cart");
        item.product = product;
        item.productVariant = variant;
        item.quantity = quantity;
        item.userId = currentUserId();
        CART.add(item);
        return null;
    }

    /** Đặt lại số lượng một dòng giỏ hàng. Trả về câu báo lỗi, hoặc null nếu thành công. */
    static String updateCartItem(String cartItemId, int quantity) {
        seedIfNeeded();
        for (CartItem item : CART) {
            if (item.id.equals(cartItemId)) {
                return setQuantity(item, quantity);
            }
        }
        return "Không tìm thấy dữ liệu.";
    }

    static void deleteCartItem(String cartItemId) {
        seedIfNeeded();
        for (int i = 0; i < CART.size(); i++) {
            if (CART.get(i).id.equals(cartItemId)) {
                CART.remove(i);
                return;
            }
        }
    }

    private static String setQuantity(CartItem item, int quantity) {
        int stock = item.productVariant.getStockQuantity();
        if (quantity > stock) {
            return "Số lượng vượt quá tồn kho (hiện còn " + stock + ")";
        }
        item.quantity = quantity;
        return null;
    }

    // ---------------------------------------------------------------- Địa chỉ

    static List<Address> getAddresses() {
        seedIfNeeded();
        return new ArrayList<>(ADDRESSES);
    }

    /** Thêm mới (id null) hoặc cập nhật (có id). Trả về địa chỉ đã lưu. */
    static Address saveAddress(Address address) {
        seedIfNeeded();
        if (address.id == null) {
            address.id = nextId("addr");
            ADDRESSES.add(address);
            return address;
        }
        for (int i = 0; i < ADDRESSES.size(); i++) {
            if (ADDRESSES.get(i).id.equals(address.id)) {
                ADDRESSES.set(i, address);
            }
        }
        return address;
    }

    static void deleteAddress(String addressId) {
        seedIfNeeded();
        for (int i = 0; i < ADDRESSES.size(); i++) {
            if (ADDRESSES.get(i).id.equals(addressId)) {
                ADDRESSES.remove(i);
                return;
            }
        }
    }

    // ---------------------------------------------------------------- Đơn hàng

    /** Danh sách đơn của tôi, đơn mới nhất ở đầu. */
    static List<Order> getOrders() {
        seedIfNeeded();
        return new ArrayList<>(ORDERS);
    }

    static Order getOrder(String orderId) {
        seedIfNeeded();
        for (Order order : ORDERS) {
            if (order.id.equals(orderId)) {
                return order;
            }
        }
        return null;
    }

    /** Các dòng đã mua (đơn hoàn thành PAID) nhưng chưa đánh giá. */
    static List<Order.OrderItem> getUnreviewedItems() {
        seedIfNeeded();
        List<Order.OrderItem> result = new ArrayList<>();
        for (Order order : ORDERS) {
            if (!"PAID".equals(order.status)) {
                continue;
            }
            for (Order.OrderItem item : order.orderItems) {
                if (!item.reviewed) {
                    result.add(item);
                }
            }
        }
        return result;
    }

    /** Tạo đơn mới (trạng thái PENDING). Trả về null nếu có dòng không đủ hàng. */
    static Order createOrder(Order.Request request) {
        seedIfNeeded();
        Order order = new Order();
        order.id = nextId("order");
        order.orderNumber = "SE-" + new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date())
                + "-" + idCounter;
        order.orderDate = nowIso();
        order.status = "PENDING";
        order.paymentStatus = "UNPAID";
        order.paymentMethod = request.paymentMethod;
        order.shippingAddress = request.shippingAddress;
        order.notes = request.notes;
        order.customerName = currentUserName();

        double total = 0;
        for (Order.Line line : request.orderItems) {
            Product product = MockDataSource.findProductByVariantId(line.productVariantId);
            ProductVariant variant = MockDataSource.findVariant(line.productVariantId);
            if (product == null || variant == null || line.quantity > variant.getStockQuantity()) {
                return null;
            }
            Order.OrderItem item = newOrderItem(product, variant, line.quantity, false);
            order.orderItems.add(item);
            total += item.totalPrice;
        }
        order.totalAmount = total;
        ORDERS.add(0, order);
        return order;
    }

    // ---------------------------------------------------------------- Đánh giá

    static List<Review> getReviews(String productId) {
        seedIfNeeded();
        List<Review> list = REVIEWS.get(productId);
        return list == null ? new ArrayList<Review>() : new ArrayList<>(list);
    }

    /** Viết đánh giá mới. Trả về câu báo lỗi qua errorHolder[0]; thành công thì trả về Review. */
    static Review createReview(Review.Request request, String[] errorHolder) {
        seedIfNeeded();
        Order.OrderItem item = findOrderItem(request.orderItemId);
        Order order = findOrderOfItem(request.orderItemId);
        if (item == null || order == null) {
            errorHolder[0] = "Không tìm thấy dòng đơn hàng.";
            return null;
        }
        if (!"PAID".equals(order.status)) {
            errorHolder[0] = "Chỉ đánh giá được đơn đã hoàn thành.";
            return null;
        }
        if (item.reviewed) {
            errorHolder[0] = "Sản phẩm này đã được đánh giá.";
            return null;
        }
        item.reviewed = true;

        User me = SessionManager.getUser();
        Review review = new Review();
        review.id = nextId("review");
        review.rating = request.rating;
        review.comment = request.comment;
        review.createdAt = nowIso();
        review.updatedAt = review.createdAt;
        review.orderItemId = request.orderItemId;
        review.user = new Review.Reviewer();
        review.user.id = me.id;
        review.user.firstName = me.firstName;
        review.user.lastName = me.lastName;
        addReview(request.productId, review);
        return review;
    }

    /** Sửa số sao và nhận xét. Trả về null nếu không tìm thấy. */
    static Review updateReview(String reviewId, Review.Request request) {
        seedIfNeeded();
        for (List<Review> list : REVIEWS.values()) {
            for (Review review : list) {
                if (review.id.equals(reviewId)) {
                    review.rating = request.rating;
                    review.comment = request.comment;
                    review.updatedAt = nowIso();
                    return review;
                }
            }
        }
        return null;
    }

    static void deleteReview(String reviewId) {
        seedIfNeeded();
        for (List<Review> list : REVIEWS.values()) {
            for (int i = 0; i < list.size(); i++) {
                Review review = list.get(i);
                if (review.id.equals(reviewId)) {
                    Order.OrderItem item = findOrderItem(review.orderItemId);
                    if (item != null) {
                        item.reviewed = false; // cho phép đánh giá lại
                    }
                    list.remove(i);
                    return;
                }
            }
        }
    }

    // ---------------------------------------------------------------- Hàm phụ

    private static Order.OrderItem findOrderItem(String orderItemId) {
        for (Order order : ORDERS) {
            for (Order.OrderItem item : order.orderItems) {
                if (item.id.equals(orderItemId)) {
                    return item;
                }
            }
        }
        return null;
    }

    private static Order findOrderOfItem(String orderItemId) {
        for (Order order : ORDERS) {
            for (Order.OrderItem item : order.orderItems) {
                if (item.id.equals(orderItemId)) {
                    return order;
                }
            }
        }
        return null;
    }

    private static void addReview(String productId, Review review) {
        List<Review> list = REVIEWS.get(productId);
        if (list == null) {
            list = new ArrayList<>();
            REVIEWS.put(productId, list);
        }
        list.add(0, review);
    }

    private static Order.OrderItem newOrderItem(Product product, ProductVariant variant, int quantity, boolean reviewed) {
        Order.OrderItem item = new Order.OrderItem();
        item.id = nextId("item");
        item.quantity = quantity;
        item.unitPrice = product.getPrice();
        item.totalPrice = product.getPrice() * quantity;
        item.productId = product.getId();
        item.productVariantId = variant.getId();
        item.productName = product.getName();
        item.thumbnail = product.getCoverUrl();
        item.reviewed = reviewed;
        return item;
    }

    private static User newUser(String email, String firstName, String lastName, String phone) {
        User user = new User();
        user.id = nextId("user");
        user.email = email;
        user.firstName = firstName;
        user.lastName = lastName;
        user.phoneNumber = phone;
        user.role = "USER";
        return user;
    }

    private static String currentUserId() {
        User me = SessionManager.getUser();
        return me == null ? null : me.id;
    }

    private static String currentUserName() {
        User me = SessionManager.getUser();
        return me == null ? "" : me.getFullName();
    }

    private static String nextId(String prefix) {
        idCounter++;
        return "mock-" + prefix + "-" + idCounter;
    }

    private static String nowIso() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).format(new Date());
    }

    // ---------------------------------------------------------------- Dữ liệu ban đầu

    /** Tạo dữ liệu mẫu ở lần dùng đầu tiên: 1 tài khoản, 2 địa chỉ, 5 đơn hàng, vài đánh giá. */
    private static void seedIfNeeded() {
        if (seeded) {
            return;
        }
        seeded = true;

        MockUser demo = new MockUser();
        demo.password = DEMO_PASSWORD;
        demo.enabled = true;
        demo.user = newUser(DEMO_EMAIL, "Lan", "Nguyen", "0911222333");
        demo.user.id = "mock-user-demo"; // id cố định để đánh giá mẫu thuộc về tài khoản này
        USERS.add(demo);

        ADDRESSES.add(newAddress("Nhà riêng", "12 Nguyễn Trãi", "Quận 1", "Hồ Chí Minh", "700000", "0911222333"));
        ADDRESSES.add(newAddress("Công ty", "45 Lê Lợi", "Quận 3", "Hồ Chí Minh", "700000", "0988000111"));

        List<Product> products = MockDataSource.allProducts();
        Product shirt = products.get(0);   // áo thun
        Product jeans = products.get(2);   // quần jean
        Product oxford = products.get(4);  // sơ mi oxford
        Product jacket = products.get(6);  // áo khoác

        // Đơn mới nhất ở đầu danh sách
        ORDERS.add(newSeedOrder("001", "PENDING", "2026-10-13", shirt, 2, false));
        ORDERS.add(newSeedOrder("002", "SHIPPING", "2026-10-10", jacket, 1, false));
        Order paid = newSeedOrder("003", "PAID", "2026-10-02", oxford, 1, false); // chưa đánh giá
        paid.orderItems.add(newOrderItem(jeans, jeans.getVariants().get(0), 1, false));
        paid.totalAmount = oxford.getPrice() + jeans.getPrice();
        ORDERS.add(paid);
        ORDERS.add(newSeedOrder("004", "CANCELED", "2026-09-20", jeans, 1, false));
        ORDERS.add(newSeedOrder("005", "PAID", "2026-09-01", shirt, 1, true)); // đã đánh giá

        // Đánh giá đầu tiên là của tài khoản demo, để thử chức năng sửa và xóa đánh giá
        Review ownReview = seedReview("mock-review-1", 5, "Vải mát, giao nhanh.", "Nguyen", "Lan", "2026-09-05");
        ownReview.user.id = demo.user.id;
        addReview(shirt.getId(), ownReview);
        addReview(shirt.getId(), seedReview("mock-review-2", 4, "Đẹp nhưng hơi rộng, nên lấy nhỏ hơn một size.", "Tran", "Minh", "2026-09-12"));
        addReview(shirt.getId(), seedReview("mock-review-3", 5, "", "Le", "Hoa", "2026-09-18"));
        addReview(oxford.getId(), seedReview("mock-review-4", 5, "Sơ mi dày dặn, mặc đi làm rất ổn.", "Pham", "Nam", "2026-09-22"));
        addReview(oxford.getId(), seedReview("mock-review-5", 4, "Đường may đẹp.", "Vo", "Thu", "2026-10-01"));
    }

    private static Address newAddress(String name, String street, String state, String city, String zip, String phone) {
        Address a = new Address();
        a.id = nextId("addr");
        a.name = name;
        a.street = street;
        a.state = state;
        a.city = city;
        a.zipCode = zip;
        a.phoneNumber = phone;
        return a;
    }

    private static Order newSeedOrder(String number, String status, String day, Product product, int quantity, boolean reviewed) {
        Order order = new Order();
        order.id = nextId("order");
        order.orderNumber = "SE-" + day.replace("-", "") + "-" + number;
        order.orderDate = day + "T09:30:00.000+00:00";
        order.status = status;
        order.paymentStatus = "PAID".equals(status) ? "PAID" : "UNPAID";
        order.paymentMethod = "COD";
        order.shippingAddress = "Nhà riêng - 0911222333 - 12 Nguyễn Trãi, Quận 1, Hồ Chí Minh";
        order.customerName = "Nguyen Lan";
        Order.OrderItem item = newOrderItem(product, product.getVariants().get(0), quantity, reviewed);
        order.orderItems.add(item);
        order.totalAmount = item.totalPrice;
        return order;
    }

    private static Review seedReview(String id, int rating, String comment, String lastName, String firstName, String day) {
        Review review = new Review();
        review.id = id;
        review.rating = rating;
        review.comment = comment;
        review.createdAt = day + "T10:00:00.000+00:00";
        review.updatedAt = review.createdAt;
        review.user = new Review.Reviewer();
        review.user.id = "mock-other-" + id;
        review.user.firstName = firstName;
        review.user.lastName = lastName;
        return review;
    }
}
