package com.example.ecommerce.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.example.ecommerce.R;

/**
 * Thành phần hiển thị các trạng thái màn hình (Đang tải, Trống, Lỗi mạng).
 */
public class StateView extends FrameLayout {

    private final View progress;
    private final View messageGroup;
    private final ImageView icon;
    private final TextView message;
    private final TextView retryButton;

    public StateView(Context context) {
        this(context, null);
    }

    public StateView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_state, this, true);
        progress = findViewById(R.id.state_progress);
        messageGroup = findViewById(R.id.state_message_group);
        icon = findViewById(R.id.state_icon);
        message = findViewById(R.id.state_message);
        retryButton = findViewById(R.id.state_retry);
        hide();
    }

    public void showLoading() {
        setVisibility(VISIBLE);
        progress.setVisibility(VISIBLE);
        messageGroup.setVisibility(GONE);
    }

    public void showEmpty(String text) {
        show(text, R.drawable.ic_search, null);
    }

    public void showEmpty(String text, int iconRes) {
        show(text, iconRes, null);
    }

    public void showError(String text, @Nullable OnClickListener onRetry) {
        retryButton.setText(R.string.retry);
        show(text, R.drawable.ic_error, onRetry);
    }

    public void showAction(String text, @DrawableRes int iconRes, String actionText, OnClickListener onAction) {
        retryButton.setText(actionText);
        show(text, iconRes, onAction);
    }

    public void hide() {
        setVisibility(GONE);
    }

    private void show(String text, int iconRes, @Nullable OnClickListener onRetry) {
        setVisibility(VISIBLE);
        progress.setVisibility(GONE);
        messageGroup.setVisibility(VISIBLE);
        icon.setImageResource(iconRes);
        message.setText(text);
        retryButton.setVisibility(onRetry != null ? VISIBLE : GONE);
        retryButton.setOnClickListener(onRetry);
    }
}
