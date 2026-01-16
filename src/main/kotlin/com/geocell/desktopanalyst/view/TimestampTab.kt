package com.geocell.desktopanalyst.view

import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.scene.control.Hyperlink
import javafx.scene.control.TextArea
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage

class TimestampTab(): BorderPane() {

    // UI Components
    private val titleText = Text("Turn .csv file with CGIs and Timestamps into Google Earth .kmz")
    private val exampleLink = Hyperlink("example.csv")
    private val textFlow = TextFlow().apply{
        style = "-fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-font-family: 'Monospaced';"
        //style = "-fx-font-family: 'Monospaced';"
        prefHeight = 104.0
        prefWidth = 484.0
    }

    private val loadCsvButton = Button("Load CSV").apply {
        prefHeight = 26.0
        prefWidth = 105.0
    }

    // Store the loaded CSV file
    private var loadedCsvFile: java.io.File? = null

    init {
        setupLayout()
        setupStyles()
        setupEventHandlers()
    }

    private fun setupLayout() {
        // Main panel configuration
        padding = Insets(10.0)

        // Superior panel with title and link
        val topPanel = VBox(10.0).apply {
            children.addAll(titleText, exampleLink)
        }

        // Central layout
        val centerPanel = HBox(10.0).apply {
            children.addAll(textFlow)
            HBox.setHgrow(textFlow, Priority.ALWAYS)
        }

        // Inferior panel with button
        val bottomPanel = HBox().apply {
            alignment = javafx.geometry.Pos.CENTER_RIGHT
            padding = Insets(10.0, 0.0, 0.0, 0.0)
            children.add(loadCsvButton)
        }

        // Add all panels to main layout
        top = topPanel
        center = centerPanel
        bottom = bottomPanel

        // Growing constraints configuration
        VBox.setVgrow(centerPanel, Priority.ALWAYS)
    }

    private fun setupStyles() {
        titleText.wrappingWidth = 397.0   // Title text style
        textFlow.padding = Insets(5.0)  // TextFlow style
    }

    private fun setupEventHandlers() {
        // Example csv link handler
        exampleLink.setOnAction {
            // TODO: Implement opening of example CSV
            showExampleCsv()
        }

        // Load CSV button handler
        loadCsvButton.setOnAction {
            loadCsvFile()
        }
    }

    private fun showExampleCsv() {
        // TODO: implement logic to show exempleCSV
        println("Opening Example.csv")

        // Example of of content to show on the TextFlow
        val sampleText = Text("""
            CSV format example:
            timestamp,cgi,color,target,notes
            2025/04/26 00:02:34,268-06-8840-8453,red,John Smith,Initial deployment
            2025/04/26 00:03:34,268-03-8521869,green,Charlie Brown,High traffic area
        """.trimIndent())

        textFlow.children.setAll(sampleText)
    }

    private fun loadCsvFile() {
        println("Load CSV file")

        val fileChooser = FileChooser().apply {
            title = "Select CSV File"
            extensionFilters.add(FileChooser.ExtensionFilter("CSV Files", "*.csv"))
        }

        val stage = scene?.window as? Stage
        val file = fileChooser.showOpenDialog(stage)

        file?.let { selectedFile ->
            println("Select file: ${selectedFile.name}")

            try {
                // Store the loaded file for later export
                loadedCsvFile = selectedFile

                // show content in textFlow
                setCsvContent("${selectedFile.name} loaded\n" +
                            "Localization: ${selectedFile.absolutePath}\n\n" +
                            "Ready to export to KMZ. Click 'Export to KMZ' button.".trimIndent()
                )
            } catch (e: Exception) {
                setCsvContent("Error loading file: ${e.message}")
                loadedCsvFile = null
            }
        } ?: run {
            println("None selected file")
        }
    }

    // Public Methods to integrate with the Controller
    fun setCsvContent(content: String) {
        textFlow.children.setAll(Text(content))
    }

    fun getLoadCsvButton(): Button = loadCsvButton

    fun getExampleLink(): Hyperlink = exampleLink

    fun getLoadedCsvFile(): java.io.File? = loadedCsvFile
}