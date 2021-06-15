import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("application")

    kotlin("jvm") version "1.5.10"

    // Shadow jars
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

application.mainClass.set("dev.giuliopime.helpdesk.LauncherKt")
group = "dev.giuliopime.helpdesk"
version = "2.0"

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_15
    targetCompatibility = JavaVersion.VERSION_15
}

repositories {
    mavenCentral()
    maven {
        name = "m2-dv8tion"
        url = uri("https://m2.dv8tion.net/releases")
    }
    maven("https://jitpack.io/")
}

val ktorVersion = "1.5.4"

dependencies {
    // JDA
    implementation("net.dv8tion:JDA:4.3.0_277") {
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

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.10")
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    withType(JavaCompile::class) {
        options.encoding = "UTF-8"
    }
    withType(KotlinCompile::class) {
        kotlinOptions {
            jvmTarget = "15"
        }
    }

    shadowJar {
        archiveFileName.set("helpdesk.jar")
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
