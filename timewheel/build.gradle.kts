import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "com.ozansan.timewheel.lib"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.core)
    implementation(libs.androidx.compose.ui.tooling.preview.core)
    debugImplementation(libs.androidx.compose.ui.tooling.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()

    coordinates(
        groupId = "com.ozansan",
        artifactId = "timewheel",
        version = providers.gradleProperty("VERSION_NAME").getOrElse("0.1.0-SNAPSHOT"),
    )

    pom {
        name.set("TimeWheel")
        description.set("An iOS-style scrolling time picker for Jetpack Compose.")
        url.set("https://github.com/ozan-san/TimeWheel")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("ozan-san")
                name.set("Ozan Şan")
                url.set("https://github.com/ozan-san")
            }
        }
        scm {
            url.set("https://github.com/ozan-san/TimeWheel")
            connection.set("scm:git:git://github.com/ozan-san/TimeWheel.git")
            developerConnection.set("scm:git:ssh://git@github.com/ozan-san/TimeWheel.git")
        }
    }
}