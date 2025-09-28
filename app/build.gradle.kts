import org.gradle.kotlin.dsl.androidTestImplementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    id("kotlin-kapt")
    id("jacoco")
    id("org.sonarqube") version "5.0.0.4638"
}

// Configure JaCoCo version
jacoco {
    toolVersion = "0.8.10"
}

// Configure JaCoCo for test tasks
tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

// --- JaCoCo Report Configuration ---

// Define common exclusion patterns for files you don't want in the coverage report
val jacocoExclusionPatterns = listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Test*.*", // Exclude test classes themselves
    "android/**/*.*",

    // Hilt generated code
    "**/di/module/*", // If you have modules in a specific 'di/module' package
    "**/*_HiltModules*.*",
    "**/*Module_Provide*Factory.*", // Hilt factories for @Provides methods
    "**/*_Factory.*",              // General Hilt factories
    "**/*_MembersInjector.*",
    "**/Dagger*Component.*",
    "**/Hilt_*.*",
    "**/*Activity_GeneratedInjector.*",
    "**/*Fragment_GeneratedInjector.*",
    "**/*View_GeneratedInjector.*",
    "**/*Service_GeneratedInjector.*",

    // Android generated classes (add more as needed)
    "**/*databinding/",      // Android Data Binding
    "**/*DataBinderMapperImpl*.*",
    "**/*BR*.*",
    "**/*Parcelable*.*", // Parcelable generated code

    // Specific classes to exclude (examples)
    "**/com/betsson/interviewtest/di/**",
    "**/com/betsson/interviewtest/presentation/features/oddsList/components/**",
    "**/com/betsson/interviewtest/presentation/features/oddsList/OddsListScreen.*",
    "**/com/betsson/interviewtest/presentation/theme/**",
    "**/com/betsson/interviewtest/InterviewTestApplication.*" // The Application class
)

// Common class directories (adjust paths if your structure is different)
val commonClassDirs = files(
    fileTree("$buildDir/tmp/kotlin-classes/debug") { include("**/*.class") },
    fileTree("$buildDir/intermediates/javac/debug/classes") { include("**/*.class") }
)

// Task for Unit Test Coverage Report - DEFINED FIRST
tasks.register<JacocoReport>("jacocoUnitTestReport") {
    dependsOn("testDebugUnitTest")

    group = "verification"
    description = "Generates JaCoCo unit test coverage report"

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    // Source directories
    val sourceDirs = files(
        "src/main/java",
        "src/main/kotlin"
    ).filter { it.exists() }
    sourceDirectories.setFrom(sourceDirs)

    // Class directories
    classDirectories.setFrom(commonClassDirs.asFileTree.matching {
        exclude(jacocoExclusionPatterns)
    })

    // Execution data
    val executionDataFiles = fileTree(buildDir) {
        include(
            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
            "jacoco/testDebugUnitTest.exec",
            "outputs/code_coverage/debugUnitTest/*.exec"
        )
    }
    executionData.setFrom(executionDataFiles)

    // Don't skip the task even if no execution data is found
    doFirst {
        logger.lifecycle("=== JaCoCo Report Generation ===")
        val foundFiles = executionDataFiles.files
        if (foundFiles.isEmpty()) {
            logger.warn("No JaCoCo execution data found. Generating empty report.")
            // Create an empty execution data file to prevent task skipping
            val emptyExecFile = file("$buildDir/jacoco/testDebugUnitTest.exec")
            emptyExecFile.parentFile.mkdirs()
            emptyExecFile.createNewFile()
            executionData.setFrom(files(emptyExecFile))
        } else {
            logger.lifecycle("Found execution data files:")
            foundFiles.forEach { file ->
                logger.lifecycle("  - ${file.absolutePath} (${file.length()} bytes)")
            }
        }
    }

    doLast {
        val htmlReportPath = reports.html.outputLocation.get().asFile.absolutePath
        val xmlReportPath = reports.xml.outputLocation.get().asFile.absolutePath
        println("JaCoCo Unit Test HTML report: file://$htmlReportPath/index.html")
        println("JaCoCo Unit Test XML report: $xmlReportPath")

        // Verify the XML report was created
        val xmlReportFile = file(xmlReportPath)
        if (xmlReportFile.exists()) {
            println("XML report size: ${xmlReportFile.length()} bytes")
        } else {
            println("WARNING: XML report was not created!")
        }
    }
}

