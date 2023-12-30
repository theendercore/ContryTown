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
description = "TeamVoided Template"
val modid = project.properties["modid"]!! as String

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") }
        filter { includeGroup("maven.modrinth") }
    }
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") {
        name = "Fuzs Mod Resources"
    }
    maven("https://maven.nucleoid.xyz")
    maven("https://gitlab.com/api/v4/projects/21830712/packages/maven") {
        name = "Flemmli97"
    }
}

modSettings {
    modId(modid)
    modName("Team Voided Template")

    entrypoint("main", "org.teamvoided.template.Template::commonInit")
    entrypoint("client", "org.teamvoided.template.Template::clientInit")
//    dependency("squaremap", "*")
}

val minecraft_version = "1.20.2"
val flan_version = "1.8.11"
val mod_loader = "fabric"
dependencies {
    compileOnly("xyz.jpenilla", "squaremap-api", "1.2.3")
//    modApi("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:20.4.0") //source: https://github.com/Fuzss/forgeconfigapiport-fabric
//    modImplementation("maven.modrinth:open-parties-and-claims:fabric-1.20.4-0.20.4")
    modImplementation(include("eu.pb4:sgui:1.4.0+1.20.4")!!)


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