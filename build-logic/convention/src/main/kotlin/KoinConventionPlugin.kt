import com.rfcoding.vibeplayer.convention.libs
import com.rfcoding.vibeplayer.convention.library
import com.rfcoding.vibeplayer.convention.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Koin for Android modules. Compose modules also get `koin-androidx-compose` (`koinViewModel()`).
 */
class KoinConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            dependencies {
                "implementation"(platform(libs.library("koin-bom")))
                "implementation"(libs.library("koin-android"))
            }

            pluginManager.withPlugin(libs.pluginId("kotlin-compose")) {
                dependencies {
                    "implementation"(libs.library("koin-androidx-compose"))
                }
            }
        }
    }
}
