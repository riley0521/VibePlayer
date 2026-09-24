plugins {
    alias(libs.plugins.vibeplayer.android.feature)
}

android {
    namespace = "com.rfcoding.vibeplayer.feature.downloader.presentation"
}

dependencies {
    implementation(projects.feature.downloader.domain)

    // Thumbnails of the search results are remote.
    implementation(libs.coil.network.okhttp)
    // rememberLauncherForActivityResult, for the notification and API 28 storage permissions.
    implementation(libs.androidx.activity.compose)

    testImplementation(projects.core.testing)
}
