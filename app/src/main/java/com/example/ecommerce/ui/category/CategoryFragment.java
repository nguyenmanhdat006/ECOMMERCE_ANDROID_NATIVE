package com.example.ecommerce.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentCategoryBinding;
import com.example.ecommerce.model.Category;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.CategoryViewModel;

import java.util.List;

/** Màn hình Danh mục: bấm một danh mục để xem các sản phẩm thuộc danh mục đó. */
public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private CategoryViewModel viewModel;
    private CategoryCardAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        adapter = new CategoryCardAdapter(this::openCategory);
        binding.categoryList.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.categoryList.setAdapter(adapter);

        viewModel.getCategories().observe(getViewLifecycleOwner(), this::render);
        if (viewModel.getCategories().getValue() == null) {
            viewModel.load();
        }
    }

    private void render(Resource<List<Category>> result) {
        switch (result.status) {
            case LOADING:
                binding.categoryList.setVisibility(View.GONE);
                binding.stateView.showLoading();
                break;
            case SUCCESS:
                if (result.data == null || result.data.isEmpty()) {
                    binding.categoryList.setVisibility(View.GONE);
                    binding.stateView.showEmpty("Chưa có danh mục nào.", R.drawable.ic_apps);
                } else {
                    binding.stateView.hide();
                    binding.categoryList.setVisibility(View.VISIBLE);
                    adapter.submit(result.data);
                }
                break;
            case ERROR:
                binding.categoryList.setVisibility(View.GONE);
                binding.stateView.showError(result.message, v -> viewModel.load());
                break;
        }
    }

    /** Mở màn Tìm kiếm đã lọc sẵn theo danh mục được chọn. */
    private void openCategory(Category category) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_CATEGORY_ID, category.getId());
        args.putString(Constants.ARG_CATEGORY_NAME, category.getName());
        NavHostFragment.findNavController(this).navigate(R.id.searchFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
