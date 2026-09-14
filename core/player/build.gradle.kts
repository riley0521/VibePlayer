plugins {
    alias(libs.plugins.vibeplayer.android.library)
    alias(libs.plugins.vibeplayer.koin)
}

android {
    namespace = "com.rfcoding.vibeplayer.core.player"
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.kotlinx.coroutines.core)
}
