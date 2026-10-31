package com.example.ecommerce.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.ecommerce.R;

/**
 * Lời nhắc "Đăng nhập để tiếp tục" cho các màn hình cần tài khoản (Giỏ hàng, Đơn hàng, Tài khoản)
 * khi người dùng chưa đăng nhập. Gọi setOnLoginClickListener() để xử lý nút Đăng nhập.
 */
public class LoginPromptView extends LinearLayout {

    private final TextView message;
    private final View loginButton;

    public LoginPromptView(Context context) {
        this(context, null);
    }

    public LoginPromptView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        LayoutInflater.from(context).inflate(R.layout.view_login_prompt, this, true);
        message = findViewById(R.id.prompt_message);
        loginButton = findViewById(R.id.prompt_button);
    }

    public void setMessage(String text) {
        message.setText(text);
    }

    public void setOnLoginClickListener(@Nullable OnClickListener listener) {
        loginButton.setOnClickListener(listener);
    }
}
