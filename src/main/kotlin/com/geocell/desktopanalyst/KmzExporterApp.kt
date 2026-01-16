package com.geocell.desktopanalyst

import com.geocell.desktopanalyst.controller.MainController
import com.geocell.desktopanalyst.view.MainView
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Alert
import javafx.scene.image.Image
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch

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

    init {
        // Configurações gráficas para Windows
        val os = System.getProperty("os.name", "").lowercase()
        if (os.contains("win")) {
            // Forçar software rendering se Direct3D falhar
            System.setProperty("prism.order", "sw")
            System.setProperty("prism.verbose", "true")

            // Tenta diferentes backends em ordem de preferência
            val backends = arrayOf("d3d", "es2", "sw")
            System.setProperty("prism.order", backends.joinToString(","))

            // Outras configurações
            System.setProperty("prism.forceGPU", "false")
            System.setProperty("prism.maxvram", "2G")
            System.setProperty("prism.dirtyopts", "false")
        }
    }

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
        primaryStage.title = "Cell KMZ Exporter - Desktop Analyst"


        // Set application icon
        try {
            val iconStream = javaClass.getResourceAsStream("/geocell-0.png")
            if (iconStream != null) {
                primaryStage.icons.add(Image(iconStream))
            } else {
                println("Warning: Could not load application icon")
            }
        } catch (e: Exception) {
            println("Error loading application icon: ${e.message}")
        }

        // Initialize database first
        controller.initializeDatabase()

        val mainView = MainView()

        // Event handlers for the buttons
        setupEventHandlers(mainView, primaryStage)

        val scene = Scene(mainView, 668.0, 700.0)
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun setupEventHandlers(mainView: MainView, primaryStage: Stage) {
        // Pass controller and main view references to tabs that need them
        mainView.getNeighborsTab().setController(controller)
        mainView.getNeighborsTab().setMainView(mainView)
        mainView.getGeographicTab().setController(controller)
        mainView.getGeographicTab().setMainView(mainView)
        mainView.getAdministrativeTab().setController(controller)
        mainView.getAdministrativeTab().setMainView(mainView)
        mainView.getAdministrativeTab().setDatabaseService(controller.databaseService)

        // Load districts into AdministrativeTab dropdown
        mainView.getAdministrativeTab().loadDistricts()

        // Set up NetworkTab
        mainView.getNetworkTab().setController(controller)
        mainView.getNetworkTab().setMainView(mainView)

        // Export KMZ Button Handler
        mainView.getExportButton().setOnAction {
            println("Export to KMZ button clicked")
            exportToKmz(mainView, primaryStage)
        }

        // Neighbors Tab handlers
        val neighborsTab = mainView.getNeighborsTab()
        neighborsTab.getQueryButton().setOnAction {
            neighborsTab.queryNeighbors()
        }

        // Geographic Tab handlers
        val geographicTab = mainView.getGeographicTab()
        geographicTab.getQueryCircleButton().setOnAction {
            geographicTab.queryCircle()
        }
        geographicTab.getQueryRectangleButton().setOnAction {
            geographicTab.queryRectangle()
        }

        // Administrative Tab handlers
        val administrativeTab = mainView.getAdministrativeTab()
        administrativeTab.getQueryButton().setOnAction {
            administrativeTab.queryAdministrativeRegion()
        }

        // Network Tab handlers
        val networkTab = mainView.getNetworkTab()
        networkTab.getQueryLacTacButton().setOnAction {
            networkTab.queryByLacTac()
        }
        networkTab.getQueryEnbGnbButton().setOnAction {
            networkTab.queryByEnbGnb()
        }
        networkTab.getQueryBandButton().setOnAction {
            networkTab.queryByBand()
        }
    }

    /**
     * Handles the Export to KMZ button action.
     *
     * This method detects which tab is currently active and exports accordingly:
     * - Timestamp tab: exports from CSV file
     * - Other tabs: exports from query results
     *
     * @param mainView the main view containing all tabs
     * @param stage the primary stage for file chooser dialogs
     */
    private fun exportToKmz(mainView: MainView, stage: Stage) {
        val selectedTab = mainView.getTabPane().selectionModel.selectedItem
        val tabName = selectedTab?.text ?: "Unknown"

        when (tabName) {
            "Timestamp" -> exportFromTimestampTab(mainView, stage)
            "Neighbors" -> exportFromNeighborsTab(mainView, stage)
            "Geographic" -> exportFromGeographicTab(mainView, stage)
            "Administrative" -> exportFromAdministrativeTab(mainView, stage)
            "Network" -> exportFromNetworkTab(mainView, stage)
            else -> {
                showAlert(
                    Alert.AlertType.WARNING,
                    "Export Not Available",
                    "Export is not available for this tab."
                )
            }
        }
    }

    /**
     * Exports KMZ from the Timestamp tab using a CSV file.
     */
    private fun exportFromTimestampTab(mainView: MainView, stage: Stage) {
        val timestampTab = mainView.getTimestampTab()
        val csvFile = timestampTab.getLoadedCsvFile()

        if (csvFile == null) {
            showAlert(
                Alert.AlertType.WARNING,
                "No CSV File Loaded",
                "Please load a CSV file first using the 'Load CSV' button in the Timestamp tab."
            )
            return
        }

        // Prompt user to select output KMZ file location
        val fileChooser = FileChooser().apply {
            title = "Save KMZ File"
            extensionFilters.add(FileChooser.ExtensionFilter("KMZ Files", "*.kmz"))
            initialFileName = csvFile.nameWithoutExtension + ".kmz"
        }

        val outputFile = fileChooser.showSaveDialog(stage)

        if (outputFile == null) {
            println("Export cancelled by user")
            return
        }

        // Process CSV and generate KMZ asynchronously
        scope.launch {
            try {
                timestampTab.setCsvContent("Processing CSV and generating KMZ...\nPlease wait...")

                val success = controller.processCsvToKmz(csvFile, outputFile)

                if (success) {
                    timestampTab.setCsvContent(
                        "✓ KMZ file generated successfully!\n\n" +
                        "Input: ${csvFile.name}\n" +
                        "Output: ${outputFile.name}\n" +
                        "Location: ${outputFile.absolutePath}\n\n" +
                        "You can now open this file in Google Earth."
                    )
                    showAlert(
                        Alert.AlertType.INFORMATION,
                        "Export Successful",
                        "KMZ file has been created successfully at:\n${outputFile.absolutePath}"
                    )
                } else {
                    timestampTab.setCsvContent(
                        "✗ Error generating KMZ file.\n\n" +
                        "Please check the console for error details."
                    )
                    showAlert(
                        Alert.AlertType.ERROR,
                        "Export Failed",
                        "An error occurred while generating the KMZ file.\nPlease check the console for details."
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                timestampTab.setCsvContent(
                    "✗ Error: ${e.message}\n\n" +
                    "Please check the console for full error details."
                )
                showAlert(
                    Alert.AlertType.ERROR,
                    "Export Error",
                    "An error occurred: ${e.message}"
                )
            }
        }
    }

    /**
     * Exports KMZ from the Neighbors tab using query results.
     */
    private fun exportFromNeighborsTab(mainView: MainView, stage: Stage) {
        val neighborsTab = mainView.getNeighborsTab()
        val cells = neighborsTab.getLastQueryResults()

        if (cells.isEmpty()) {
            showAlert(
                Alert.AlertType.WARNING,
                "No Query Results",
                "Please run a query first to get results to export."
            )
            return
        }

        exportCellsToKmz(cells, "neighbors_query", stage, neighborsTab.getResultsTextArea())
    }

    /**
     * Exports KMZ from the Geographic tab using query results.
     */
    private fun exportFromGeographicTab(mainView: MainView, stage: Stage) {
        val geographicTab = mainView.getGeographicTab()
        val cells = geographicTab.getLastQueryResults()

        if (cells.isEmpty()) {
            showAlert(
                Alert.AlertType.WARNING,
                "No Query Results",
                "Please run a query first to get results to export."
            )
            return
        }

        exportCellsToKmz(cells, "geographic_query", stage, geographicTab.getResultsTextArea())
    }

    /**
     * Exports KMZ from the Administrative tab using query results.
     */
    private fun exportFromAdministrativeTab(mainView: MainView, stage: Stage) {
        val administrativeTab = mainView.getAdministrativeTab()
        val cells = administrativeTab.getLastQueryResults()

        if (cells.isEmpty()) {
            showAlert(
                Alert.AlertType.WARNING,
                "No Query Results",
                "Please run a query first to get results to export."
            )
            return
        }

        exportCellsToKmz(cells, "administrative_query", stage, administrativeTab.getResultsTextArea())
    }

    /**
     * Exports KMZ from the Network tab using query results.
     */
    private fun exportFromNetworkTab(mainView: MainView, stage: Stage) {
        val networkTab = mainView.getNetworkTab()
        val cells = networkTab.getLastQueryResults()

        if (cells.isEmpty()) {
            showAlert(
                Alert.AlertType.WARNING,
                "No Query Results",
                "Please run a query first to get results to export."
            )
            return
        }

        exportCellsToKmz(cells, "network_query", stage, networkTab.getResultsTextArea())
    }

    /**
     * Common method to export cells to KMZ from query results.
     */
    private fun exportCellsToKmz(
        cells: List<com.geocell.desktopanalyst.model.domain.Cell>,
        defaultFileName: String,
        stage: Stage,
        feedbackArea: javafx.scene.control.TextArea
    ) {
        // Prompt user to select output KMZ file location
        val fileChooser = FileChooser().apply {
            title = "Save KMZ File"
            extensionFilters.add(FileChooser.ExtensionFilter("KMZ Files", "*.kmz"))
            initialFileName = "$defaultFileName.kmz"
        }

        val outputFile = fileChooser.showSaveDialog(stage)

        if (outputFile == null) {
            println("Export cancelled by user")
            return
        }

        // Generate KMZ asynchronously
        scope.launch {
            try {
                val originalText = feedbackArea.text
                feedbackArea.text = "Generating KMZ file...\nPlease wait..."

                // Create a simple KMZ with current timestamp for all cells
                val success = controller.exportQueryResultsToKmz(cells, outputFile)

                if (success) {
                    feedbackArea.text = originalText + "\n\n✓ KMZ exported successfully to:\n${outputFile.absolutePath}"
                    showAlert(
                        Alert.AlertType.INFORMATION,
                        "Export Successful",
                        "KMZ file has been created successfully at:\n${outputFile.absolutePath}\n\n" +
                        "Exported ${cells.size} cell(s)."
                    )
                } else {
                    feedbackArea.text = originalText + "\n\n✗ Error generating KMZ file."
                    showAlert(
                        Alert.AlertType.ERROR,
                        "Export Failed",
                        "An error occurred while generating the KMZ file.\nPlease check the console for details."
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showAlert(
                    Alert.AlertType.ERROR,
                    "Export Error",
                    "An error occurred: ${e.message}"
                )
            }
        }
    }

    /**
     * Shows an alert dialog to the user.
     *
     * @param type the type of alert (information, warning, error)
     * @param title the title of the alert dialog
     * @param message the message content to display
     */
    private fun showAlert(type: Alert.AlertType, title: String, message: String) {
        Alert(type).apply {
            this.title = title
            headerText = null
            contentText = message
            showAndWait()
        }
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