package com.example.ecommerce.ui.common;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.annotation.ColorRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.example.ecommerce.R;

/**
 * C13 - Nhãn trạng thái đơn hàng. Truyền mã trạng thái của backend:
 * PENDING, SHIPPING, WAIT_DELIVER, PAID, CANCELED, REFUND.
 */
public class OrderStatusView extends AppCompatTextView {

    public OrderStatusView(Context context) {
        this(context, null);
    }

    public OrderStatusView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        setTypeface(getTypeface(), android.graphics.Typeface.BOLD);
        float d = context.getResources().getDisplayMetrics().density;
        setPadding((int) (10 * d), (int) (3 * d), (int) (10 * d), (int) (3 * d));
    }

    public void setStatus(@Nullable String code) {
        int textRes;
        @ColorRes int fg;
        @ColorRes int bg;
        String c = code == null ? "" : code;
        switch (c) {
            case "PENDING":
                textRes = R.string.status_pending;
                fg = R.color.warning;
                bg = R.color.warning_bg;
                break;
            case "SHIPPING":
                textRes = R.string.status_shipping;
                fg = R.color.primary;
                bg = R.color.primary_light;
                break;
            case "WAIT_DELIVER":
                textRes = R.string.status_wait_deliver;
                fg = R.color.primary;
                bg = R.color.primary_light;
                break;
            case "PAID":
                textRes = R.string.status_paid;
                fg = R.color.success;
                bg = R.color.success_bg;
                break;
            case "CANCELED":
                textRes = R.string.status_canceled;
                fg = R.color.danger;
                bg = R.color.danger_bg;
                break;
            case "REFUND":
                textRes = R.string.status_refund;
                fg = R.color.text_secondary;
                bg = R.color.gray_fill;
                break;
            default:
                setText(c);
                fg = R.color.text_secondary;
                bg = R.color.gray_fill;
                textRes = 0;
                break;
        }
        if (textRes != 0) {
            setText(textRes);
        }
        setTextColor(ContextCompat.getColor(getContext(), fg));
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(10 * getResources().getDisplayMetrics().density);
        background.setColor(ContextCompat.getColor(getContext(), bg));
        setBackground(background);
    }
}
