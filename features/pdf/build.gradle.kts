/*
 * Copyright (c) 2021. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import java.io.FileInputStream
import java.util.Properties
import org.gradle.api.Project

plugins {
    alias(libs.plugins.sgp.base)
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

val psPdfKitKey = readPropertyValue(
    filePath = "$rootDir/${BuildAndroidConfig.API_KEYS_PROPS_FILE}",
    key = "PSPDFKIT_LICENSE",
    defaultValue = ""
)

android {
    namespace = "app.ss.pdf"

    defaultConfig {
        manifestPlaceholders["psPdfKitKey"] = psPdfKitKey
    }

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
    }
}

dependencies {
    coreLibraryDesugaring(libs.coreLibraryDesugaring)
    implementation(projects.common.core)
    implementation(projects.common.design)
    implementation(projects.common.lessonsData)
    implementation(projects.features.media)
    implementation(projects.common.translations)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.google.material)
    implementation(libs.androidx.preference)

    implementation(libs.google.hilt.android)
    kapt(libs.google.hilt.compiler)

    implementation(libs.timber)

    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.coroutines.android)

    api(libs.pdfkit)

    testImplementation(libs.bundles.testing.common)
    kaptTest(libs.google.hilt.compiler)
    androidTestImplementation(libs.bundles.testing.android.common)
    kaptAndroidTest(libs.google.hilt.compiler)
    testImplementation(projects.libraries.testUtils)
}

/**
 * Reads a value saved in a [Properties] file
 */
fun Project.readPropertyValue(
    filePath: String,
    key: String,
    defaultValue: String
): String {
    val file = file(filePath)
    return if (file.exists()) {
        val keyProps = Properties().apply {
            load(FileInputStream(file))
        }
        return keyProps.getProperty(key, defaultValue)
    } else {
        defaultValue
    }
}

object BuildAndroidConfig {
    const val API_KEYS_PROPS_FILE = "release/ss_public_keys.properties"
}
