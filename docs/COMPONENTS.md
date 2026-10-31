# Hướng dẫn dự án Android (Java + XML, MVVM, không dùng DI)

## 1. Chạy app

- Mở thư mục `Ecommerce_android` bằng Android Studio, bấm **Sync Project with Gradle Files**, chọn emulator rồi Run.
- Mặc định app dùng **dữ liệu giả** (không cần backend): cờ `USE_MOCK_DATA = true` trong `app/build.gradle.kts`.
  - Tài khoản thử: `lan@example.com` / `Test@12345`. Đăng ký tài khoản mới thì mã xác thực là `123456`.
  - Dữ liệu giả nằm trong bộ nhớ, tắt app là mất (giỏ hàng, đơn hàng, đánh giá mới tạo).
- Chạy với backend thật: bật Spring Boot ở cổng 8080, đổi `USE_MOCK_DATA` thành `false`, Sync lại.
  Emulator gọi máy tính qua `10.0.2.2` (`BASE_URL`). Máy thật: đổi `BASE_URL` sang IP máy tính.
- Xem các trạng thái giao diện khác khi dùng dữ liệu giả: đặt `MockDataSource.scenario` thành `EMPTY`, `ERROR` hoặc `SLOW`.

## 2. Cấu trúc thư mục (`app/src/main/java/com/example/ecommerce`)

| Thư mục | Vai trò |
| --- | --- |
| `model/` | Lớp dữ liệu khớp JSON của backend (Product, Order, Address...) |
| `service/` | `ApiService` (danh sách API), `RetrofitClient` (tạo Retrofit, tự gắn token) |
| `repository/` | Lấy dữ liệu: gọi API hoặc dữ liệu giả (`MockDataSource`, `MockStore`) |
| `viewmodel/` | Giữ trạng thái và logic của từng màn hình |
| `ui/` | Các màn hình (Fragment), adapter và component dùng chung (`ui/common`) |
| `util/` | Hàm tiện ích: định dạng, lưu phiên đăng nhập, hằng số |

Luồng dữ liệu: `Fragment` → `ViewModel` → `Repository` → `ApiService` (hoặc dữ liệu giả) → backend.
Không dùng DI: ViewModel tự `new` repository, Retrofit là singleton trong `RetrofitClient`.

## 3. Các màn hình (`ui/`) và API liên quan

| Nhóm | Màn hình | API |
| --- | --- | --- |
| auth | Đăng nhập, Đăng ký, Xác thực email | `/api/auth/login`, `/register`, `/verify` |
| home | Trang chủ | `/api/products/search`, `/api/category` |
| category | Danh mục | `/api/category` |
| search | Tìm kiếm + bảng Lọc | `/api/products/search` (keyword, giá, thương hiệu, đánh giá, hàng mới, sắp xếp) |
| product | Chi tiết sản phẩm (chọn size, màu, số lượng) | `/api/products/{id}`, `/api/carts` |
| review | Danh sách đánh giá, Viết hoặc sửa đánh giá | `/api/reviews` |
| cart | Giỏ hàng | `/api/carts` |
| checkout | Thanh toán (COD), Kết quả đặt hàng | `/api/orders` |
| address | Danh sách địa chỉ, Thêm hoặc sửa địa chỉ | `/api/addresses` |
| order | Danh sách đơn, Chi tiết đơn, Chờ đánh giá | `/api/orders/me`, `/{id}`, `/unreviewed` |
| account | Tài khoản, Cài đặt (đăng xuất) | lưu cục bộ (`SessionManager`) |

Chưa có theo API hiện tại: hủy đơn của khách, gửi lại mã xác thực, sửa hồ sơ, quên mật khẩu, thanh toán VNPAY (đã bỏ).

## 4. Component dùng chung (`ui/common`)

| Mã | Component | Cách dùng nhanh |
| --- | --- | --- |
| C1 | Theme, màu, font, style | `res/values/colors.xml, themes.xml, styles.xml`; chữ dùng `TextAppearance.App.*`, nút dùng `Widget.App.Button.*` |
| C2 | `AppToolbar` | `<AppToolbar app:title="..." app:showBack="true"/>`; `setAction(1, icon, mô tả, listener)` |
| C3 | `SearchHeader`, `CartButton` | `setOnQueryChangeListener(q -> ...)` (debounce 400 ms); `setAsButton(...)`; `CartButton.setCount(n)` |
| C4 | Khung + thanh điều hướng dưới | `MainActivity`, `navigation/nav_graph.xml`, `menu/bottom_nav_menu.xml` |
| C5 | `StateView` | `showLoading()`, `showEmpty("...")`, `showError("...", retry)`, `hide()` |
| C6 | `LoadingButton`, style ô nhập | `setLoading(true)` khi gọi API; ô nhập dùng `Widget.App.TextInput` |
| C7 | `ProductAdapter` (thẻ sản phẩm) | `new ProductAdapter(product -> ...)`, `submitList(list)` |
| C8 | `LineItemView` | `bind(ảnh, tên, phân loại, giá)` |
| C9 | `QuantityStepper` | `setMax(n)`, `setQuantity(n)`, `setOnQuantityChangeListener(...)` |
| C10 | `BottomActionBar` | `setLabel`, `setPrice`, `setPrimaryButton(text, listener)` |
| C11 | `AddressCardView` | `bind(tên, sđt, địa chỉ, đangChọn)` |
| C12 | `StarRatingView`, `ReviewItemView` | `setRating(4.6f)`; `setEditable(true, ...)`; `ReviewItemView.bind(...)` |
| C13 | `OrderStatusView` | `setStatus("PENDING")` |
| C14 | `ConfirmDialog` | `ConfirmDialog.show(ctx, tiêu đề, nội dung, "Xóa", true, () -> ...)` |
| – | `LoginPromptView` | Lời nhắc đăng nhập cho tab cần tài khoản |

## 5. Quy ước khi viết thêm

- **Thêm API mới:** khai báo trong `service/ApiService.java` → bọc trong Repository (kèm nhánh dữ liệu giả nếu muốn) → gọi từ ViewModel.
- **Thêm màn hình:** tạo `fragment_xxx.xml` + `XxxFragment.java` (ViewBinding: `FragmentXxxBinding`), khai báo trong `nav_graph.xml`,
  chuyển màn bằng `NavHostFragment.findNavController(this).navigate(R.id.xxxFragment, bundle)`. Khóa dữ liệu truyền sang để ở `util/Constants.java`.
- **Màn hình cần đăng nhập:** `if (!AuthGuard.requireLogin(this)) return;`
- **Kết quả một lần (đăng nhập, đặt hàng...):** sau khi xử lý xong gọi `clear...()` của ViewModel để không bị xử lý lại khi quay về màn hình.
- Chữ hiển thị của các màn mới viết thẳng trong layout cho dễ đọc; muốn đa ngôn ngữ thì chuyển vào `strings.xml`.
- Ảnh sản phẩm có URL `localhost` được tự đổi sang host của `BASE_URL` (`ImageUrls.fix`).
