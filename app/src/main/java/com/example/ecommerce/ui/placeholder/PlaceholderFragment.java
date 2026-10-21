package com.example.ecommerce.ui.placeholder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ecommerce.R;

/**
 * Màn hình tạm cho các tab chưa làm (Giỏ hàng, Đơn hàng, Tài khoản): chỉ hiện tên tab.
 * Khi làm màn thật, đổi thuộc tính android:name của tab đó trong navigation/nav_graph.xml.
 */
public class PlaceholderFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        NavDestination destination = NavHostFragment.findNavController(this).getCurrentDestination();
        CharSequence label = destination != null ? destination.getLabel() : null;
        ((TextView) view.findViewById(R.id.placeholder_title)).setText(label);
    }
}
