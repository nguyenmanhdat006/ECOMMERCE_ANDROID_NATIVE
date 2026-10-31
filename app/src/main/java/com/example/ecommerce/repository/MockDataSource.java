package com.example.ecommerce.repository;

import android.os.Handler;
import android.os.Looper;

import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductFilter;
import com.example.ecommerce.model.ProductPage;
import com.example.ecommerce.model.ProductVariant;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * DỮ LIỆU GIẢ về sản phẩm và danh mục, dùng khi backend chưa chạy (USE_MOCK_DATA = true).
 * Dữ liệu giả về giỏ hàng, đơn hàng, địa chỉ, đánh giá, tài khoản nằm ở MockStore.
 *
 * Đổi kịch bản để xem các trạng thái giao diện: gán MockDataSource.scenario (xem enum Scenario).
 */
public final class MockDataSource {

    public enum Scenario {
        /** Có dữ liệu, phản hồi sau 0,5 giây. */
        NORMAL,
        /** Danh sách sản phẩm rỗng (xem trạng thái "không tìm thấy"). */
        EMPTY,
        /** Mọi yêu cầu đều báo lỗi (xem trạng thái lỗi + nút Thử lại). */
        ERROR,
        /** Có dữ liệu nhưng phản hồi sau 3 giây (xem trạng thái đang tải). */
        SLOW
    }

    /** Đổi giá trị này để xem các trạng thái khác nhau. */
    public static Scenario scenario = Scenario.NORMAL;

    private static final Gson GSON = new Gson();
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static final String IMAGE_URL = "https://placehold.co/600x800/png?text=";

    private static final List<Category> CATEGORIES = new ArrayList<>();
    private static final List<Product> PRODUCTS = new ArrayList<>();

    private MockDataSource() {
    }

    // ---------------------------------------------------------------- Giả lập phản hồi mạng

    /** Trả dữ liệu thành công sau một khoảng trễ (giống mạng thật). */
    static <T> void success(RepositoryCallback<T> callback, T data) {
        deliver(callback, data, null);
    }

    /** Trả một thông báo lỗi sau một khoảng trễ. */
    static <T> void error(RepositoryCallback<T> callback, String message) {
        deliver(callback, null, message);
    }