// Task for Instrumented Test (androidTest) Coverage Report
tasks.register<JacocoReport>("jacocoAndroidTestReport") {
    dependsOn("connectedDebugAndroidTest")

    val executionDataFiles = fileTree(buildDir) {
        include("outputs/code_coverage/debugAndroidTest/connected/**/*.ec")
    }

    group = "verification"
    description = "Generates JaCoCo Android test coverage report"

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    // Source directories
    val sourceDirs = files(
        "src/main/java",
        "src/main/kotlin"
    ).filter { it.exists() }
    sourceDirectories.setFrom(sourceDirs)

    // Class directories
    classDirectories.setFrom(commonClassDirs.asFileTree.matching {
        exclude(jacocoExclusionPatterns)
    })

    // Execution data
    executionData.setFrom(executionDataFiles)

    doLast {
        val htmlReportPath = reports.html.outputLocation.get().asFile.absolutePath
        val xmlReportPath = reports.xml.outputLocation.get().asFile.absolutePath
        println("JaCoCo Android Test HTML report: file://$htmlReportPath/index.html")
        println("JaCoCo Android Test XML report: $xmlReportPath")
    }
}

android {
    namespace = "com.betsson.interviewtest"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.betsson.interviewtest"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Debug task to find execution data
tasks.register("findExecFiles") {
    doLast {
        println("=== Searching for JaCoCo Execution Data ===")

        val execFiles = fileTree(buildDir) {
            include("**/*.exec")
        }

        if (execFiles.files.isEmpty()) {
            println("No .exec files found in build directory!")
            println("Build directory: ${buildDir.absolutePath}")

            // Show directory structure
            println("\nBuild directory structure (first 20 files):")
            fileTree(buildDir) {
                include("**/*")
            }.files.take(20).forEach { file ->
                println("  ${file.relativeTo(buildDir)}")
            }
        } else {
            println("Found .exec files:")
            execFiles.files.forEach { file ->
                println("  - ${file.absolutePath} (${file.length()} bytes)")
            }
        }
        println("=== End Search ===")
    }
}

// Simple SonarQube task with unit test coverage only
tasks.register("sonarWithCoverage") {
    group = "verification"
    description = "Run unit tests, generate coverage, and execute SonarQube analysis"

    dependsOn("clean", "testDebugUnitTest", "jacocoUnitTestReport", "sonar")
}

// Make sure sonar runs after coverage report
tasks.named("sonar") {
    mustRunAfter("jacocoUnitTestReport")
}

// Make sure coverage report runs after tests
tasks.named("jacocoUnitTestReport") {
    mustRunAfter("testDebugUnitTest")
}

// Update SonarQube to use unit test coverage
sonar {
    properties {
        property("sonar.projectKey", "Interview-Test")
        property("sonar.projectName", "Interview Test")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.token", "sqp_1168884c11b0f20f79842df01d3c3a7800515e48")
        property("sonar.java.source", "11")
        property("sonar.coverage.jacoco.xmlReportPaths",
            "${layout.buildDirectory.get()}/reports/jacoco/jacocoUnitTestReport/jacocoUnitTestReport.xml")
        property("sonar.coverage.exclusions", jacocoExclusionPatterns.joinToString(","))
        property("sonar.junit.reportPaths",
            "${layout.buildDirectory.get()}/test-results/testDebugUnitTest")

        // Dynamically detect source directories
        val sourceDirs = listOf("src/main/java", "src/main/kotlin")
            .filter { file(it).exists() }
            .joinToString(",")
        property("sonar.sources", sourceDirs)

        // Dynamically detect test directories
        val testDirs = listOf("src/test/java", "src/test/kotlin", "src/androidTest/java", "src/androidTest/kotlin")
            .filter { file(it).exists() }
            .joinToString(",")
        if (testDirs.isNotEmpty()) {
            property("sonar.tests", testDirs)
        }

        property("sonar.java.binaries",
            "${layout.buildDirectory.get()}/tmp/kotlin-classes/debug,${layout.buildDirectory.get()}/intermediates/javac/debug/classes")
        property("sonar.language", "kotlin")
        property("sonar.verbose", "true")
    }
}

// Force JaCoCo execution data generation as fallback
afterEvaluate {
    tasks.withType<Test>().configureEach {
        doLast {
            // Force creation of JaCoCo execution data if missing
            val jacocoDir = file("$buildDir/jacoco")
            jacocoDir.mkdirs()
            val execFile = file("$buildDir/jacoco/${name}.exec")
            if (!execFile.exists()) {
                logger.lifecycle("Creating empty JaCoCo execution file: ${execFile.absolutePath}")
                execFile.createNewFile()
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtimeKtx)
    implementation(libs.androidx.lifecycle.viewModelKtx)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.uiGraphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.uiToolingPreview)
    implementation(libs.androidx.activity.compose)

    // Image Loading (Coil)
    implementation(libs.coil.compose)

    // Dependency Injection (Dagger Hilt)
    implementation(libs.hilt.android)
    testImplementation(libs.jupiter.junit.jupiter)
    kapt(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.uiTooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit.junit)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}