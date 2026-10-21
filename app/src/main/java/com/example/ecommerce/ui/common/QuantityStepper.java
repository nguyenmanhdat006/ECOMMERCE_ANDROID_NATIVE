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
 * C9 - Bộ chỉnh số lượng "- 1 +". Giá trị luôn nằm trong [min, max]; mặc định min = 1, max = 99.
 * setQuantity()/setMax() không gọi listener; chỉ khi người dùng bấm nút mới gọi.
 */
public class QuantityStepper extends LinearLayout {

    public interface OnQuantityChangeListener {
        void onQuantityChanged(int quantity);
    }

    private final TextView valueView;
    private final View minusButton;
    private final View plusButton;
    private int min = 1;
    private int max = 99;
    private int value = 1;
    private OnQuantityChangeListener listener;

    public QuantityStepper(Context context) {
        this(context, null);
    }

    public QuantityStepper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_quantity_stepper, this, true);
        valueView = findViewById(R.id.stepper_value);
        minusButton = findViewById(R.id.stepper_minus);
        plusButton = findViewById(R.id.stepper_plus);

        minusButton.setOnClickListener(v -> change(value - 1));
        plusButton.setOnClickListener(v -> change(value + 1));
        render();
    }

    public int getQuantity() {
        return value;
    }

    public void setQuantity(int quantity) {
        value = Math.max(min, Math.min(quantity, max));
        render();
    }

    public void setMax(int max) {
        this.max = Math.max(min, max);
        value = Math.min(value, this.max);
        render();
    }

    public void setOnQuantityChangeListener(@Nullable OnQuantityChangeListener listener) {
        this.listener = listener;
    }

    private void change(int newValue) {
        int clamped = Math.max(min, Math.min(newValue, max));
        if (clamped == value) {
            return;
        }
        value = clamped;
        render();
        if (listener != null) {
            listener.onQuantityChanged(value);
        }
    }

    private void render() {
        valueView.setText(String.valueOf(value));
        minusButton.setEnabled(value > min);
        minusButton.setAlpha(value > min ? 1f : 0.4f);
        plusButton.setEnabled(value < max);
        plusButton.setAlpha(value < max ? 1f : 0.4f);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            render();
        } else {
            minusButton.setEnabled(false);
            plusButton.setEnabled(false);
            setAlpha(0.5f);
            return;
        }
        setAlpha(1f);
    }
}
