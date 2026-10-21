package com.example.ecommerce.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.ecommerce.R;

/**
 * C3 (một phần) - Nút giỏ hàng có chấm số lượng. Số lượng bằng 0 thì ẩn chấm.
 */
public class CartButton extends FrameLayout {

    private final TextView badge;
    private final android.view.View icon;

    public CartButton(Context context) {
        this(context, null);
    }

    public CartButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_cart_button, this, true);
        badge = findViewById(R.id.cart_badge);
        icon = findViewById(R.id.cart_icon);
    }

    public void setCount(int count) {
        if (count <= 0) {
            badge.setVisibility(GONE);
            return;
        }
        badge.setText(count > 99 ? "99+" : String.valueOf(count));
        badge.setVisibility(VISIBLE);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        icon.setOnClickListener(listener);
    }
}
