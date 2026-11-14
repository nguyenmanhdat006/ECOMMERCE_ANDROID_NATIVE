package com.example.ecommerce.ui.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.example.ecommerce.R;
import com.google.android.material.button.MaterialButton;

/**
 * Nút bấm hành động tích hợp trạng thái chờ tiến trình (ProgressBar).
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

<<<<<<< HEAD
        int[] attrsText = {android.R.attr.text};
        android.content.res.TypedArray a = context.obtainStyledAttributes(attrs, attrsText);
=======
        // Cho phép đặt chữ ngay trong XML bằng android:text
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LoadingButton);
>>>>>>> 20ba9a7cedd951ceef69aca31d50c2a189689380
        try {
            CharSequence t = a.getText(R.styleable.LoadingButton_android_text);
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
