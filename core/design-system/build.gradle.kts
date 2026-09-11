plugins {
    alias(libs.plugins.vibeplayer.android.library)
    alias(libs.plugins.vibeplayer.compose)
}

android {
    namespace = "com.rfcoding.vibeplayer.core.designsystem"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
}
