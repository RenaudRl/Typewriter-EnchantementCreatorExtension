plugins {
    kotlin("jvm") version "2.2.10"
    id("com.typewritermc.module-plugin")
}

group = "btc.renaud"
version = "0.1" // The version is the same with the plugin to avoid confusion. :)

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://maven.typewritermc.com/beta/")
}

dependencies {
    implementation("com.typewritermc:BasicExtension:0.9.0")
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:5.14.2")
}

typewriter {
    namespace = "renaud"

    extension {
        name = "EnchantmentCreator"
        shortDescription = "Typewriter extension For Enchantment Creation support."
        description =
            "This extension adds support for enchantment Creation with multiples triggers, criteria , cooldown and a lot" +
            " of differents things!"
        engineVersion = file("../../version.txt").readText().trim()
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA
        dependencies {
            dependency("typewritermc", "Basic")
        }
        paper()

    }

}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}


