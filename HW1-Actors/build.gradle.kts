import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10-RC"
    kotlin("plugin.serialization") version "1.6.10"
    application
}

group = "me.zerge"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    implementation("org.mock-server:mockserver-netty:5.12.0")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.typesafe.akka:akka-actor_2.13:2.6.18")
    implementation("org.jsoup:jsoup:1.14.3")
    implementation("com.xebialabs.restito:restito:0.9.4")
    testImplementation("com.xebialabs.restito:restito:0.9.4")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}