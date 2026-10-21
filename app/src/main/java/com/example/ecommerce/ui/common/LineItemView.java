package com.example.ecommerce.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.ecommerce.R;
import com.example.ecommerce.util.ImageUrls;

/**
 * C8 - Dòng sản phẩm: ảnh nhỏ, tên, phân loại, giá. Có một ô bên phải (getEndContainer) để
 * gắn thêm QuantityStepper, nút xóa, số lượng... tùy màn hình (Giỏ hàng, Thanh toán, Chi tiết đơn...).
 */
public class LineItemView extends LinearLayout {

    private final ImageView image;
    private final TextView name;
    private final TextView variant;
    private final TextView price;
    private final FrameLayout endContainer;

    public LineItemView(Context context) {
        this(context, null);
    }

    public LineItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_line_item, this, true);
        image = findViewById(R.id.line_image);
        name = findViewById(R.id.line_name);
        variant = findViewById(R.id.line_variant);
        price = findViewById(R.id.line_price);
        endContainer = findViewById(R.id.line_end);
    }

    public void bind(@Nullable String imageUrl, String nameText, @Nullable String variantText, String priceText) {
        Glide.with(image)
                .load(ImageUrls.fix(imageUrl))
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_image)
                .into(image);
        name.setText(nameText);
        variant.setText(variantText);
        variant.setVisibility(variantText == null || variantText.isEmpty() ? GONE : VISIBLE);
        price.setText(priceText);
    }

    public FrameLayout getEndContainer() {
        return endContainer;
    }
}
