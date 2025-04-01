plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven("https://maven.architectury.dev/")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.terraformersmc.com/")
}

dependencies {
    implementation(libs.kotlin)

    implementation(libs.licenser)
    implementation(libs.shadow)
    implementation(libs.loom)
    implementation(libs.architectury)

    implementation(libs.blossom)
    implementation(libs.ideaExt)
}