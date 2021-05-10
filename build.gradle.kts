import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"

    // Shadow jars
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "dev.giuliopime.helpdesk"
version = "2.0"

repositories {
    mavenCentral()
    maven {
        name = "m2-dv8tion"
        url = uri("https://m2.dv8tion.net/releases")
    }
    maven("https://jitpack.io/")
    jcenter()
}

val ktorVersion = "1.5.4"

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:4.2.1_255") {
        exclude(module = "opus-java")
    }
    // JDA Utilities for Kotlin
    implementation("com.github.minndevelopment:jda-ktx:1a45395")
    // JDA Webhook
    implementation("club.minnced:discord-webhooks:0.5.6")

    // InfluxDB
    implementation("com.influxdb:influxdb-client-java:2.0.0")
    // Redis
    implementation("redis.clients:jedis:3.5.2")
    // MongoDB
    implementation("org.litote.kmongo:kmongo:4.2.4")

    // .env files
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")

    // KTor
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")

    // Logger
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.6")
    implementation("org.slf4j:slf4j-api:1.7.28")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("shadow")
        mergeServiceFiles()
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to "dev.giuliopime.helpdesk.LauncherKt"
                )
            )
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

