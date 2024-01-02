import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom") version "1.3.8"
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.teamvoided.iridium") version "3.1.9"
}

group = project.properties["maven_group"]!!
version = project.properties["mod_version"]!!
base.archivesName.set(project.properties["archives_base_name"] as String)
description = "civilization the mod"
val modid = project.properties["modid"]!! as String

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

}

dependencies {
    compileOnly("xyz.jpenilla", "squaremap-api", "1.2.3")
    modImplementation(include("eu.pb4:sgui:1.4.0+1.20.4")!!)
    modImplementation(include("eu.pb4:player-data-api:0.4.0+1.20.3")!!)

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