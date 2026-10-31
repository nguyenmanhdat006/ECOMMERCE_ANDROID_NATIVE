package com.example.ecommerce.ui.address;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.databinding.FragmentAddressFormBinding;
import com.example.ecommerce.model.Address;
import com.example.ecommerce.util.CheckoutSession;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.AddressViewModel;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

/** Biểu mẫu thêm địa chỉ mới hoặc sửa địa chỉ (khi có địa chỉ truyền sang qua arguments). */
public class AddressFormFragment extends Fragment {

    private FragmentAddressFormBinding binding;
    private AddressViewModel viewModel;
    private Address editing; // null nghĩa là đang thêm mới

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
        binding = null;
    }
}
