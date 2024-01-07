@file:Suppress("PropertyName")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom") version "1.3.8"
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.teamvoided.iridium") version "3.1.9"
}

group = properties["maven_group"]!!
version = properties["mod_version"]!!
base.archivesName.set(properties["archives_base_name"].toString())
description = "civilization the mod"
val modid: String by project

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.nucleoid.xyz")
}

modSettings {
    modId(modid)
    modName("Civilization")
    entrypoint("main", "org.teamvoided.civilization.Civilization::commonInit")
    mixinFile("civilization.mixins.json")

}
val squaremap: String by project
val sgui: String by project
val player_data: String by project
val server_translations: String by project
val jts_core: String by project

dependencies {
    compileOnly("xyz.jpenilla", "squaremap-api", squaremap)
    modImplementation(include("eu.pb4", "sgui", sgui))
    modImplementation(include("eu.pb4", "player-data-api", player_data))
    modImplementation(include("xyz.nucleoid", "server-translations-api", server_translations))

    implementation(include("org.locationtech.jts", "jts-core", jts_core))

//    modImplementation("maven.modrinth:flan:1.20.2-1.8.11")
}

tasks {
    val targetJavaVersion = 17
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = targetJavaVersion.toString()
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(JavaVersion.toVersion(targetJavaVersion).toString()))
        withSourcesJar()
    }
}