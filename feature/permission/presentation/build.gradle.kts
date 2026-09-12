plugins {
    alias(libs.plugins.vibeplayer.android.feature)
}

android {
    namespace = "com.rfcoding.vibeplayer.feature.permission.presentation"
}

dependencies {
    // rememberLauncherForActivityResult and LocalActivity, which the feature plugin doesn't include.
    implementation(libs.androidx.activity.compose)
}
