plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.base")
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    mainClass.set("client.gui.Launcher")
}

tasks.register<JavaExec>("runServer") {
    mainClass.set("server.ServerMain")
    classpath = sourceSets.main.get().runtimeClasspath
    group = "application"
}

tasks.register<JavaExec>("runClient") {
    mainClass.set("client.gui.Launcher")
    classpath = sourceSets.main.get().runtimeClasspath
    group = "application"
}