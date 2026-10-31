package com.example.ecommerce.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecommerce.model.Category;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.RepositoryCallback;
import com.example.ecommerce.util.Resource;

import java.util.List;

/** Danh sách danh mục cho màn Danh mục. */
public class CategoryViewModel extends ViewModel {

    private final ProductRepository repository = new ProductRepository();
    private final MutableLiveData<Resource<List<Category>>> categories = new MutableLiveData<>();

    public LiveData<Resource<List<Category>>> getCategories() { return categories; }

    public void load() {
        categories.setValue(Resource.<List<Category>>loading());
        repository.getCategories(new RepositoryCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                categories.setValue(Resource.success(data));
            }

            @Override
            public void onError(String message) {
                categories.setValue(Resource.<List<Category>>error(message));
            }
        });
    }
}
