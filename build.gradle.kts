// Top-level build file: khai báo plugin dùng chung, áp dụng ở module app
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
