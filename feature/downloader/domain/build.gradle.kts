plugins {
    alias(libs.plugins.vibeplayer.domain.module)
}

dependencies {
    implementation(projects.core.domain)

    testImplementation(projects.core.testing)
}
