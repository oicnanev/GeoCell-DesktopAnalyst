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

/**
 * Main JavaFX application for exporting cellular network data to KMZ format.
 *
 * This application provides a graphical user interface for:
 * - Selecting CSV files containing cell data with timestamps and metadata
 * - Exporting the data to KMZ format for visualization in Google Earth
 * - Monitoring export progress with visual feedback
 * - Managing database connections for cellular infrastructure data
 *
 * The application uses coroutines for non-blocking UI operations and provides
 * real-time progress updates during the export process.
 *
 * @see MainController
 * @since 1.0.0
 */
class KmzExporterApp : Application() {

    private val controller = MainController()
    private val scope = CoroutineScope(Dispatchers.JavaFx)

    /**
     * Initializes and displays the main application window.
     *
     * Sets up the user interface with file selection controls, export functionality,
     * and progress monitoring. Also initializes the database connection with
     * predefined credentials.
     *
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set
     */
    override fun start(primaryStage: Stage) {
        primaryStage.title = "Cell KMZ Exporter"

        // UI Components
        val selectCsvBtn = Button("Select CSV File")
        val csvPathLabel = Label("No file selected")
        val exportBtn = Button("Export to KMZ")
        val progressBar = ProgressBar()
        val statusLabel = Label("Ready")

        var selectedCsvFile: File? = null

        // CSV File Selection Handler
        selectCsvBtn.setOnAction {
            FileChooser().apply {
                title = "Select CSV File"
                extensionFilters.add(FileChooser.ExtensionFilter("CSV Files", "*.csv"))
            }.showOpenDialog(primaryStage)?.let { file ->
                selectedCsvFile = file
                csvPathLabel.text = file.name
            }
        }

        // KMZ Export Handler
        exportBtn.setOnAction {
            selectedCsvFile?.let { csvFile ->
                FileChooser().apply {
                    title = "Save KMZ File"
                    extensionFilters.add(FileChooser.ExtensionFilter("KMZ Files", "*.kmz"))
                }.showSaveDialog(primaryStage)?.let { outputFile ->
                    scope.launch {
                        // Update UI for processing state
                        exportBtn.isDisable = true
                        progressBar.progress = ProgressBar.INDETERMINATE_PROGRESS
                        statusLabel.text = "Processing..."

                        // Execute export process
                        val success = controller.processCsvToKmz(csvFile, outputFile)

                        // Update UI with result
                        statusLabel.text = if (success) "Export completed!" else "Export failed!"
                        progressBar.progress = if (success) 1.0 else 0.0
                        exportBtn.isDisable = false
                    }
                }
            }
        }

        // Layout Configuration
        val layout = VBox(10.0).apply {
            children.addAll(
                selectCsvBtn, csvPathLabel, exportBtn, progressBar, statusLabel
            )
            padding = Insets(20.0)
        }

        primaryStage.scene = Scene(layout, 400.0, 200.0)
        primaryStage.show()

        // Initialize database connection
        controller.initializeDatabase()
    }

    /**
     * Companion object containing the application entry point.
     *
     * This method launches the JavaFX application when the program starts.
     */
    companion object {

        /**
         * The main entry point for the JavaFX application.
         *
         * @param args the command line arguments passed to the application
         */
        @JvmStatic
        fun main(args: Array<String>) {
            launch(KmzExporterApp::class.java, *args)
        }
    }
}