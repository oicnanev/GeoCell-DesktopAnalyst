import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "com.geocell.desktopanalyst"
version = "1.0.0"

val javafxVersion = "21"
val javafxPlatform = when {
    org.gradle.internal.os.OperatingSystem.current().isWindows -> "win"
    org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "mac"
    else -> "linux"
}
val javafxLibPath = "libs/javafx-sdk-$javafxVersion-$javafxPlatform/lib"

repositories {
    mavenCentral()
    flatDir {
        dirs(javafxLibPath)
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.8.0")

    // JavaFX via flatDir - corrigido para usar a estrutura correta de pastas
    listOf("base", "controls", "fxml", "graphics").forEach { module ->
        //implementation("org.openjfx:javafx-$module:$javafxVersion")
        implementation(mapOf("name" to "javafx.$module", "ext" to "jar"))
        //implementation(fileTree(javafxLibPath) { include("javafx-$module*.jar") })
    }

    // Outras dependências
    implementation("org.locationtech.jts:jts-core:1.19.0")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("net.postgis:postgis-jdbc:2.5.1")
    implementation("org.jetbrains.exposed:exposed-core:0.44.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.44.0")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.2")
    implementation("de.micromata.jak:JavaAPIforKml:2.2.1")
    implementation("org.slf4j:slf4j-simple:2.0.9")
}

tasks {
    compileKotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(listOf("-Xjvm-default=all"))
        }
    }
    compileTestKotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

application {
    mainClass.set("com.geocell.desktopanalyst.KmzExporterApp")
    applicationDefaultJvmArgs = listOf(
        "--module-path=$javafxLibPath",
        "--add-modules=javafx.controls,javafx.fxml,javafx.graphics,javafx.base",
        "--add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED",
        "--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED",
        "--add-opens=javafx.graphics/com.sun.javafx.css=ALL-UNNAMED",
        "--enable-native-access=javafx.graphics",
        "--add-opens=javafx.graphics/com.sun.marlin=ALL-UNNAMED",
        "--add-opens=javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED",
        "--add-opens=javafx.graphics/com.sun.javafx.util=ALL-UNNAMED",
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.io=ALL-UNNAMED",
        "-Dprism.verbose=true" // Opcional: para debug de rendering
    )
}

tasks.withType<JavaExec> {
    jvmArgs = listOf(
        "--module-path=$javafxLibPath",
        "--add-modules=javafx.controls,javafx.fxml,javafx.graphics,javafx.base",
        "--add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED",
        "--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED",
        "--enable-native-access=javafx.graphics",
        "--add-opens=javafx.graphics/com.sun.marlin=ALL-UNNAMED"
    )
}

// Task para gerar instalador com jpackage
val installerType = when {
    org.gradle.internal.os.OperatingSystem.current().isWindows -> "exe"
    org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "dmg"
    else -> "app-image"
}

tasks.register<Exec>("jpackage") {
    group = "distribution"
    description = "Generates native installer with jpackage"

    dependsOn("build") // Garante que o .jar seja gerado antes

    val outputDir = layout.buildDirectory.dir("jpackage").get().asFile.absolutePath
    val inputDir = layout.buildDirectory.dir("libs").get().asFile.absolutePath
    val mainJar = tasks.named<Jar>("jar").get().archiveFileName.get()

    commandLine(
        "jpackage",
        "--type", installerType,
        "--name", "KMZ Exporter",
        "--input", inputDir,
        "--main-jar", mainJar,
        "--main-class", "com.geocell.desktopanalyst.KmzExporterApp",
        "--dest", outputDir,
        "--java-options", "-Xmx2048m",
        "--java-options", "-Dprism.order=sw",
        "--module-path", javafxLibPath,
        "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics,javafx.base,java.sql,java.naming,jdk.unsupported,java.scripting",
        "--java-options", "--enable-native-access=javafx.graphics",
        "--java-options", "--add-opens=javafx.graphics/com.sun.marlin=ALL-UNNAMED"
    )
}

// Task para verificar se o JavaFX está configurado corretamente
tasks.register("checkJavaFX") {
    group = "verification"
    description = "Checks if JavaFX is properly configured"

    doLast {
        val javafxDir = file(javafxLibPath)
        if (!javafxDir.exists()) {
            throw GradleException("JavaFX SDK not found at: $javafxLibPath\n" +
                    "Please download JavaFX $javafxVersion from https://gluonhq.com/products/javafx/\n" +
                    "and extract it to libs/javafx-sdk-$javafxVersion/")
        }

        println("JavaFX found at: ${javafxDir.absolutePath}")
        javafxDir.listFiles()?.forEach { file ->
            println("  - ${file.name}")
        }
    }
}

// Executa o checkJavaFX antes de compilar
tasks.named("compileKotlin") {
    dependsOn("checkJavaFX")
}