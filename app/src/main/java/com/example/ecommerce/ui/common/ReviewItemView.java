package com.example.ecommerce.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.ecommerce.R;

import java.util.Locale;

/**
 * C12 - Một dòng đánh giá: ảnh đại diện (chữ cái đầu), tên, số sao, ngày, nhận xét.
 */
public class ReviewItemView extends LinearLayout {

    private final TextView avatar;
    private final TextView name;
    private final TextView date;
    private final TextView comment;
    private final StarRatingView stars;

    public ReviewItemView(Context context) {
        this(context, null);
    }

    public ReviewItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_review_item, this, true);
        avatar = findViewById(R.id.review_avatar);
        name = findViewById(R.id.review_name);
        date = findViewById(R.id.review_date);
        comment = findViewById(R.id.review_comment);
        stars = findViewById(R.id.review_stars);
    }

    public void bind(String userName, int rating, @Nullable String commentText, @Nullable String dateText) {
        String safeName = userName == null || userName.trim().isEmpty() ? "?" : userName.trim();
        avatar.setText(safeName.substring(0, 1).toUpperCase(Locale.getDefault()));
        name.setText(safeName);
        stars.setRating(rating);
        date.setText(dateText);
        comment.setText(commentText);
        comment.setVisibility(commentText == null || commentText.isEmpty() ? GONE : VISIBLE);
    }
}
