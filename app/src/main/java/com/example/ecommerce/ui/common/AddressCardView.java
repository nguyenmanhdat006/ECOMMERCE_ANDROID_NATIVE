package com.example.ecommerce.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.ecommerce.R;

/**
 * Thành phần thẻ hiển thị chi tiết thông tin địa chỉ giao nhận.
 */
public class AddressCardView extends LinearLayout {

    private final TextView name;
    private final TextView phone;
    private final TextView line;
    private final View selectedBadge;

    public AddressCardView(Context context) {
        this(context, null);
    }

    public AddressCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setBackgroundResource(R.drawable.bg_card);
        int padPx = (int) (14 * context.getResources().getDisplayMetrics().density);
        setPadding(padPx, padPx, padPx, padPx);
        LayoutInflater.from(context).inflate(R.layout.view_address_card, this, true);
        name = findViewById(R.id.address_name);
        phone = findViewById(R.id.address_phone);
        line = findViewById(R.id.address_line);
        selectedBadge = findViewById(R.id.address_selected);
    }

    public void bind(String nameText, String phoneText, String addressLine, boolean selected) {
        name.setText(nameText);
        phone.setText(phoneText);
        line.setText(addressLine);
        selectedBadge.setVisibility(selected ? VISIBLE : GONE);
    }
}
