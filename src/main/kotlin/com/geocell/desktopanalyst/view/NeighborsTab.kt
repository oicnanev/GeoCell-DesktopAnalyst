package com.geocell.desktopanalyst.view

import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text

/**
 * Tab for querying neighbor cells of a given CGI within a specified radius.
 */
class NeighborsTab : BorderPane() {

    // UI Components
    private val titleText = Text("Neighbor cells of a CGI given a radius in kilometres")
    private val cgiTextField = TextField().apply {
        promptText = "CGI"
        prefWidth = 150.0
    }
    private val radiusTextField = TextField().apply {
        promptText = "radius (kms)"
        prefWidth = 150.0
    }
    private val queryButton = Button("Query Neighbors").apply {
        prefHeight = 26.0
        prefWidth = 120.0
    }
    private val resultsTextArea = TextArea().apply {
        prefHeight = 128.0
        prefWidth = 431.0
        isEditable = false
        style = "-fx-font-family: 'Monospaced';"
    }

    // Controller reference
    private var controller: com.geocell.desktopanalyst.controller.MainController? = null
    
    // Main view reference for accessing filters
    private var mainView: MainView? = null

    init {
        setupLayout()
        setupStyles()
        setupEventHandlers()
    }

    private fun setupLayout() {
        padding = Insets(10.0)

        // Top panel with title
        val topPanel = VBox(10.0).apply {
            children.add(titleText)
        }

        // Center panel with input fields and results
        val centerPanel = HBox(20.0).apply {
            // Left side - input fields
            val inputPanel = VBox(15.0).apply {
                children.addAll(
                    Label("CGI:"),
                    cgiTextField,
                    Label("Radius (km):"),
                    radiusTextField,
                    queryButton
                )
                padding = Insets(10.0)
            }

            // Right side - results area
            val resultsPanel = VBox(5.0).apply {
                children.addAll(
                    Label("Results:"),
                    resultsTextArea
                )
                VBox.setVgrow(resultsTextArea, Priority.ALWAYS)
            }

            children.addAll(inputPanel, resultsPanel)
            HBox.setHgrow(resultsPanel, Priority.ALWAYS)
        }

        // Set layout
        top = topPanel
        center = centerPanel
    }

    private fun setupStyles() {
        titleText.wrappingWidth = 397.0
        resultsTextArea.style = "-fx-background-color: #f5f5f5; -fx-border-color: #cccccc;"
    }

    private fun setupEventHandlers() {
        queryButton.setOnAction {
            queryNeighbors()
        }

        // Allow Enter key to trigger query
        cgiTextField.setOnAction { queryNeighbors() }
        radiusTextField.setOnAction { queryNeighbors() }
    }

