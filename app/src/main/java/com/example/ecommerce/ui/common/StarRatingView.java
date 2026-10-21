package com.example.ecommerce.ui.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.ecommerce.R;

/**
 * C12 - Thanh 5 sao. Mặc định chỉ hiển thị; gọi setEditable(true, listener) để người dùng chọn 1-5 sao
 * (màn Viết đánh giá).
 */
public class StarRatingView extends LinearLayout {

    public interface OnRatingChangeListener {
        void onRatingChanged(int rating);
    }

    private static final int STAR_COUNT = 5;

    private final ImageView[] stars = new ImageView[STAR_COUNT];
    private final ColorStateList filled;
    private final ColorStateList empty;
    private int rating;
    private OnRatingChangeListener listener;

    public StarRatingView(Context context) {
        this(context, null);
    }

    public StarRatingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        filled = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.star));
        empty = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.stroke));
        float d = context.getResources().getDisplayMetrics().density;
        int size = (int) (14 * d);
        for (int i = 0; i < STAR_COUNT; i++) {
            ImageView star = new ImageView(context);
            star.setImageResource(R.drawable.ic_star);
            star.setLayoutParams(new LayoutParams(size, size));
            star.setContentDescription((i + 1) + "/5");
            final int value = i + 1;
            star.setOnClickListener(v -> {
                if (listener != null) {
                    setRating(value);
                    listener.onRatingChanged(value);
                }
            });
            star.setClickable(false);
            stars[i] = star;
            addView(star);
        }
        setRating(0);
    }

    /** Làm tròn về số sao nguyên gần nhất (4.6 -> 5 sao). */
    public void setRating(float value) {
        rating = Math.max(0, Math.min(STAR_COUNT, Math.round(value)));
        for (int i = 0; i < STAR_COUNT; i++) {
            ImageViewCompat.setImageTintList(stars[i], i < rating ? filled : empty);
        }
    }

    public int getRating() {
        return rating;
    }

    /** Đổi kích thước mỗi sao (đơn vị dp). */
    public void setStarSize(int dp) {
        int px = (int) (dp * getResources().getDisplayMetrics().density);
        for (ImageView star : stars) {
            star.getLayoutParams().width = px;
            star.getLayoutParams().height = px;
            star.requestLayout();
        }
    }

    public void setEditable(boolean editable, @Nullable OnRatingChangeListener listener) {
        this.listener = editable ? listener : null;
        for (ImageView star : stars) {
            star.setClickable(editable);
        }
    }
}
