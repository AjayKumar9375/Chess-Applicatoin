plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseStorePath = providers.gradleProperty("CHESS_RELEASE_STORE_FILE").orNull
val hasReleaseSigning = !releaseStorePath.isNullOrBlank()

android {
    namespace = "com.pramod.chessmasteroffline"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pramod.chessmasteroffline"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(releaseStorePath!!)
                storePassword = providers.gradleProperty("CHESS_RELEASE_STORE_PASSWORD").orNull
                keyAlias = providers.gradleProperty("CHESS_RELEASE_KEY_ALIAS").orNull
                keyPassword = providers.gradleProperty("CHESS_RELEASE_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        checkTestSources = false
        htmlReport = false
        sarifReport = false
        textReport = false
        xmlReport = false
        disable.addAll(listOf(
            "AccidentalOctal",
            "AndroidGradlePluginVersion",
            "AnnotationProcessorOnCompilePath",
            "BomWithoutPlatform",
            "ChromeOsAbiSupport",
            "CoreLibDesugaringV1",
            "DataBindingWithoutKapt",
            "DevModeObsolete",
            "DuplicatePlatformClasses",
            "EditedTargetSdkVersion",
            "ExpiredTargetSdkVersion",
            "ExpiringTargetSdkVersion",
            "GradleCompatible",
            "GradleDeprecated",
            "GradleDeprecatedConfiguration",
            "GradleDependency",
            "GradleDynamicVersion",
            "GradleGetter",
            "GradleIdeError",
            "GradlePath",
            "GradlePluginVersion",
            "HighAppVersionCode",
            "InstantAppDeprecation",
            "JavaPluginLanguageLevel",
            "JcenterRepositoryObsolete",
            "KaptUsageInsteadOfKsp",
            "KtxExtensionAvailable",
            "LifecycleAnnotationProcessorWithJava8",
            "MinSdkTooLow",
            "NewerVersionAvailable",
            "NotInterpolated",
            "OldTargetApi",
            "OutdatedLibrary",
            "PlaySdkIndexDeprecated",
            "PlaySdkIndexGenericIssues",
            "PlaySdkIndexNonCompliant",
            "PlaySdkIndexVulnerability",
            "R8GradualApi",
            "RiskyLibrary",
            "SimilarGradleDependency",
            "StringShouldBeInt",
            "UseOfBundledGooglePlayServices",
            "UseTomlInstead",
        ))
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.05.00"))

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
}
