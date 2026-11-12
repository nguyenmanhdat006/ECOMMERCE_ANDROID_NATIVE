package com.example.ecommerce.ui.address;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.os.CancellationSignal;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentAddressFormBinding;
import com.example.ecommerce.model.Address;
import com.example.ecommerce.ui.common.ConfirmDialog;
import com.example.ecommerce.util.CheckoutSession;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.AddressViewModel;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Biểu mẫu thêm địa chỉ mới hoặc sửa địa chỉ (khi có địa chỉ truyền sang qua arguments).
 *
 * Nút "Dùng vị trí hiện tại": xin quyền vị trí, nhắc bật vị trí của thiết bị nếu đang tắt, lấy tọa độ rồi
 * đổi thành địa chỉ chữ (Geocoder) để điền sẵn các ô. Người dùng vẫn sửa được trước khi lưu.
 */
public class AddressFormFragment extends Fragment {

    private static final String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private FragmentAddressFormBinding binding;
    private AddressViewModel viewModel;
    private Address editing; // null nghĩa là đang thêm mới

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private CancellationSignal locationCancel;

    /** Kết quả hộp thoại xin quyền: được cấp thì đi tiếp, bị từ chối thì báo hoặc chỉ cách bật trong Cài đặt. */
    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionResult);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddressFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AddressViewModel.class);

        // Có địa chỉ truyền sang thì đây là màn Sửa: điền sẵn các ô
        String json = (getArguments() == null) ? null : getArguments().getString(Constants.ARG_ADDRESS_JSON);
        if (json != null) {
            editing = new Gson().fromJson(json, Address.class);
            fillFields(editing);
            binding.toolbar.setTitle("Sửa địa chỉ");
        }

        binding.saveButton.setOnClickListener(v -> submit());
        binding.useLocationButton.setOnClickListener(v -> onUseLocationClicked());
        viewModel.getSaveResult().observe(getViewLifecycleOwner(), this::render);
    }

    private void fillFields(Address address) {
        binding.nameInput.setText(address.name);
        binding.phoneInput.setText(address.phoneNumber);
        binding.streetInput.setText(address.street);
        binding.stateInput.setText(address.state);
        binding.cityInput.setText(address.city);
        binding.zipInput.setText(address.zipCode);
    }

    // ---------------------------------------------------------------- dùng vị trí hiện tại

    /** Bước 1: có quyền chưa? Chưa thì xin. */
    private void onUseLocationClicked() {
        if (hasLocationPermission()) {
            checkLocationEnabled();
        } else {
            permissionLauncher.launch(LOCATION_PERMISSIONS);
        }
    }

    private boolean hasLocationPermission() {
        for (String permission : LOCATION_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    private void onPermissionResult(Map<String, Boolean> result) {
        if (binding == null) {
            return;
        }
        if (hasLocationPermission()) {
            checkLocationEnabled();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Từ chối lần này, vẫn xin lại được lần sau
            toast(getString(R.string.location_permission_denied));
        } else {
            // Từ chối hẳn (chọn "Không hỏi lại"): chỉ còn cách bật trong Cài đặt của app
            ConfirmDialog.show(requireContext(), getString(R.string.location_permission_title),
                    getString(R.string.location_permission_message), getString(R.string.open_settings), false,
                    () -> startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", requireContext().getPackageName(), null))));
        }
    }

    /** Bước 2: vị trí của thiết bị đang bật chưa? Chưa thì nhắc bật và mở sẵn trang cài đặt. */
    private void checkLocationEnabled() {
        LocationManager manager = ContextCompat.getSystemService(requireContext(), LocationManager.class);
        if (manager == null || !LocationManagerCompat.isLocationEnabled(manager)) {
            ConfirmDialog.show(requireContext(), getString(R.string.location_off_title),
                    getString(R.string.location_off_message), getString(R.string.open_settings), false,
                    () -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
            return;
        }
        fetchLocation(manager);
    }

    /** Bước 3: lấy tọa độ. Thử lần lượt từng nguồn đang bật (GPS, mạng); không được thì dùng vị trí gần nhất đã biết. */
    private void fetchLocation(LocationManager manager) {
        setLocating(true);
        List<String> providers = new ArrayList<>();
        for (String provider : new String[]{LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER}) {
            if (manager.isProviderEnabled(provider)) {
                providers.add(provider);
            }
        }
        locationCancel = new CancellationSignal();
        requestFrom(manager, providers, 0);
    }

    @SuppressLint("MissingPermission") // đã kiểm tra quyền ở onUseLocationClicked()
    private void requestFrom(LocationManager manager, List<String> providers, int index) {
        if (index >= providers.size()) {
            Location last = lastKnownLocation(manager, providers);
            if (last != null) {
                reverseGeocode(last);
            } else {
                setLocating(false);
                toast(getString(R.string.location_failed));
            }
            return;
        }
        LocationManagerCompat.getCurrentLocation(manager, providers.get(index), locationCancel,
                ContextCompat.getMainExecutor(requireContext()), location -> {
                    if (binding == null) {
                        return;
                    }
                    if (location != null) {
                        reverseGeocode(location);
                    } else {
                        requestFrom(manager, providers, index + 1); // nguồn này không có, thử nguồn kế tiếp
                    }
                });
    }

    @SuppressLint("MissingPermission")
    @Nullable
    private Location lastKnownLocation(LocationManager manager, List<String> providers) {
        Location best = null;
        for (String provider : providers) {
            Location location = manager.getLastKnownLocation(provider);
            if (location != null && (best == null || location.getTime() > best.getTime())) {
                best = location;
            }
        }
        return best;
    }

    /** Bước 4: đổi tọa độ thành địa chỉ chữ. Geocoder cần mạng nên chạy ở luồng nền. */
    private void reverseGeocode(Location location) {
        final android.content.Context appContext = requireContext().getApplicationContext();
        final double latitude = location.getLatitude();
        final double longitude = location.getLongitude();
        new Thread(() -> {
            List<android.location.Address> results = null;
            try {
                if (Geocoder.isPresent()) {
                    results = new Geocoder(appContext, new Locale("vi", "VN")).getFromLocation(latitude, longitude, 1);
                }
            } catch (IOException | IllegalArgumentException ignored) {
                // Không có mạng hoặc tọa độ lạ: coi như không tìm thấy địa chỉ
            }
            final android.location.Address found = (results == null || results.isEmpty()) ? null : results.get(0);
            mainHandler.post(() -> onGeocoded(found));
        }).start();
    }

    private void onGeocoded(@Nullable android.location.Address found) {
        if (binding == null) {
            return; // người dùng đã rời màn hình
        }
        setLocating(false);
        if (found == null) {
            toast(getString(R.string.location_no_address));
            return;
        }
        // Chỉ ghi đè ô nào tìm được; tên gợi nhớ và số điện thoại giữ nguyên
        setIfPresent(binding.streetInput, streetOf(found));
        setIfPresent(binding.stateInput, firstNonEmpty(found.getSubAdminArea(), found.getSubLocality()));
        setIfPresent(binding.cityInput, firstNonEmpty(found.getLocality(), found.getAdminArea()));
        setIfPresent(binding.zipInput, found.getPostalCode());
        binding.streetLayout.setError(null);
        binding.stateLayout.setError(null);
        binding.cityLayout.setError(null);
        binding.zipLayout.setError(null);
        toast(getString(R.string.location_filled));
    }

    /** "Số nhà + tên đường"; thiếu thì lấy đoạn đầu của dòng địa chỉ đầy đủ. */
    private String streetOf(android.location.Address a) {
        String number = a.getSubThoroughfare() == null ? "" : a.getSubThoroughfare();
        String road = a.getThoroughfare() == null ? "" : a.getThoroughfare();
        String street = (number + " " + road).trim();
        if (street.isEmpty() && a.getMaxAddressLineIndex() >= 0 && a.getAddressLine(0) != null) {
            street = a.getAddressLine(0).split(",")[0].trim();
        }
        return street;
    }

    @Nullable
    private String firstNonEmpty(@Nullable String first, @Nullable String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }
        return (second != null && !second.trim().isEmpty()) ? second.trim() : null;
    }

    private void setIfPresent(com.google.android.material.textfield.TextInputEditText input, @Nullable String value) {
        if (value != null && !value.trim().isEmpty()) {
            input.setText(value.trim());
        }
    }

    /** Đang lấy vị trí thì khóa nút và đổi chữ để người dùng biết app đang xử lý. */
    private void setLocating(boolean locating) {
        binding.useLocationButton.setEnabled(!locating);
        binding.useLocationButton.setText(locating ? R.string.location_loading : R.string.location_use);
    }

    private void toast(String text) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show();
    }

    // ---------------------------------------------------------------- lưu địa chỉ (giữ nguyên)

    private void submit() {
        Address address = new Address();
        address.id = (editing == null) ? null : editing.id;
        address.name = binding.nameInput.getText().toString().trim();
        address.phoneNumber = binding.phoneInput.getText().toString().trim();
        address.street = binding.streetInput.getText().toString().trim();
        address.state = binding.stateInput.getText().toString().trim();
        address.city = binding.cityInput.getText().toString().trim();
        address.zipCode = binding.zipInput.getText().toString().trim();

        // Các ô bắt buộc: báo lỗi ngay dưới ô còn trống
        boolean valid = true;
        valid &= requireNotEmpty(binding.phoneLayout, address.phoneNumber);
        valid &= requireNotEmpty(binding.streetLayout, address.street);
        valid &= requireNotEmpty(binding.stateLayout, address.state);
        valid &= requireNotEmpty(binding.cityLayout, address.city);
        valid &= requireNotEmpty(binding.zipLayout, address.zipCode);
        if (valid) {
            viewModel.save(address);
        }
    }

    /** Báo lỗi cho một ô nếu để trống. Trả về true nếu ô hợp lệ. */
    private boolean requireNotEmpty(TextInputLayout layout, String value) {
        if (value.isEmpty()) {
            layout.setError("Không được để trống");
            return false;
        }
        layout.setError(null);
        return true;
    }

    private void render(Resource<Address> result) {
        if (result == null) {
            return;
        }
        switch (result.status) {
            case LOADING:
                binding.saveButton.setLoading(true);
                binding.errorText.setVisibility(View.GONE);
                break;
            case ERROR:
                binding.saveButton.setLoading(false);
                binding.errorText.setText(result.message);
                binding.errorText.setVisibility(View.VISIBLE);
                viewModel.clearSaveResult();
                break;
            case SUCCESS:
                binding.saveButton.setLoading(false);
                viewModel.clearSaveResult();
                // Sửa đúng địa chỉ đang chọn để giao hàng thì cập nhật luôn bản đang chọn
                Address selected = CheckoutSession.getSelectedAddress();
                if (editing != null && selected != null && editing.id.equals(selected.id)) {
                    CheckoutSession.setSelectedAddress(result.data);
                }
                NavHostFragment.findNavController(this).popBackStack();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationCancel != null) {
            locationCancel.cancel(); // dừng chờ vị trí khi rời màn hình
        }
        binding = null;
    }
}