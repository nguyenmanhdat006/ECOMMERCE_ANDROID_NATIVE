package com.example.ecommerce.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.ecommerce.model.Address;
import com.example.ecommerce.repository.AddressRepository;
import com.example.ecommerce.repository.RepositoryCallback;
import com.example.ecommerce.util.Resource;

import java.util.List;

/**
 * Quản lý trạng thái và thao tác xem, thêm, chỉnh sửa, xóa địa chỉ giao hàng.
 */
public class AddressViewModel extends ViewModel {

    private final AddressRepository repository = new AddressRepository();

    private final MutableLiveData<Resource<List<Address>>> addresses = new MutableLiveData<>();
    private final MutableLiveData<Resource<Address>> saveResult = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public LiveData<Resource<List<Address>>> getAddresses() { return addresses; }

    public LiveData<Resource<Address>> getSaveResult() { return saveResult; }

    public LiveData<String> getMessage() { return message; }

    public void clearSaveResult() { saveResult.setValue(null); }

    public void clearMessage() { message.setValue(null); }

    public void load() {
        addresses.setValue(Resource.<List<Address>>loading());
        fetch();
    }

    /** Lưu thông tin địa chỉ giao nhận (Thêm mới hoặc Cập nhật). */
    public void save(Address address) {
        saveResult.setValue(Resource.<Address>loading());
        repository.save(address, new RepositoryCallback<Address>() {
            @Override
            public void onSuccess(Address data) {
                saveResult.setValue(Resource.success(data));
            }

            @Override
            public void onError(String errorMessage) {
                saveResult.setValue(Resource.<Address>error(errorMessage));
            }
        });
    }

    public void delete(Address address) {
        repository.delete(address.id, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                fetch();
            }

            @Override
            public void onError(String errorMessage) {
                message.setValue(errorMessage);
            }
        });
    }

    private void fetch() {
        repository.getAddresses(new RepositoryCallback<List<Address>>() {
            @Override
            public void onSuccess(List<Address> data) {
                addresses.setValue(Resource.success(data));
            }

            @Override
            public void onError(String errorMessage) {
                addresses.setValue(Resource.<List<Address>>error(errorMessage));
            }
        });
    }
}