    fun queryNeighbors() {
        val cgi = cgiTextField.text.trim()
        val radiusText = radiusTextField.text.trim()

        if (cgi.isEmpty()) {
            resultsTextArea.text = "Error: Please enter a CGI"
            return
        }

        if (radiusText.isEmpty()) {
            resultsTextArea.text = "Error: Please enter a radius"
            return
        }

        val radius = try {
            radiusText.toDouble()
        } catch (e: NumberFormatException) {
            resultsTextArea.text = "Error: Radius must be a valid number"
            return
        }

        if (radius <= 0) {
            resultsTextArea.text = "Error: Radius must be greater than 0"
            return
        }

        // Check if controller is available
        if (controller == null) {
            resultsTextArea.text = "Error: Controller not initialized. Please restart the application."
            return
        }

        try {
            // Show processing message
            resultsTextArea.text = "Querying neighbors for CGI: $cgi\nRadius: $radius km\nProcessing..."
            
            // Extract filters from main UI
            val filters = extractFiltersFromMainView()
            
            // Query the database through controller with filters
            val neighbors = controller!!.queryNeighbors(
                cgi = cgi,
                radiusKm = radius,
                technologies = filters.technologies,
                operators = filters.operators,
                sameNetwork = filters.sameNetwork,
                startDate = filters.startDate,
                endDate = filters.endDate
            )
            
            // Format and display results
            resultsTextArea.text = formatResults(neighbors, cgi, radius, filters)
            
            println("Querying neighbors - CGI: $cgi, Radius: $radius km")
            println("Filters applied: $filters")
            println("Found ${neighbors.size} neighbor(s)")
            
        } catch (e: IllegalArgumentException) {
            resultsTextArea.text = "Error: ${e.message}"
        } catch (e: Exception) {
            resultsTextArea.text = "Error querying database: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun extractFiltersFromMainView(): FilterParams {
        val mainView = this.mainView
        if (mainView == null) {
            println("WARNING: MainView not set, using empty filters")
            return FilterParams()
        }
        
        // Extract technology filters
        val technologyCheckboxes = mainView.getTechnologyCheckboxes()
        val selectedTechnologies = technologyCheckboxes
            .filter { it.isSelected }
            .mapNotNull { checkbox ->
                when (checkbox.text) {
                    "2G" -> 2
                    "3G" -> 3
                    "4G" -> 4
                    "5G" -> 5
                    "NR-IoT" -> 10
                    else -> null
                }
            }
        
        // Extract operator filters  
        val operatorCheckboxes = mainView.getOperatorCheckboxes()
        val selectedOperators = operatorCheckboxes
            .filter { it.isSelected }
            .map { it.text }
        
        // Extract date filters
        val datePickers = mainView.getDatePickers()
        val fromDate = datePickers.getOrNull(0)?.value?.toString()
        val toDate = datePickers.getOrNull(1)?.value?.toString()
        
        println("Extracted filters:")
        println("  Technologies: $selectedTechnologies")
        println("  Operators: $selectedOperators")
        println("  Date range: $fromDate to $toDate")
        
        return FilterParams(
            technologies = selectedTechnologies,
            operators = selectedOperators,
            startDate = fromDate,
            endDate = toDate
        )
    }

    private fun formatResults(
        neighbors: List<com.geocell.desktopanalyst.model.domain.Cell>,
        referenceCgi: String,
        radius: Double,
        filters: FilterParams
    ): String {
        val header = "Neighbor cells for CGI: $referenceCgi\n" +
                     "Search radius: $radius km\n" +
                     formatFiltersInfo(filters) +
                     "Found ${neighbors.size} neighbor(s)\n" +
                     "=".repeat(60) + "\n"
        
        if (neighbors.isEmpty()) {
            return header + "No neighbors found within the specified radius and filters."
        }
        
        val results = neighbors.mapIndexed { index, cell ->
            "${index + 1}. CGI: ${cell.cgi ?: "N/A"}\n" +
            "   Technology: ${getTechnologyName(cell.technology)}\n" +
            "   Operator: ${cell.mccMnc?.brand ?: "N/A"}\n" +
            "   Name: ${cell.name ?: "N/A"}\n" +
            "   LAC/TAC: ${cell.lacTac}\n" +
            "   eCI/nCI: ${cell.eciNci ?: "N/A"}\n" +
            "   Band: ${cell.band?.band ?: "N/A"}\n" +
            "   Location: ${formatLocation(cell.location)}\n"
        }.joinToString("\n")
        
        return header + results
    }

    private fun formatFiltersInfo(filters: FilterParams): String {
        val info = StringBuilder()
        
        if (filters.technologies.isNotEmpty()) {
            val techNames = filters.technologies.map { getTechnologyName(it) }
            info.append("Technologies: ${techNames.joinToString(", ")}\n")
        }
        
        if (filters.operators.isNotEmpty()) {
            info.append("Operators: ${filters.operators.joinToString(", ")}\n")
        }
        
        if (filters.sameNetwork) {
            info.append("Same network only: Yes\n")
        }
        
        if (filters.startDate != null || filters.endDate != null) {
            info.append("Date range: ${filters.startDate ?: "Any"} to ${filters.endDate ?: "Any"}\n")
        }
        
        return info.toString()
    }

    private fun getTechnologyName(technologyCode: Int): String {
        return when (technologyCode) {
            2 -> "2G"
            3 -> "3G"
            4 -> "4G"
            5 -> "5G"
            10 -> "NR-IoT"
            else -> "Unknown ($technologyCode)"
        }
    }

    private fun formatLocation(location: com.geocell.desktopanalyst.model.domain.Location?): String {
        if (location == null) return "No location data"
        
        val coords = location.coordinates
        val address = location.address
        val postal = location.postalDesignation
        
        return if (coords != null) {
            val coordStr = "(${coords.y}, ${coords.x})"
            val addressStr = if (address != null) "$address, " else ""
            val postalStr = if (postal != null) postal else ""
            "$addressStr$postalStr $coordStr"
        } else {
            "No coordinates"
        }
    }

    // Public methods
    fun setController(controller: com.geocell.desktopanalyst.controller.MainController) {
        this.controller = controller
    }
    
    fun setMainView(mainView: MainView) {
        this.mainView = mainView
    }

    fun setResults(content: String) {
        resultsTextArea.text = content
    }

    fun getCgiTextField(): TextField = cgiTextField
    fun getRadiusTextField(): TextField = radiusTextField
    fun getQueryButton(): Button = queryButton
    fun getResultsTextArea(): TextArea = resultsTextArea
}

// Data class to hold filter parameters
data class FilterParams(
    val technologies: List<Int> = emptyList(),
    val operators: List<String> = emptyList(),
    val sameNetwork: Boolean = false,
    val startDate: String? = null,
    val endDate: String? = null
)