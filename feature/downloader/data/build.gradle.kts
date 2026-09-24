plugins {
    alias(libs.plugins.vibeplayer.android.library)
    alias(libs.plugins.vibeplayer.koin)
    alias(libs.plugins.vibeplayer.kotlinx.serialization)
}

android {
    namespace = "com.rfcoding.vibeplayer.feature.downloader.data"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(projects.feature.downloader.domain)
    implementation(projects.core.domain)
    implementation(projects.core.data)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.koin.androidx.workmanager)
    implementation(libs.youtubedl.android.library)
    implementation(libs.youtubedl.android.ffmpeg)

    testImplementation(projects.core.testing)
}
