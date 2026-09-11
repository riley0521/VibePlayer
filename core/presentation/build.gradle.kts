plugins {
    alias(libs.plugins.vibeplayer.android.library)
    alias(libs.plugins.vibeplayer.compose)
    alias(libs.plugins.vibeplayer.koin)
}

android {
    namespace = "com.rfcoding.vibeplayer.core.presentation"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.designSystem)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.material3.adaptive)
}
