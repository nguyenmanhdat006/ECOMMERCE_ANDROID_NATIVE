package com.example.ecommerce.ui.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentSearchBinding;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductFilter;
import com.example.ecommerce.ui.product.ProductAdapter;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.ProductListViewModel;

import java.util.List;

/**
 * Màn hình Tìm kiếm: gõ từ khóa, sắp xếp, lọc nâng cao, hoặc xem sản phẩm của một danh mục.
 * Nhận (không bắt buộc) từ khóa và danh mục qua arguments.
 */
public class SearchFragment extends Fragment implements FilterBottomSheet.Listener {

    private FragmentSearchBinding binding;
    private ProductListViewModel viewModel;
    private ProductAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ProductListViewModel.class);

        // Đọc điều kiện ban đầu do màn hình trước truyền sang (có thể không có)
        Bundle args = getArguments();
        String keyword = (args == null) ? null : args.getString(Constants.ARG_KEYWORD);
        String categoryId = (args == null) ? null : args.getString(Constants.ARG_CATEGORY_ID);
        String categoryName = (args == null) ? null : args.getString(Constants.ARG_CATEGORY_NAME);
        viewModel.start(keyword, categoryId);

        setupHeader(keyword, categoryId, categoryName);
        setupList();

        viewModel.getProducts().observe(getViewLifecycleOwner(), this::render);
        viewModel.getLoadingMore().observe(getViewLifecycleOwner(),
                loading -> binding.loadMoreProgress.setVisibility(loading ? View.VISIBLE : View.GONE));
        showFilterSummary();
    }

    private void setupHeader(String keyword, String categoryId, String categoryName) {
        binding.backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        if (keyword != null) {
            binding.searchHeader.setQuery(keyword);
        } else if (categoryId == null) {
            binding.searchHeader.focusInput(); // mở từ ô tìm ở Trang chủ: bật bàn phím luôn
        }
        binding.searchHeader.setOnQueryChangeListener(query -> viewModel.setKeyword(query));
        binding.searchHeader.setOnFilterClickListener(v ->
                FilterBottomSheet.show(getChildFragmentManager(), viewModel.getFilter()));

        // Nút sắp xếp
        binding.sortButton.setText(sortLabel() + " ▾");
        binding.sortButton.setOnClickListener(v -> showSortMenu());

        // Chip danh mục đang lọc: bấm vào để bỏ lọc danh mục
        if (categoryName != null && viewModel.getFilter().categoryId != null) {
            binding.categoryChip.setText(categoryName + "  ✕");
            binding.categoryChip.setVisibility(View.VISIBLE);
            binding.categoryChip.setOnClickListener(v -> {
                binding.categoryChip.setVisibility(View.GONE);
                viewModel.setCategory(null);
            });
        }
    }

    private void setupList() {
        adapter = new ProductAdapter(this::openDetail);
        final GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        binding.productList.setLayoutManager(layoutManager);
        binding.productList.setAdapter(adapter);

        // Cuộn gần tới cuối thì tải thêm
        binding.productList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                if (dy > 0 && lastVisible >= adapter.getItemCount() - 4) {
                    viewModel.loadMore();
                }
            }
        });
    }

    private void render(Resource<List<Product>> result) {
        switch (result.status) {
            case LOADING:
                binding.productList.setVisibility(View.GONE);
                binding.stateView.showLoading();
                break;
            case SUCCESS:
                if (result.data == null || result.data.isEmpty()) {
                    binding.productList.setVisibility(View.GONE);
                    binding.stateView.showEmpty("Không tìm thấy sản phẩm phù hợp. Thử từ khóa khác hoặc bỏ bớt bộ lọc.");
                } else {
                    binding.stateView.hide();
                    binding.productList.setVisibility(View.VISIBLE);
                    adapter.submitList(result.data);
                }
                break;
            case ERROR:
                binding.productList.setVisibility(View.GONE);
                binding.stateView.showError(result.message, v -> viewModel.refresh());
                break;
        }
    }

    /** Khi người dùng bấm "Áp dụng" trong bảng lọc. */
    @Override
    public void onFilterApplied(ProductFilter filter) {
        viewModel.applyAdvancedFilter(filter);
        showFilterSummary();
    }

    private void showFilterSummary() {
        int count = viewModel.getFilter().countActive();
        binding.filterSummary.setText(count == 0 ? "" : "Đang lọc: " + count);
    }

    private void showSortMenu() {
        PopupMenu popup = new PopupMenu(requireContext(), binding.sortButton);
        popup.getMenuInflater().inflate(R.menu.menu_sort, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.sort_name) {
                viewModel.setSort("name", "asc");
            } else if (id == R.id.sort_price_asc) {
                viewModel.setSort("price", "asc");
            } else if (id == R.id.sort_price_desc) {
                viewModel.setSort("price", "desc");
            } else if (id == R.id.sort_rating) {
                viewModel.setSort("rating", "desc");
            }
            binding.sortButton.setText(sortLabel() + " ▾");
            return true;
        });
        popup.show();
    }

    /** Tên cách sắp xếp đang dùng để hiện trên nút. */
    private String sortLabel() {
        ProductFilter filter = viewModel.getFilter();
        if ("price".equals(filter.sortBy)) {
            return "asc".equals(filter.sortDirection) ? "Giá tăng dần" : "Giá giảm dần";
        }
        if ("rating".equals(filter.sortBy)) {
            return "Đánh giá cao nhất";
        }
        return "Tên A–Z";
    }

    private void openDetail(Product product) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_PRODUCT_ID, product.getId());
        NavHostFragment.findNavController(this).navigate(R.id.productDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
