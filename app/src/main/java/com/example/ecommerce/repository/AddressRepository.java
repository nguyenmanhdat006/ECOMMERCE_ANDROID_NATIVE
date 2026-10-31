package com.example.ecommerce.repository;

import com.example.ecommerce.model.Address;
import com.example.ecommerce.service.ApiService;
import com.example.ecommerce.service.RetrofitClient;
import com.example.ecommerce.util.Constants;

import java.util.List;

/** Địa chỉ giao hàng của người đang đăng nhập. */
public class AddressRepository extends BaseRepository {

    private final ApiService api = Constants.USE_MOCK_DATA ? null : RetrofitClient.getApiService();

    public void getAddresses(RepositoryCallback<List<Address>> callback) {
        if (Constants.USE_MOCK_DATA) {
            MockDataSource.success(callback, MockStore.getAddresses());
            return;
        }
        enqueue(api.getMyAddresses(), callback);
    }

    /** Địa chỉ chưa có id thì thêm mới, đã có id thì cập nhật. */
    public void save(Address address, RepositoryCallback<Address> callback) {
        if (Constants.USE_MOCK_DATA) {
            MockDataSource.success(callback, MockStore.saveAddress(address));
            return;
        }
        if (address.id == null) {
            enqueue(api.createAddress(address), callback);
        } else {
            enqueue(api.updateAddress(address.id, address), callback);
        }
    }

    public void delete(String addressId, RepositoryCallback<Void> callback) {
        if (Constants.USE_MOCK_DATA) {
            MockStore.deleteAddress(addressId);
            MockDataSource.success(callback, null);
            return;
        }
        enqueue(api.deleteAddress(addressId), callback);
    }
}