    private static <T> void deliver(final RepositoryCallback<T> callback, final T data, final String errorMessage) {
        long delay = (scenario == Scenario.SLOW) ? 3000 : 500;
        HANDLER.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (scenario == Scenario.ERROR) {
                    callback.onError("Không kết nối được máy chủ. (dữ liệu giả - kịch bản ERROR)");
                } else if (errorMessage != null) {
                    callback.onError(errorMessage);
                } else {
                    callback.onSuccess(data);
                }
            }
        }, delay);
    }

    // ---------------------------------------------------------------- API giả: sản phẩm, danh mục

    static void getCategories(RepositoryCallback<List<Category>> callback) {
        success(callback, new ArrayList<>(categories()));
    }

    static void getProduct(String id, RepositoryCallback<Product> callback) {
        Product found = findProduct(id);
        if (found == null) {
            error(callback, "Không tìm thấy dữ liệu.");
        } else {
            success(callback, found);
        }
    }

    static void search(ProductFilter filter, int page, int size, RepositoryCallback<ProductPage> callback) {
        List<Product> result = new ArrayList<>();
        if (scenario != Scenario.EMPTY) {
            String key = normalize(filter.keyword);
            for (Product p : products()) {
                if (matches(p, filter, key)) {
                    result.add(p);
                }
            }
            sort(result, filter.sortBy, filter.sortDirection);
        }
        success(callback, toPage(result, page, size));
    }

    /** Kiểm tra một sản phẩm có thỏa các điều kiện lọc không. */
    private static boolean matches(Product p, ProductFilter filter, String normalizedKeyword) {
        if (filter.categoryId != null && !filter.categoryId.equals(p.getCategoryId())) {
            return false;
        }
        if (!normalizedKeyword.isEmpty()) {
            boolean found = normalize(p.getName()).contains(normalizedKeyword)
                    || normalize(p.getBrand()).contains(normalizedKeyword)
                    || normalize(p.getDescription()).contains(normalizedKeyword);
            if (!found) {
                return false;
            }
        }
        if (filter.brand != null && !filter.brand.isEmpty()
                && !normalize(p.getBrand()).equals(normalize(filter.brand))) {
            return false;
        }
        if (filter.minPrice != null && p.getPrice() < filter.minPrice) {
            return false;
        }
        if (filter.maxPrice != null && p.getPrice() > filter.maxPrice) {
            return false;
        }
        double rating = (p.getRating() == null) ? 0 : p.getRating();
        if (filter.minRating != null && rating < filter.minRating) {
            return false;
        }
        if (filter.onlyNew && !p.isNewArrival()) {
            return false;
        }
        return true;
    }

    // ---------------------------------------------------------------- Tra cứu cho MockStore

    /** Toàn bộ sản phẩm giả. */
    static List<Product> allProducts() {
        return products();
    }

    static Product findProduct(String id) {
        for (Product p : products()) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    /** Tìm sản phẩm chứa một biến thể (dùng khi thêm giỏ hàng, tạo đơn). */
    static Product findProductByVariantId(String variantId) {
        for (Product p : products()) {
            for (ProductVariant v : p.getVariants()) {
                if (v.getId().equals(variantId)) {
                    return p;
                }
            }
        }
        return null;
    }

    static ProductVariant findVariant(String variantId) {
        Product p = findProductByVariantId(variantId);
        if (p == null) {
            return null;
        }
        for (ProductVariant v : p.getVariants()) {
            if (v.getId().equals(variantId)) {
                return v;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- Tiện ích

    private static ProductPage toPage(List<Product> all, int page, int size) {
        int total = all.size();
        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        JsonObject json = new JsonObject();
        json.addProperty("totalItems", total);
        json.addProperty("totalPages", (int) Math.ceil(total / (double) size));
        json.addProperty("pageSize", size);
        json.addProperty("currentPage", page);
        json.addProperty("hasNext", to < total);
        json.addProperty("hasPrevious", page > 0);
        json.add("products", GSON.toJsonTree(all.subList(from, to)));
        return GSON.fromJson(json, ProductPage.class);
    }

    /** Sắp xếp theo tên, giá hoặc điểm đánh giá, tăng hoặc giảm dần. */
    private static void sort(List<Product> list, final String sortBy, String sortDirection) {
        Comparator<Product> comparator = new Comparator<Product>() {
            @Override
            public int compare(Product a, Product b) {
                if ("price".equals(sortBy)) {
                    return Double.compare(a.getPrice(), b.getPrice());
                }
                if ("rating".equals(sortBy)) {
                    double ra = (a.getRating() == null) ? 0 : a.getRating();
                    double rb = (b.getRating() == null) ? 0 : b.getRating();
                    return Double.compare(ra, rb);
                }
                return normalize(a.getName()).compareTo(normalize(b.getName()));
            }
        };
        Collections.sort(list, comparator);
        if ("desc".equals(sortDirection)) {
            Collections.reverse(list);
        }
    }

    /** Bỏ dấu tiếng Việt và chữ hoa để tìm kiếm giống backend ("ao" khớp "Áo"). */
    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String s = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return s.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).trim();
    }

    // ---------------------------------------------------------------- Dữ liệu giả

    private static synchronized List<Category> categories() {
        if (CATEGORIES.isEmpty()) {
            CATEGORIES.add(category("mock-c-men", "Thời trang nam", "MEN", "Áo thun", "Áo sơ mi", "Quần jean"));
            CATEGORIES.add(category("mock-c-women", "Thời trang nữ", "WOMEN", "Váy", "Đầm", "Áo khoác"));
            CATEGORIES.add(category("mock-c-acc", "Phụ kiện", "ACC", "Mũ", "Túi", "Giày"));
        }
        return CATEGORIES;
    }

    private static synchronized List<Product> products() {
        if (!PRODUCTS.isEmpty()) {
            return PRODUCTS;
        }
        Category men = categories().get(0);
        Category women = categories().get(1);
        Category acc = categories().get(2);
        String[] sizesTop = {"S", "M", "L", "XL"};
        String[] sizesJeans = {"29", "30", "31", "32"};
        String[] sizesShoes = {"38", "39", "40", "41", "42", "43"};
        String[] free = {"Free size"};

        // Thứ tự tham số: id, tên, mô tả, giá, thương hiệu, điểm, danh mục, loại, hàng mới,
        //                 số ảnh, các size, các màu, tồn kho cơ bản
        PRODUCTS.add(product("mock-p-01", "Áo thun basic cổ tròn", "Cotton 100%, form regular, thoáng mát.", 259000, "Routine", 4.5, men, "Áo thun", false, 3, sizesTop, new String[]{"Trắng", "Đen"}, 12));
        PRODUCTS.add(product("mock-p-02", "Áo polo pique", "Áo polo vải cá sấu, cổ dệt bo.", 349000, "Routine", 4.2, men, "Áo polo", false, 2, sizesTop, new String[]{"Xanh navy", "Trắng"}, 8));
        PRODUCTS.add(product("mock-p-03", "Quần jean slim fit", "Denim co giãn nhẹ, ôm vừa phải.", 899000, "Levi's", 4.6, men, "Quần jean", false, 2, sizesJeans, new String[]{"Xanh đậm"}, 6));
        PRODUCTS.add(product("mock-p-04", "Quần short kaki", "Chất kaki mềm, có túi hai bên.", 329000, "Coolmate", 4.0, men, "Quần short", false, 1, sizesTop, new String[]{"Be", "Rêu", "Đen"}, 5));
        PRODUCTS.add(product("mock-p-05", "Áo sơ mi oxford tay dài", "Sơ mi oxford dày dặn, ít nhăn, mặc đi làm hay đi chơi đều đẹp.", 459000, "Owen", 4.8, men, "Áo sơ mi", false, 3, sizesTop, new String[]{"Trắng", "Xanh nhạt", "Hồng"}, 10));
        PRODUCTS.add(product("mock-p-06", "Váy midi hoa nhí", "Váy xòe nhẹ, họa tiết hoa nhí vintage.", 520000, "Yody", 4.4, women, "Váy", false, 2, sizesTop, new String[]{"Hoa vàng", "Hoa hồng"}, 7));
        PRODUCTS.add(product("mock-p-07", "Áo khoác gió siêu nhẹ", "Chống nước nhẹ, gấp gọn bỏ túi.", 690000, "Uniqlo", 4.7, women, "Áo khoác", true, 3, sizesTop, new String[]{"Đen", "Xám", "Be"}, 9));
        PRODUCTS.add(product("mock-p-08", "Đầm suông tay lỡ", "Dáng suông thoải mái, chất đũi mát.", 380000, "Yody", 4.1, women, "Đầm", true, 1, sizesTop, new String[]{"Kem"}, 4));
        PRODUCTS.add(product("mock-p-09", "Mũ lưỡi trai thêu logo", "Mũ cotton, khóa điều chỉnh phía sau.", 159000, "Coolmate", 4.3, acc, "Mũ", false, 2, free, new String[]{"Đen", "Trắng"}, 20));
        // Hết hàng toàn bộ, chưa có đánh giá
        PRODUCTS.add(product("mock-p-10", "Mũ bucket vải dù", "Mũ bucket chống nước nhẹ. Hiện đang hết hàng để test.", 159000, "Coolmate", null, acc, "Mũ", false, 1, free, new String[]{"Rêu"}, 0));
        // Không có mô tả
        PRODUCTS.add(product("mock-p-11", "Túi tote canvas", null, 199000, "Routine", 4.5, acc, "Túi", false, 2, free, new String[]{"Be", "Đen"}, 15));
        // Tên rất dài, nhiều size
        PRODUCTS.add(product("mock-p-12", "Giày sneaker da trắng cổ thấp phối đế êm siêu nhẹ phiên bản giới hạn mùa hè", "Da thật, đế cao su non, êm chân khi đi bộ cả ngày.", 1290000, "Adidas", 4.9, acc, "Giày", true, 3, sizesShoes, new String[]{"Trắng", "Trắng kem"}, 3));
        PRODUCTS.add(product("mock-p-13", "Thắt lưng da bò khóa kim", "Da bò thật, bản 3,5 cm.", 249000, "Owen", 3.9, acc, "Thắt lưng", false, 0, free, new String[]{"Nâu", "Đen"}, 11));
        // Không có ảnh, không có biến thể
        PRODUCTS.add(product("mock-p-14", "Kính mát gọng tròn", "Chống tia UV400.", 320000, "Rayban", null, acc, "Kính", false, 0, new String[0], new String[0], 0));
        return PRODUCTS;
    }

    private static Category category(String id, String name, String code, String... typeNames) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("code", code);
        json.addProperty("description", name);
        JsonArray types = new JsonArray();
        for (String typeName : typeNames) {
            JsonObject type = new JsonObject();
            type.addProperty("id", id + "-" + typeName);
            type.addProperty("name", typeName);
            types.add(type);
        }
        json.add("categoryTypes", types);
        return GSON.fromJson(json, Category.class);
    }

    /**
     * Tạo một sản phẩm giả từ các thông tin cơ bản.
     * baseStock = 0: hết hàng toàn bộ; lớn hơn 0: tồn kho cơ bản (cứ 4 biến thể có 1 cái hết hàng để thử chip bị mờ).
     */
    private static Product product(String id, String name, String description, double price, String brand,
                                   Double rating, Category category, String typeName, boolean isNew,
                                   int imageCount, String[] sizes, String[] colors, int baseStock) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("description", description);
        json.addProperty("price", price);
        json.addProperty("brand", brand);
        json.addProperty("rating", rating);
        json.addProperty("categoryId", category.getId());
        json.addProperty("categoryName", category.getName());
        json.addProperty("categoryTypeId", "mock-t-" + typeName.toLowerCase(Locale.ROOT).replace(' ', '-'));
        json.addProperty("categoryTypeName", typeName);
        json.addProperty("slug", id);
        json.addProperty("newArrival", isNew);

        // Ảnh giả lấy từ placehold.co, ảnh đầu tiên là ảnh chính
        JsonArray resources = new JsonArray();
        String text = normalize(name).replace(' ', '+');
        for (int i = 0; i < imageCount; i++) {
            JsonObject r = new JsonObject();
            r.addProperty("id", id + "-img-" + i);
            r.addProperty("name", id + "-" + i + ".png");
            r.addProperty("url", IMAGE_URL + text + "+" + (i + 1));
            r.addProperty("type", "image/png");
            r.addProperty("isPrimary", i == 0);
            resources.add(r);
        }
        json.add("productResources", resources);
        if (imageCount > 0) {
            json.addProperty("thumbnail", IMAGE_URL + text + "+1");
        }

        // Mỗi cặp (size, màu) là một biến thể
        JsonArray variants = new JsonArray();
        int index = 0;
        for (String size : sizes) {
            for (String color : colors) {
                JsonObject v = new JsonObject();
                v.addProperty("id", id + "-v-" + index);
                v.addProperty("size", size);
                v.addProperty("color", color);
                int stock = 0;
                if (baseStock > 0 && index % 4 != 3) {
                    stock = baseStock + (index * 3) % 7;
                }
                v.addProperty("stockQuantity", stock);
                variants.add(v);
                index++;
            }
        }
        json.add("variants", variants);
        return GSON.fromJson(json, Product.class);
    }
}
