import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    application
}

group = "com.mr3y"
version = "1.0-alpha01"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.30")
    implementation("com.github.ajalt.clikt:clikt:3.2.0")
    implementation("com.squareup.okio:okio:3.0.0-alpha.9")
    implementation("com.github.ajalt.mordant:mordant:2.0.0-beta2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.1")
}

tasks.test {
    useJUnit()
}

tasks.named<ShadowJar>("shadowJar") {
    // the jar remains up to date even when changing excludes
    // https://github.com/johnrengelman/shadow/issues/62
    outputs.upToDateWhen { false }

    group = "Build"
    description = "Creates a fat jar"
    archiveFileName.set("$archiveBaseName-${project.version}-all.jar")
    isReproducibleFileOrder = true

    from(sourceSets.main.get().output)
    from(project.configurations.runtimeClasspath)

    // Excluding these helps shrink our binary dramatically
    exclude("**/*.kotlin_metadata")
    exclude("**/*.kotlin_module")
    exclude("META-INF/maven/**")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClassName = "MainKt"
}
