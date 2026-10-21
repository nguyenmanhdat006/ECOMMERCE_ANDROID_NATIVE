package com.example.ecommerce.repository;

import android.os.Handler;
import android.os.Looper;

import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductPage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * DỮ LIỆU GIẢ để dựng và kiểm tra giao diện khi backend chưa chạy.
 *
 * Bật/tắt: buildConfigField USE_MOCK_DATA trong app/build.gradle.kts (true = dùng dữ liệu giả).
 * Đổi kịch bản để xem các trạng thái giao diện: gán MockDataSource.scenario (xem enum Scenario),
 * ví dụ đặt Scenario.ERROR để xem màn hình lỗi, Scenario.EMPTY để xem màn hình rỗng.
 *
 * Dữ liệu có đủ các trường hợp: còn hàng, hết hàng, không có ảnh, không có đánh giá, không có mô tả,
 * tên rất dài, nhiều size và màu (có biến thể hết hàng), hàng mới.
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

    // ---------------------------------------------------------------- API giả

    static void getCategories(RepositoryCallback<List<Category>> callback) {
        respond(() -> callback.onSuccess(new ArrayList<>(categories())), callback);
    }

    static void getProduct(String id, RepositoryCallback<Product> callback) {
        respond(() -> {
            for (Product p : products()) {
                if (p.getId().equals(id)) {
                    callback.onSuccess(p);
                    return;
                }
            }
            callback.onError("Không tìm thấy dữ liệu.");
        }, callback);
    }

    static void search(String keyword, String categoryId, String sortBy, String sortDirection,
                       int page, int size, RepositoryCallback<ProductPage> callback) {
        respond(() -> {
            List<Product> result = new ArrayList<>();
            if (scenario != Scenario.EMPTY) {
                String key = normalize(keyword);
                for (Product p : products()) {
                    boolean matchCategory = categoryId == null || categoryId.equals(p.getCategoryId());
                    boolean matchKeyword = key.isEmpty()
                            || normalize(p.getName()).contains(key)
                            || normalize(p.getBrand()).contains(key)
                            || normalize(p.getDescription()).contains(key);
                    if (matchCategory && matchKeyword) {
                        result.add(p);
                    }
                }
                sort(result, sortBy, sortDirection);
            }
            callback.onSuccess(toPage(result, page, size));
        }, callback);
    }

    // ---------------------------------------------------------------- tiện ích

    /** Giả lập độ trễ mạng và các kịch bản lỗi. */
    private static void respond(Runnable onSuccess, RepositoryCallback<?> callback) {
        long delay = scenario == Scenario.SLOW ? 3000 : 500;
        HANDLER.postDelayed(() -> {
            if (scenario == Scenario.ERROR) {
                callback.onError("Không kết nối được máy chủ. (dữ liệu giả - kịch bản ERROR)");
            } else {
                onSuccess.run();
            }
        }, delay);
    }

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

    private static void sort(List<Product> list, String sortBy, String sortDirection) {
        Comparator<Product> comparator;
        if ("price".equals(sortBy)) {
            comparator = Comparator.comparingDouble(Product::getPrice);
        } else if ("rating".equals(sortBy)) {
            comparator = Comparator.comparingDouble(p -> p.getRating() == null ? 0 : p.getRating());
        } else {
            comparator = Comparator.comparing(p -> normalize(p.getName()));
        }
        if ("desc".equals(sortDirection)) {
            comparator = comparator.reversed();
        }
        list.sort(comparator);
    }

    /** Bỏ dấu tiếng Việt và chữ hoa để tìm kiếm giống backend ("ao" khớp "Áo"). */
    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String s = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return s.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).trim();
    }

    // ---------------------------------------------------------------- dữ liệu

    private static synchronized List<Category> categories() {
        if (CATEGORIES.isEmpty()) {
            CATEGORIES.add(category("mock-c-men", "Thời trang nam", "MEN"));
            CATEGORIES.add(category("mock-c-women", "Thời trang nữ", "WOMEN"));
            CATEGORIES.add(category("mock-c-acc", "Phụ kiện", "ACC"));
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

        //                id        tên                                          mô tả                    giá     thương hiệu  điểm  danh mục loại       mới    ảnh size         màu                              tồn kho
        PRODUCTS.add(product("mock-p-01", "Áo thun basic cổ tròn", "Cotton 100%, form regular, thoáng mát.", 259000, "Routine", 4.5, men, "Áo thun", false, 3, sizesTop, new String[]{"Trắng", "Đen"}, 12));
        PRODUCTS.add(product("mock-p-02", "Áo polo pique", "Áo polo vải cá sấu, cổ dệt bo.", 349000, "Routine", 4.2, men, "Áo polo", false, 2, sizesTop, new String[]{"Xanh navy", "Trắng"}, 8));
        PRODUCTS.add(product("mock-p-03", "Quần jean slim fit", "Denim co giãn nhẹ, ôm vừa phải.", 899000, "Levi's", 4.6, men, "Quần jean", false, 2, sizesJeans, new String[]{"Xanh đậm"}, 6));
        PRODUCTS.add(product("mock-p-04", "Quần short kaki", "Chất kaki mềm, có túi hai bên.", 329000, "Coolmate", 4.0, men, "Quần short", false, 1, sizesTop, new String[]{"Be", "Rêu", "Đen"}, 5));
        PRODUCTS.add(product("mock-p-05", "Áo sơ mi oxford tay dài", "Sơ mi oxford dày dặn, ít nhăn, mặc đi làm hay đi chơi đều đẹp.", 459000, "Owen", 4.8, men, "Áo sơ mi", false, 3, sizesTop, new String[]{"Trắng", "Xanh nhạt", "Hồng"}, 10));
        PRODUCTS.add(product("mock-p-06", "Váy midi hoa nhí", "Váy xòe nhẹ, họa tiết hoa nhí vintage.", 520000, "Yody", 4.4, women, "Váy", false, 2, sizesTop, new String[]{"Hoa vàng", "Hoa hồng"}, 7));
        PRODUCTS.add(product("mock-p-07", "Áo khoác gió siêu nhẹ", "Chống nước nhẹ, gấp gọn bỏ túi.", 690000, "Uniqlo", 4.7, women, "Áo khoác", true, 3, sizesTop, new String[]{"Đen", "Xám", "Be"}, 9));
        PRODUCTS.add(product("mock-p-08", "Đầm suông tay lỡ", "Dáng suông thoải mái, chất đũi mát.", 380000, "Yody", 4.1, women, "Đầm", true, 1, sizesTop, new String[]{"Kem"}, 4));
        PRODUCTS.add(product("mock-p-09", "Mũ lưỡi trai thêu logo", "Mũ cotton, khóa điều chỉnh phía sau.", 159000, "Coolmate", 4.3, acc, "Mũ", false, 2, free, new String[]{"Đen", "Trắng"}, 20));
        // Hết hàng toàn bộ + chưa có đánh giá
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

    private static Category category(String id, String name, String code) {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("name", name);
        json.addProperty("code", code);
        json.addProperty("description", name);
        return GSON.fromJson(json, Category.class);
    }

    /**
     * @param baseStock 0 = hết hàng toàn bộ; lớn hơn 0 = tồn kho cơ bản (một số biến thể sẽ hết hàng để thử chip bị mờ).
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

        JsonArray variants = new JsonArray();
        int index = 0;
        for (String size : sizes) {
            for (String color : colors) {
                JsonObject v = new JsonObject();
                v.addProperty("id", id + "-v-" + index);
                v.addProperty("size", size);
                v.addProperty("color", color);
                // Cứ 4 biến thể thì 1 cái hết hàng để thử chip bị mờ
                int stock = baseStock == 0 ? 0 : (index % 4 == 3 ? 0 : baseStock + (index * 3) % 7);
                v.addProperty("stockQuantity", stock);
                variants.add(v);
                index++;
            }
        }
        json.add("variants", variants);
        return GSON.fromJson(json, Product.class);
    }
}
