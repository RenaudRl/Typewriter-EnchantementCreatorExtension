plugins {
    kotlin("jvm") version "2.3.20"
    id("com.typewritermc.module-plugin") version "2.1.0"
}

group = "btcrenaud"
version = "0.0.5"

repositories {
    mavenLocal()
}

dependencies {
    implementation("com.typewritermc:BasicExtension:0.9.0")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

typewriter {
    namespace = "btcrenaud"

    extension {
        name = "EnchantmentCreator"
        shortDescription = "Advanced enchantment creation system for TypeWriter"
        description = "EnchantmentCreator extension for TypeWriter providing advanced " +
            "enchantment creation and management tools for Paper 1.21+. Fully compatible " +
            "with the official TypeWriter engine."
        engineVersion = "0.9.0-beta-174"
        channel = com.typewritermc.moduleplugin.ReleaseChannel.BETA
        dependencies {
            dependency("typewritermc", "Basic")
        }
        paper()
    }
}

kotlin {
    jvmToolchain(25)
}
