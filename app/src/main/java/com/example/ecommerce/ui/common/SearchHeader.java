package com.example.ecommerce.ui.common;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.ecommerce.R;

/**
 * C3 - Thanh tìm kiếm + nút bộ lọc. Gõ xong khoảng 400 ms thì báo từ khóa mới (debounce),
 * bấm phím tìm kiếm trên bàn phím thì báo ngay.
 */
public class SearchHeader extends LinearLayout {

    public interface OnQueryChangeListener {
        void onQueryChanged(String query);
    }

    private static final long DEBOUNCE_MS = 400L;

    private final EditText input;
    private final ImageButton clearButton;
    private final ImageButton filterButton;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable notifyRunnable = this::notifyQuery;
    private OnQueryChangeListener listener;

    public SearchHeader(Context context) {
        this(context, null);
    }

    public SearchHeader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.view_search_header, this, true);
        input = findViewById(R.id.search_input);
        clearButton = findViewById(R.id.search_clear);
        filterButton = findViewById(R.id.search_filter);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                clearButton.setVisibility(s.length() > 0 ? VISIBLE : GONE);
                handler.removeCallbacks(notifyRunnable);
                handler.postDelayed(notifyRunnable, DEBOUNCE_MS);
            }
        });
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                handler.removeCallbacks(notifyRunnable);
                notifyQuery();
                return true;
            }
            return false;
        });
        clearButton.setOnClickListener(v -> input.setText(""));
    }

    public String getQuery() {
        return input.getText().toString().trim();
    }

    public void setOnQueryChangeListener(@Nullable OnQueryChangeListener listener) {
        this.listener = listener;
    }

    public void setOnFilterClickListener(@Nullable OnClickListener listener) {
        filterButton.setOnClickListener(listener);
    }

    /** Điểm neo để hiển thị PopupMenu sắp xếp. */
    public View getFilterButton() {
        return filterButton;
    }

    private void notifyQuery() {
        if (listener != null) {
            listener.onQueryChanged(getQuery());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        handler.removeCallbacks(notifyRunnable);
        super.onDetachedFromWindow();
    }
}
