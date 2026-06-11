plugins {
    java
    application
}

group = "com.Lab7.1"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.3")

    implementation("io.github.cdimascio:dotenv-java:3.0.0")

    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-fxml:21")
    implementation("org.openjfx:javafx-base:21")
}

application {
    mainClass.set("client.gui.Launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as? StandardJavadocDocletOptions)?.let {
        it.charSet = "UTF-8"
        it.docEncoding = "UTF-8"
        it.addStringOption("Xdoclint:none", "-quiet")
    }
    isFailOnError = false
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "client.gui.Launcher"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--module-path", "lib",
        "--add-modules", "javafx.controls,javafx.fxml,javafx.base"
    )
}