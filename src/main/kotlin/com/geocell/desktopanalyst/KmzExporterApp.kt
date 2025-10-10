package com.geocell.desktopanalyst

import com.geocell.desktopanalyst.controller.MainController
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import java.io.File

class KmzExporterApp : Application() {
    private val controller = MainController()
    private val scope = CoroutineScope(Dispatchers.JavaFx)

    override fun start(primaryStage: Stage) {
        primaryStage.title = "Cell KMZ Exporter"

        val selectCsvBtn = Button("Select CSV File")
        val csvPathLabel = Label("No file selected")
        val exportBtn = Button("Export to KMZ")
        val progressBar = ProgressBar()
        val statusLabel = Label("Ready")

        var selectedCsvFile: File? = null

        selectCsvBtn.setOnAction {
            FileChooser().apply {
                title = "Select CSV File"
                extensionFilters.add(FileChooser.ExtensionFilter("CSV Files", "*.csv"))
            }.showOpenDialog(primaryStage)?.let { file ->
                selectedCsvFile = file
                csvPathLabel.text = file.name
            }
        }

        exportBtn.setOnAction {
            selectedCsvFile?.let { csvFile ->
                FileChooser().apply {
                    title = "Save KMZ File"
                    extensionFilters.add(FileChooser.ExtensionFilter("KMZ Files", "*.kmz"))
                }.showSaveDialog(primaryStage)?.let { outputFile ->
                    scope.launch {
                        exportBtn.isDisable = true
                        progressBar.progress = ProgressBar.INDETERMINATE_PROGRESS
                        statusLabel.text = "Processing..."

                        val success = controller.processCsvToKmz(csvFile, outputFile)

                        statusLabel.text = if (success) "Export completed!" else "Export failed!"
                        progressBar.progress = if (success) 1.0 else 0.0
                        exportBtn.isDisable = false
                    }
                }
            }
        }

        val layout = VBox(10.0).apply {
            children.addAll(
                selectCsvBtn, csvPathLabel, exportBtn, progressBar, statusLabel
            )
            padding = Insets(20.0)
        }

        primaryStage.scene = Scene(layout, 400.0, 200.0)
        primaryStage.show()

        // Initialize database connection
        controller.initializeDatabase(
            host = "192.168.1.27",
            port = 5432,
            database = "geocell",
            user = "postgres",
            password = "4w5_Yd4xee35$"
        )
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(KmzExporterApp::class.java, *args)
        }
    }
}