# Component dùng chung (Java + XML, không dùng DI)

Tất cả nằm trong `app/src/main/java/com/example/ecommerce/ui/common/` (mỗi cái kèm một layout `view_*.xml`),
trừ C1 (tài nguyên) và C7 (`ui/product/ProductAdapter`).

| Mã | Component | Cách dùng nhanh |
| --- | --- | --- |
| C1 | Theme, màu, font, style | `res/values/colors.xml, themes.xml, styles.xml, dimens.xml`; chữ dùng `TextAppearance.App.*`, nút dùng `Widget.App.Button.*` |
| C2 | `AppToolbar` | `<AppToolbar app:title="..." app:showBack="true"/>`; `setAction(1, icon, mô tả, listener)` |
| C3 | `SearchHeader`, `CartButton` | `setOnQueryChangeListener(q -> ...)` (đã debounce 400 ms), `CartButton.setCount(n)` |
| C4 | Khung + thanh điều hướng dưới | `MainActivity`, `navigation/nav_graph.xml`, `menu/bottom_nav_menu.xml`. Id tab phải trùng id màn hình trong nav_graph |
| C5 | `StateView` | `showLoading()`, `showEmpty("...")`, `showError("...", retryListener)`, `hide()` |
| C6 | `LoadingButton`, style ô nhập | `setLoading(true)` khi gọi API; ô nhập dùng `Widget.App.TextInput` với `TextInputLayout` |
| C7 | `ProductAdapter` (thẻ sản phẩm) | `new ProductAdapter(product -> ...)`, `submitList(list)` |
| C8 | `LineItemView` | `bind(imageUrl, tên, phân loại, giá)`; `getEndContainer()` để gắn `QuantityStepper` hoặc nút xóa |
| C9 | `QuantityStepper` | `setMax(n)`, `setQuantity(n)`, `setOnQuantityChangeListener(q -> ...)` |
| C10 | `BottomActionBar` | `setLabel`, `setPrice`, `setPrimaryButton(text, listener)`, `setSecondaryIconButton(...)` |
| C11 | `AddressCardView` | `bind(tên, sđt, địa chỉ, đangChọn)` |
| C12 | `StarRatingView`, `ReviewItemView` | `setRating(4.6f)`; `setEditable(true, rating -> ...)` khi viết đánh giá; `ReviewItemView.bind(...)` |
| C13 | `OrderStatusView` | `setStatus("PENDING")` (PENDING, SHIPPING, WAIT_DELIVER, PAID, CANCELED, REFUND) |
| C14 | `ConfirmDialog` | `ConfirmDialog.show(ctx, tiêu đề, nội dung, "Xóa", true, () -> ...)` |

## Luồng dữ liệu (MVVM, không DI)

`Fragment` -> `ViewModel` (tự `new ProductRepository()`) -> `Repository` -> `RetrofitClient.getApiService()` -> backend.

- Thêm API: khai báo trong `service/ApiService.java`, bọc trong một Repository, gọi từ ViewModel qua `RepositoryCallback`.
- ViewModel tạo bằng `new ViewModelProvider(this).get(X.class)` (constructor không tham số).
- Khi làm đăng nhập: thêm interceptor gắn `Authorization: Bearer <token>` trong `RetrofitClient`.

## Thêm một màn hình mới

1. Tạo `fragment_xxx.xml` và `XxxFragment.java` (dùng ViewBinding: `FragmentXxxBinding`).
2. Khai báo `<fragment>` trong `navigation/nav_graph.xml`; chuyển màn bằng `NavHostFragment.findNavController(this).navigate(R.id...., bundle)`.
3. Tab chính: thay `android:name` của tab đang dùng `PlaceholderFragment` bằng Fragment thật.

## Dữ liệu giả (mock) khi backend chưa chạy

- Cờ `USE_MOCK_DATA` trong `app/build.gradle.kts` (mặc định `true`): bật thì app dùng dữ liệu giả, không cần backend.
  Chuyển sang backend thật: đổi thành `false`, rồi Sync Project.
- Dữ liệu nằm ở `repository/MockDataSource.java` (14 sản phẩm, 3 danh mục). Có đủ trường hợp để kiểm tra giao diện:
  còn hàng, hết hàng, không ảnh, không đánh giá, không mô tả, tên rất dài, nhiều size và màu (có biến thể hết hàng), hàng mới.
  Tìm kiếm, lọc danh mục, sắp xếp và phân trang chạy trên dữ liệu giả giống backend.
- Xem các trạng thái khác của giao diện: đặt `MockDataSource.scenario` thành `EMPTY` (rỗng), `ERROR` (lỗi + nút Thử lại)
  hoặc `SLOW` (đang tải 3 giây); mặc định là `NORMAL`.
- Thêm sản phẩm giả mới: thêm một dòng `PRODUCTS.add(product(...))` trong `MockDataSource.products()`.
- Khi thêm API mới (giỏ hàng, đơn hàng...): thêm hàm mock tương ứng trong `MockDataSource` và rẽ nhánh theo
  `Constants.USE_MOCK_DATA` trong Repository, giống `ProductRepository`.

## Chạy app

Backend chạy ở cổng 8080; emulator gọi qua `10.0.2.2` (`BASE_URL` trong `app/build.gradle.kts`).
Máy thật: đổi `BASE_URL` sang IP máy tính. Ảnh có URL `localhost` được tự đổi sang host của `BASE_URL`.
