package com.example.ecommerce.ui.home;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecommerce.R;
import com.example.ecommerce.databinding.FragmentHomeBinding;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.ui.product.ProductAdapter;
import com.example.ecommerce.util.Constants;
import com.example.ecommerce.util.Resource;
import com.example.ecommerce.viewmodel.ProductListViewModel;

import java.util.List;

/**
 * Trang chủ / danh sách sản phẩm: header, tìm kiếm, chip danh mục, banner, lưới sản phẩm.
 * Fragment chỉ hiển thị; việc tải dữ liệu nằm ở ProductListViewModel.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ProductListViewModel viewModel;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(ProductListViewModel.class);

        setupLists();
        setupHeader();

        binding.swipeRefresh.setOnRefreshListener(viewModel::refresh);
        binding.scroll.setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    View content = v.getChildAt(0);
                    boolean nearBottom = content != null
                            && content.getMeasuredHeight() - v.getMeasuredHeight() - scrollY < 400;
                    if (scrollY > oldScrollY && nearBottom) {
                        viewModel.loadMore();
                    }
                });

        viewModel.getProducts().observe(getViewLifecycleOwner(), this::render);
        viewModel.getCategories().observe(getViewLifecycleOwner(),
                list -> categoryAdapter.submit(list, viewModel.getCategoryId()));
        viewModel.getLoadingMore().observe(getViewLifecycleOwner(),
                loading -> binding.loadMoreProgress.setVisibility(loading ? View.VISIBLE : View.GONE));
    }

    private void setupLists() {
        productAdapter = new ProductAdapter(this::openDetail);
        binding.productList.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.productList.setAdapter(productAdapter);

        categoryAdapter = new CategoryAdapter(viewModel::setCategory);
        binding.categoryList.setLayoutManager(
                new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        binding.categoryList.setAdapter(categoryAdapter);
    }

    private void setupHeader() {
        binding.searchHeader.setOnQueryChangeListener(viewModel::setKeyword);
        binding.searchHeader.setOnFilterClickListener(v -> showSortMenu());
        binding.cartButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.cartFragment));
        binding.banner.bannerButton.setOnClickListener(v ->
                binding.scroll.smoothScrollTo(0, binding.sectionTitle.getTop()));
    }

    private void render(Resource<List<Product>> resource) {
        switch (resource.status) {
            case LOADING:
                binding.productList.setVisibility(View.GONE);
                if (!binding.swipeRefresh.isRefreshing()) {
                    binding.stateView.showLoading();
                }
                break;
            case SUCCESS:
                binding.swipeRefresh.setRefreshing(false);
                List<Product> data = resource.data;
                if (data == null || data.isEmpty()) {
                    binding.productList.setVisibility(View.GONE);
                    binding.stateView.showEmpty(getString(R.string.empty_products));
                } else {
                    binding.stateView.hide();
                    binding.productList.setVisibility(View.VISIBLE);
                    productAdapter.submitList(data);
                }
                break;
            case ERROR:
                binding.swipeRefresh.setRefreshing(false);
                binding.productList.setVisibility(View.GONE);
                binding.stateView.showError(resource.message, v -> viewModel.refresh());
                break;
        }
    }

    private void showSortMenu() {
        PopupMenu popup = new PopupMenu(requireContext(), binding.searchHeader.getFilterButton());
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
            return true;
        });
        popup.show();
    }

    private void openDetail(Product product) {
        Bundle args = new Bundle();
        args.putString(Constants.ARG_PRODUCT_ID, product.getId());
        NavHostFragment.findNavController(this).navigate(R.id.action_home_to_detail, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
