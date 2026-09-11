import com.rfcoding.vibeplayer.convention.libs
import com.rfcoding.vibeplayer.convention.library
import com.rfcoding.vibeplayer.convention.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Feature presentation module: Android library + Compose + Koin + serializable nav routes,
 * wired to the shared core modules every screen needs.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("vibeplayer-android-library"))
            pluginManager.apply(libs.pluginId("vibeplayer-compose"))
            pluginManager.apply(libs.pluginId("vibeplayer-koin"))
            pluginManager.apply(libs.pluginId("vibeplayer-kotlinx-serialization"))

            dependencies {
                "implementation"(project(":core:domain"))
                "implementation"(project(":core:presentation"))
                "implementation"(project(":core:design-system"))

                "implementation"(libs.library("androidx-navigation-compose"))
                "implementation"(libs.library("androidx-lifecycle-runtime-compose"))
                "implementation"(libs.library("androidx-lifecycle-viewmodel-compose"))
            }
        }
    }
}
