package com.example.ecommerce.ui.common;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.example.ecommerce.R;

/**
 * C2 - Header chuẩn: nút back, tiêu đề (có thể kèm phụ đề) và tối đa 2 nút hành động bên phải.
 *
 * Dùng trong XML:
 * <com.example.ecommerce.ui.common.AppToolbar app:title="Giỏ hàng" app:showBack="true" ... />
 * Mặc định nút back gọi nút Back của hệ thống; đổi bằng setOnBackClickListener().
 */
public class AppToolbar extends LinearLayout {

    private final ImageButton backButton;
    private final ImageButton action1;
    private final ImageButton action2;
    private final TextView titleView;
    private final TextView subtitleView;

    public AppToolbar(Context context) {
        this(context, null);
    }

    public AppToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_app_toolbar, this, true);

        backButton = findViewById(R.id.toolbar_back);
        action1 = findViewById(R.id.toolbar_action1);
        action2 = findViewById(R.id.toolbar_action2);
        titleView = findViewById(R.id.toolbar_title);
        subtitleView = findViewById(R.id.toolbar_subtitle);

        backButton.setOnClickListener(v -> goBack());

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AppToolbar);
        try {
            setTitle(a.getString(R.styleable.AppToolbar_title));
            setSubtitle(a.getString(R.styleable.AppToolbar_subtitle));
            setShowBack(a.getBoolean(R.styleable.AppToolbar_showBack, true));
        } finally {
            a.recycle();
        }
    }

    public void setTitle(@Nullable CharSequence title) {
        titleView.setText(title);
    }

    public void setSubtitle(@Nullable CharSequence subtitle) {
        boolean has = subtitle != null && subtitle.length() > 0;
        subtitleView.setText(subtitle);
        subtitleView.setVisibility(has ? VISIBLE : GONE);
    }

    public void setShowBack(boolean show) {
        backButton.setVisibility(show ? VISIBLE : GONE);
    }

    public void setOnBackClickListener(@Nullable OnClickListener listener) {
        backButton.setOnClickListener(listener != null ? listener : v -> goBack());
    }

    /** index 1 hoặc 2: nút hành động bên phải (1 nằm bên trái nút 2). */
    public void setAction(int index, @DrawableRes int icon, @Nullable String description,
                          @Nullable OnClickListener listener) {
        ImageButton button = index == 1 ? action1 : action2;
        button.setImageResource(icon);
        button.setContentDescription(description);
        button.setOnClickListener(listener);
        button.setVisibility(VISIBLE);
    }

    /** Đổi biểu tượng của nút hành động (ví dụ tim rỗng -> tim đầy). */
    public void setActionIcon(int index, @DrawableRes int icon) {
        (index == 1 ? action1 : action2).setImageResource(icon);
    }

    public void hideAction(int index) {
        (index == 1 ? action1 : action2).setVisibility(GONE);
    }

    private void goBack() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                break;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (context instanceof ComponentActivity) {
            ((ComponentActivity) context).getOnBackPressedDispatcher().onBackPressed();
        }
    }
}
