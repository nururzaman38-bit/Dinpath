// Top-level build file where you can add configuration options common to all sub-projects/modules.
import java.io.File
import java.util.Base64

val base64File = File(rootDir, "debug.keystore.base64")
val keystoreFile = File(rootDir, "debug.keystore")
println("--- DEBUG KEYSTORE CONFIG ---")
println("rootDir: ${rootDir.absolutePath}")
println("base64File path: ${base64File.absolutePath}, exists: ${base64File.exists()}")
println("keystoreFile path: ${keystoreFile.absolutePath}, exists: ${keystoreFile.exists()}")
val apkFile = File(rootDir, "app-debug.apk")
if (apkFile.exists()) {
    println("APK file path: ${apkFile.absolutePath}, size: ${apkFile.length()} bytes")
} else {
    println("APK file does not exist at ${apkFile.absolutePath}")
}
val buildOutputsApk = File(rootDir, ".build-outputs/app-debug.apk")
if (buildOutputsApk.exists()) {
    println("Build outputs APK path: ${buildOutputsApk.absolutePath}, size: ${buildOutputsApk.length()} bytes")
} else {
    println("Build outputs APK does not exist at ${buildOutputsApk.absolutePath}")
}
if (base64File.exists() && !keystoreFile.exists()) {
    try {
        val bytes = Base64.getDecoder().decode(base64File.readText().trim())
        keystoreFile.writeBytes(bytes)
        println("Successfully decoded debug.keystore from base64")
    } catch (e: Exception) {
        println("Error decoding debug.keystore: ${e.message}")
        e.printStackTrace()
    }
}
println("-----------------------------")

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
}
