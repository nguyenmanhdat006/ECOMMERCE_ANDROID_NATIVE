package com.example.ecommerce.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.example.ecommerce.R;
import com.google.android.material.button.MaterialButton;

/**
 * C6 - Nút chính có trạng thái đang xử lý: setLoading(true) hiện vòng quay và khóa nút
 * (dùng cho Đăng nhập, Đặt hàng, Lưu...).
 */
public class LoadingButton extends FrameLayout {

    private final MaterialButton button;
    private final View progress;
    private CharSequence text = "";
    private boolean loading;

    public LoadingButton(Context context) {
        this(context, null);
    }

    public LoadingButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_loading_button, this, true);
        button = findViewById(R.id.loading_button);
        progress = findViewById(R.id.loading_progress);

        // Cho phép đặt chữ ngay trong XML bằng android:text
        int[] attrsText = {android.R.attr.text};
        android.content.res.TypedArray a = context.obtainStyledAttributes(attrs, attrsText);
        try {
            CharSequence t = a.getText(0);
            if (t != null) {
                setText(t);
            }
        } finally {
            a.recycle();
        }
    }

    public void setText(CharSequence text) {
        this.text = text;
        if (!loading) {
            button.setText(text);
        }
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
        button.setEnabled(!loading);
        button.setText(loading ? "" : text);
        progress.setVisibility(loading ? VISIBLE : GONE);
    }

    public boolean isLoading() {
        return loading;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        button.setEnabled(enabled && !loading);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener listener) {
        button.setOnClickListener(listener);
    }
}
