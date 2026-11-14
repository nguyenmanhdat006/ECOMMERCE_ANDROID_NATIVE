package com.example.ecommerce.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.example.ecommerce.R;
import com.google.android.material.button.MaterialButton;

/**
 * C10 - Thanh hành động cố định ở đáy màn hình: bên trái là nhãn + giá, bên phải là nút chính
 * (và tuỳ chọn một nút phụ). Dùng cho Chi tiết sản phẩm ("Mua ngay"), Giỏ hàng, Thanh toán.
 */
public class BottomActionBar extends LinearLayout {

    private final View info;
    private final TextView label;
    private final TextView price;
    private final TextView primary;
    private final MaterialButton secondary;

    public BottomActionBar(Context context) {
        this(context, null);
    }

    public BottomActionBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundResource(R.drawable.bg_bottom_bar);
        float d = context.getResources().getDisplayMetrics().density;
        setPadding((int) (20 * d), (int) (14 * d), (int) (20 * d), (int) (14 * d));
        setElevation(8 * d);
        LayoutInflater.from(context).inflate(R.layout.view_bottom_action_bar, this, true);
        info = findViewById(R.id.bar_info);
        label = findViewById(R.id.bar_label);
        price = findViewById(R.id.bar_price);
        primary = findViewById(R.id.bar_primary);
        secondary = findViewById(R.id.bar_secondary);
    }

    public void setLabel(CharSequence text) {
        label.setText(text);
    }

    public void setPrice(CharSequence text) {
        price.setText(text);
    }

    public void setPrimaryButton(CharSequence text, @Nullable OnClickListener listener) {
        primary.setText(text);
        primary.setOnClickListener(listener);
    }

    public void setPrimaryEnabled(boolean enabled) {
        primary.setEnabled(enabled);
        primary.setAlpha(enabled ? 1f : 0.5f);
    }

    /** Truyền text null để ẩn nút phụ. */
    public void setSecondaryButton(@Nullable CharSequence text, @Nullable OnClickListener listener) {
        if (text == null) {
            secondary.setVisibility(View.GONE);
            return;
        }
        secondary.setIcon(null);
        secondary.setText(text);
        secondary.setOnClickListener(listener);
        secondary.setVisibility(View.VISIBLE);
    }

    /** Nút phụ dạng chỉ có biểu tượng (ví dụ giỏ hàng) để tiết kiệm chỗ trên màn hình hẹp. */
    public void setSecondaryIconButton(@DrawableRes int icon, @Nullable CharSequence description,
                                       @Nullable OnClickListener listener) {
        secondary.setText("");
        secondary.setIconResource(icon);
        secondary.setIconPadding(0);
        secondary.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        secondary.setContentDescription(description);
        secondary.setOnClickListener(listener);
        secondary.setVisibility(View.VISIBLE);
    }

    /** Nút phụ có cả biểu tượng lẫn chữ (ví dụ giỏ hàng + "Thêm vào giỏ"). */
    public void setSecondaryButton(CharSequence text, @DrawableRes int icon, @Nullable OnClickListener listener) {
        secondary.setText(text);
        secondary.setIconResource(icon);
        secondary.setIconPadding((int) (8 * getResources().getDisplayMetrics().density));
        secondary.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
        secondary.setOnClickListener(listener);
        secondary.setVisibility(View.VISIBLE);
    }

    public void setSecondaryEnabled(boolean enabled) {
        secondary.setEnabled(enabled);
        secondary.setAlpha(enabled ? 1f : 0.5f);
    }

    /**
     * true: ẩn nhãn + giá bên trái, hai nút chia đều chiều ngang (Chi tiết sản phẩm).
     * false: bố cục mặc định (nhãn + giá bên trái, nút bên phải).
     */
    public void setButtonsOnly(boolean buttonsOnly) {
        info.setVisibility(buttonsOnly ? View.GONE : View.VISIBLE);
        for (View button : new View[]{secondary, primary}) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) button.getLayoutParams();
            params.width = buttonsOnly ? 0 : LayoutParams.WRAP_CONTENT;
            params.weight = buttonsOnly ? 1f : 0f;
            button.setLayoutParams(params);
        }
    }
}