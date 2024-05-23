@file:Suppress("PropertyName", "VariableNaming")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.iridium)
    alias(libs.plugins.iridium.publish)
    alias(libs.plugins.iridium.upload)
    alias(libs.plugins.detekt)
}

detekt {
    allRules = true
    config.setFrom(file("gradle/detekt.yml"))
}


group = property("maven_group")!!
version = property("mod_version")!!
base.archivesName.set(property("archives_base_name") as String)
description = property("description") as String

val modid: String by project
val mod_name: String by project
val modrinth_id: String? by project
val curse_id: String? by project

repositories {
    maven("https://teamvoided.org/releases")
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://maven.nucleoid.xyz")
    mavenCentral()
}

modSettings {
    modId(modid)
    modName(mod_name)

    entrypoint("main", "org.teamvoided.civilization.Civilization::commonInit")
//    entrypoint("fabric-datagen", "org.teamvoided.template.TemplateData")
    mixinFile("$modid.mixins.json")
//    accessWidener("$modid.accesswidener")
}

dependencies {
    modImplementation(fileTree("libs"))
    modImplementation(libs.farrow)

    compileOnly(libs.squaremap.api)

    modImplementation(libs.sgui)
    modImplementation(libs.player.data.api)
    modImplementation(libs.server.translations.api)

    implementation(libs.jts.core)
//    modImplementation("maven.modrinth:flan:1.20.2-1.8.11")

    modImplementation(libs.fabric.permissions.api)
    compileOnly(libs.luckperms.api)
}

sourceSets["main"].resources.srcDir("src/main/generated")

tasks {
    val targetJavaVersion = 21
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

publishScript {
    releaseRepository("TeamVoided", "https://maven.teamvoided.org/releases")
    publication(modSettings.modId(), false)
    publishSources(true)
}

uploadConfig {
//    debugMode = true
    modrinthId = modrinth_id
    curseId = curse_id

    // FabricApi
    modrinthDependency("P7dR8mSH", uploadConfig.REQUIRED)
    curseDependency("fabric-api", uploadConfig.REQUIRED)
    // Fabric Language Kotlin
    modrinthDependency("Ha28R6CL", uploadConfig.REQUIRED)
    curseDependency("fabric-language-kotlin", uploadConfig.REQUIRED)
}
