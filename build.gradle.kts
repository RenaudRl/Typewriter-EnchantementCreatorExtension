plugins {
    kotlin("jvm") version "2.2.10"
    id("com.typewritermc.module-plugin") version "2.2.0"
}

group = "btcrenaud"
version = "0.9"

repositories {
}

dependencies {
    implementation("com.typewritermc:BasicExtension:0.9.0")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation(kotlin("test"))
    testRuntimeOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

typewriter {
    namespace = "btcrenaud"

    extension {
        name = "EnchantmentCreator"
        shortDescription = "Advanced enchantment creation system for TypeWriter"
        description = "EnchantmentCreator extension for TypeWriter providing advanced " +
            "enchantment creation and management tools for Paper 1.21+. Fully compatible " +
            "with the official TypeWriter engine."
        engineVersion = "0.9.0-beta-176"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA
        dependencies {
            dependency("typewritermc", "Basic")
        }
        paper()
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

