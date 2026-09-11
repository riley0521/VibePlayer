plugins {
    alias(libs.plugins.vibeplayer.android.library)
    alias(libs.plugins.vibeplayer.koin)
}

android {
    namespace = "com.rfcoding.vibeplayer.feature.library.data"
}

dependencies {
    implementation(projects.feature.library.domain)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.database)
}
