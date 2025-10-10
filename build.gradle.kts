plugins {
    kotlin("jvm") version "1.9.22"
    id("org.openjfx.javafxplugin") version "0.0.13"
    application
}

group = "com.geocell.desktopanalyst"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.8.0")

    // JTS Core para geometrias
    implementation("org.locationtech.jts:jts-core:1.19.0")

    // Database PostgreSQL with PostGIS
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("net.postgis:postgis-jdbc:2.5.1")

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.44.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.44.0")

    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.2")
    implementation("de.micromata.jak:JavaAPIforKml:2.2.1")
    implementation("org.locationtech.jts:jts-core:1.19.0")

    // KMZ generation
    implementation("de.micromata.jak:JavaAPIforKml:2.2.1")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "21"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "21"
    }
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs = listOf(
        "--module-path=/path/to/javafx-sdk-21/lib",
        "--add-modules=javafx.controls,javafx.fxml"
    )
}

application {
    mainClass.set("com.geocell.desktopanalyst.KmzExporterApp")
}