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
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
}

application {
    mainClass.set("client.gui.Launcher")
}

tasks.register<JavaExec>("runClient") {
    mainClass.set("client.gui.Launcher")
    classpath = sourceSets.main.get().runtimeClasspath

    // Добавляем JavaFX модули
    jvmArgs = listOf(
        "--module-path", classpath.asPath,
        "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics,javafx.base"
    )
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec> {
    systemProperty("file.encoding", "UTF-8")
}