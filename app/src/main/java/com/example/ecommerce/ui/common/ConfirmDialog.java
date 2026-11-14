package com.example.ecommerce.ui.common;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.ecommerce.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Tiện ích hiển thị hộp thoại cảnh báo xác nhận hành động.
 */
public final class ConfirmDialog {

    private ConfirmDialog() {
    }

    /** Hiển thị Dialog xác nhận thao tác người dùng. */
    public static void show(Context context, String title, String message, String confirmText,
                            boolean danger, Runnable onConfirm) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(confirmText, (d, which) -> onConfirm.run())
                .create();
        dialog.setOnShowListener(d -> {
            int color = ContextCompat.getColor(context, danger ? R.color.danger : R.color.primary);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        });
        dialog.show();
    }
}
