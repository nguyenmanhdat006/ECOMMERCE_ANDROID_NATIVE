package com.example.ecommerce.repository;

/** Callback trả kết quả từ repository về ViewModel. */
public interface RepositoryCallback<T> {
    void onSuccess(T data);

    void onError(String message);
}
