package com.example.ecommerce.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.example.ecommerce.databinding.SheetFilterBinding;
import com.example.ecommerce.model.ProductFilter;
import com.example.ecommerce.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

/**
 * Bảng "Lọc sản phẩm" trượt từ dưới lên: khoảng giá, thương hiệu, đánh giá tối thiểu, chỉ hàng mới.
 *
 * Cách dùng: FilterBottomSheet.show(getChildFragmentManager(), filterHienTai).
 * Fragment cha phải implement FilterBottomSheet.Listener để nhận kết quả khi bấm "Áp dụng".
 */
public class FilterBottomSheet extends BottomSheetDialogFragment {

    /** Fragment cha implement để nhận bộ lọc người dùng vừa chọn. */
    public interface Listener {
        void onFilterApplied(ProductFilter filter);
    }

    private static final String ARG_FILTER_JSON = "filterJson";
    private static final Gson GSON = new Gson();

    private SheetFilterBinding binding;

    /** Mở bảng lọc, điền sẵn giá trị của bộ lọc hiện tại. */
    public static void show(FragmentManager fragmentManager, ProductFilter current) {
        FilterBottomSheet sheet = new FilterBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER_JSON, GSON.toJson(current));
        sheet.setArguments(args);
        sheet.show(fragmentManager, "filter");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = SheetFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ProductFilter current = GSON.fromJson(requireArguments().getString(ARG_FILTER_JSON), ProductFilter.class);
        fillFields(current);

        binding.clearButton.setOnClickListener(v -> fillFields(new ProductFilter()));
        binding.applyButton.setOnClickListener(v -> apply());
    }

    /** Điền các ô theo một bộ lọc. */
    private void fillFields(ProductFilter filter) {
        binding.minPriceInput.setText(filter.minPrice == null ? "" : String.valueOf(filter.minPrice.longValue()));
        binding.maxPriceInput.setText(filter.maxPrice == null ? "" : String.valueOf(filter.maxPrice.longValue()));
        binding.brandInput.setText(filter.brand == null ? "" : filter.brand);
        binding.onlyNewSwitch.setChecked(filter.onlyNew);

        if (filter.minRating == null) {
            binding.ratingGroup.check(R.id.rating_any);
        } else if (filter.minRating >= 4) {
            binding.ratingGroup.check(R.id.rating_4);
        } else {
            binding.ratingGroup.check(R.id.rating_3);
        }
    }

    /** Đọc các ô nhập thành một bộ lọc và gửi cho Fragment cha. */
    private void apply() {
        ProductFilter result = new ProductFilter();
        result.minPrice = parseNumber(binding.minPriceInput.getText().toString());
        result.maxPrice = parseNumber(binding.maxPriceInput.getText().toString());
        String brand = binding.brandInput.getText().toString().trim();
        result.brand = brand.isEmpty() ? null : brand;
        result.onlyNew = binding.onlyNewSwitch.isChecked();

        int checkedId = binding.ratingGroup.getCheckedChipId();
        if (checkedId == R.id.rating_4) {
            result.minRating = 4.0;
        } else if (checkedId == R.id.rating_3) {
            result.minRating = 3.0;
        }

        if (getParentFragment() instanceof Listener) {
            ((Listener) getParentFragment()).onFilterApplied(result);
        }
        dismiss();
    }

    /** "250000" -> 250000.0; ô trống hoặc không phải số -> null (không lọc). */
    private Double parseNumber(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
