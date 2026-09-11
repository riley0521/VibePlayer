import com.android.build.api.dsl.ApplicationExtension
import com.rfcoding.vibeplayer.convention.configureKotlinAndroid
import com.rfcoding.vibeplayer.convention.configureUnitTesting
import com.rfcoding.vibeplayer.convention.libs
import com.rfcoding.vibeplayer.convention.pluginId
import com.rfcoding.vibeplayer.convention.version
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("android-application"))

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    applicationId = libs.version("projectApplicationId")
                    targetSdk = libs.version("projectTargetSdk").toInt()
                    versionCode = libs.version("projectVersionCode").toInt()
                    versionName = libs.version("projectVersionName")
                }

                configureKotlinAndroid(this)
            }

            configureUnitTesting()
        }
    }
}
