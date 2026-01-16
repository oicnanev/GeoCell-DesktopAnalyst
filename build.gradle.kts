import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.internal.os.OperatingSystem
import java.io.File

plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "com.geocell.desktopanalyst"
version = "1.0.0"

val javafxVersion = "21"
val javafxPlatform = when {
    OperatingSystem.current().isWindows -> "win"
    OperatingSystem.current().isMacOsX -> "mac"
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

    // JavaFX
    listOf("base", "controls", "fxml", "graphics").forEach { module ->
        implementation(mapOf("name" to "javafx.$module", "ext" to "jar"))
    }

    // Outras dependências (mantenha as suas)
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
        "-Dprism.order=sw",                 // Força software rendering
        "-Dprism.verbose=true",             // Logs detalhados (opcional)
        "-Dprism.forceGPU=true",            // Tenta forçar GPU
        "-Dprism.maxvram=2G",               // Limite de VRAM
        "-Dprism.dirtyopts=false",          // Desabilita otimizações problemáticas
        "-Dglass.platform=win"              // Força plataforma Windows
    )
}

// Task para criar JAR executável (fat jar)
tasks.register<Jar>("createFatJar") {
    group = "distribution"
    description = "Cria JAR com todas as dependências"

    archiveBaseName.set("KMZ-Exporter")
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
        attributes["Implementation-Version"] = project.version
    }

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)

    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") && !it.name.startsWith("javafx") }
            .map { zipTree(it) }
    })

    doLast {
        println("✓ JAR criado: ${archiveFile.get()}")
    }
}

// Task SIMPLES para criar distribuição portável
tasks.register<Copy>("createPortableDistribution") {
    group = "distribution"
    description = "Cria distribuição portável"

    dependsOn("createFatJar")

    val distDir = file("${layout.buildDirectory.get().asFile}/distributions/portable")

    // 1. Copiar JAR
    from(tasks.getByName<Jar>("createFatJar").archiveFile) {
        into(".")
        rename { "KMZ-Exporter.jar" }
    }

    // 2. Copiar JavaFX
    from(file(javafxLibPath).parentFile) {
        into("javafx")
    }

    // 3. Copiar templates e processá-los
    from("distribution-templates") {
        include("*.template")
        rename { it.replace(".template", "") }

        // Substituir variáveis nos templates
        filter { line ->
            line.replace("{{VERSION}}", version.toString())
        }
    }

    into(distDir)

    doFirst {
        println("🚀 Criando distribuição portável...")
        distDir.mkdirs()
    }

    doLast {
        // Tornar script Linux executável
        val linuxScript = File(distDir, "run-linux.sh")
        if (linuxScript.exists()) {
            linuxScript.setExecutable(true)
        }

        println("✅ Distribuição criada em: ${distDir.absolutePath}")
        println("📁 Conteúdo:")
        distDir.listFiles()?.forEach { file ->
            val icon = if (file.isDirectory) "📁" else "📄"
            val size = if (file.isFile) " (${file.length() / 1024} KB)" else ""
            println("  $icon ${file.name}$size")
        }

        // Criar ZIP automaticamente
        createDistributionZip(distDir)
    }
}

// Função para criar ZIP da distribuição
fun createDistributionZip(distDir: File) {
    val zipFile = File(distDir.parent, "KMZ-Exporter-v${project.version}-portable.zip")

    project.exec {
        workingDir = distDir
        commandLine("zip", "-r", zipFile.absolutePath, ".")
    }

    val sizeMB = zipFile.length() / (1024 * 1024)
    println("\n📦 ZIP criado: ${zipFile.name} (${sizeMB} MB)")
    println("📍 Local: ${zipFile.absolutePath}")
    println("\n🎉 PRONTO PARA DISTRIBUIR!")
}

// Task para executar a aplicação
tasks.register<JavaExec>("runApp") {
    group = "application"
    description = "Executa a aplicação"

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(application.mainClass.get())
    jvmArgs = listOf(
        "--module-path=$javafxLibPath",
        "--add-modules=javafx.controls,javafx.fxml,javafx.graphics,javafx.base",
        "--add-opens=javafx.controls/javafx.scene.control=ALL-UNNAMED",
        "--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED",
        "--enable-native-access=javafx.graphics",
        "-Xmx2048m",
        "-Dprism.order=sw",
        "-Dprism.verbose=true"
    )
}

// Task para limpar tudo
tasks.register("cleanAll") {
    group = "build"
    description = "Limpa build e distribuições"

    dependsOn("clean")

    doLast {
        val distDir = file("build/distributions")
        if (distDir.exists()) {
            distDir.deleteRecursively()
            println("✓ Distribuições removidas")
        }
    }
}