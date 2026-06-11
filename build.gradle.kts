plugins {
    java
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.Lab7.1"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("client.gui.Launcher")
}

tasks.register<Copy>("copyJavafxLibs") {
    group = "build setup"
    description = "Copy JavaFX runtime jars into lib/ for IntelliJ run configuration"
    from({
        configurations.runtimeClasspath.get().filter {
            it.name.matches(Regex("javafx-(base|controls|fxml|graphics)-\\d+.*\\.jar"))
        }
    })
    into(layout.projectDirectory.dir("lib"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.processResources {
    filteringCharset = "UTF-8"
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
