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

task<JavaExec>("runServer") {
    group = "application"
    description = "Run the server"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("server.ServerMain")
    systemProperty("file.encoding", "UTF-8")
}

task<JavaExec>("runClient") {
    group = "application"
    description = "Run the client"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("client.gui.Launcher")
    systemProperty("file.encoding", "UTF-8")
}