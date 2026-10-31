package com.example.ecommerce.ui.address;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentAddressListBinding;
import com.example.ecommerce.model.Address;
import com.example.ecommerce.ui.common.ConfirmDialog;
import com.example.ecommerce.util.CheckoutSession;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.AddressViewModel;
import com.google.gson.Gson;

import java.util.List;

/**
 * Danh sách địa chỉ giao hàng.
 * Chế độ chọn (mở từ màn Thanh toán): bấm một địa chỉ để chọn rồi quay lại.
 */
public class AddressListFragment extends Fragment {

    private FragmentAddressListBinding binding;
    private AddressViewModel viewModel;
    private AddressAdapter adapter;
    private boolean selectMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddressListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AddressViewModel.class);
        selectMode = getArguments() != null && getArguments().getBoolean(Constants.ARG_SELECT_MODE, false);

        if (selectMode) {
            binding.toolbar.setTitle("Chọn địa chỉ giao hàng");
        }

        adapter = new AddressAdapter(new AddressAdapter.OnAddressActionListener() {
            @Override
            public void onClick(Address address) {
                if (selectMode) {
                    CheckoutSession.setSelectedAddress(address);
                    NavHostFragment.findNavController(AddressListFragment.this).popBackStack();
                }
            }

            @Override
            public void onEdit(Address address) {
                openForm(address);
            }

            @Override
            public void onDelete(Address address) {
                confirmDelete(address);
            }
        });
        binding.addressList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.addressList.setAdapter(adapter);

        binding.addButton.setOnClickListener(v -> openForm(null));
        binding.toolbar.setAction(1, R.drawable.ic_add, "Thêm địa chỉ", v -> openForm(null));

        viewModel.getAddresses().observe(getViewLifecycleOwner(), this::render);
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearMessage();
            }
        });
    }

    /** Tải lại mỗi khi quay về (sau khi thêm hoặc sửa địa chỉ). */
    @Override
    public void onResume() {
        super.onResume();
        viewModel.load();
    }

    private void render(Resource<List<Address>> result) {
        switch (result.status) {
            case LOADING:
                if (adapter.getItemCount() == 0) {
                    binding.stateView.showLoading();
                }
                break;
            case SUCCESS:
                if (result.data == null || result.data.isEmpty()) {
                    binding.addressList.setVisibility(View.GONE);
                    binding.stateView.showEmpty("Bạn chưa có địa chỉ nào.", R.drawable.ic_location);
                } else {
                    binding.stateView.hide();
                    binding.addressList.setVisibility(View.VISIBLE);
                    adapter.submit(result.data);
                }
                break;
            case ERROR:
                binding.addressList.setVisibility(View.GONE);
                binding.stateView.showError(result.message, v -> viewModel.load());
                break;
        }
    }

    /** Mở biểu mẫu: address = null là thêm mới, khác null là sửa. */
    private void openForm(@Nullable Address address) {
        Bundle args = new Bundle();
        if (address != null) {
            args.putString(Constants.ARG_ADDRESS_JSON, new Gson().toJson(address));
        }
        NavHostFragment.findNavController(this).navigate(R.id.addressFormFragment, args);
    }

    private void confirmDelete(final Address address) {
        ConfirmDialog.show(requireContext(), "Xóa địa chỉ?", address.getFullLine(), "Xóa", true, () -> {
            // Nếu xóa đúng địa chỉ đang chọn để giao hàng thì bỏ chọn
            Address selected = CheckoutSession.getSelectedAddress();
            if (selected != null && selected.id.equals(address.id)) {
                CheckoutSession.setSelectedAddress(null);
            }
            viewModel.delete(address);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
