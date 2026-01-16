package com.geocell.desktopanalyst.view

import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import com.geocell.desktopanalyst.model.FilterParams

/**
 * Tab for querying cells within a geographical area (circle or rectangle).
 */
class GeographicTab : BorderPane() {

    // UI Components
    private val titleText = Text("Cells in a geographical circle or rectangle")

    // Circle section
    private val circleLabel = Text("Circle:")
    private val circleLatTextField = TextField().apply {
        promptText = "Latitude"
        prefWidth = 114.0
    }
    private val circleLonTextField = TextField().apply {
        promptText = "Longitude"
        prefWidth = 114.0
    }
    private val circleRadiusTextField = TextField().apply {
        promptText = "radius (kms)"
        prefWidth = 116.0
    }

    // Rectangle section
    private val rectangleLabel = Text("Rectangle:")
    private val rectLat1TextField = TextField().apply {
        promptText = "Latitude 1"
        prefWidth = 114.0
    }
    private val rectLon1TextField = TextField().apply {
        promptText = "Longitude 1"
        prefWidth = 114.0
    }
    private val rectLat2TextField = TextField().apply {
        promptText = "Latitude 2"
        prefWidth = 114.0
    }
    private val rectLon2TextField = TextField().apply {
        promptText = "Longitude 2"
        prefWidth = 114.0
    }

    // Buttons
    private val queryCircleButton = Button("Query Circle").apply {
        prefHeight = 26.0
        prefWidth = 100.0
    }
    private val queryRectangleButton = Button("Query Rectangle").apply {
        prefHeight = 26.0
        prefWidth = 120.0
    }

    // Results
    private val resultsTextArea = TextArea().apply {
        prefHeight = 164.0
        prefWidth = 338.0
        isEditable = false
        style = "-fx-font-family: 'Monospaced';"
    }

    // Controller reference
    private var controller: com.geocell.desktopanalyst.controller.MainController? = null

    // Main view reference for accessing filters
    private var mainView: MainView? = null

    // Store last query results for export
    private var lastQueryResults: List<com.geocell.desktopanalyst.model.domain.Cell> = emptyList()

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

        // Center panel with input sections and results
        val centerPanel = HBox(20.0).apply {
            // Left side - input sections
            val inputPanel = VBox(15.0).apply {
                // Circle section
                val circleSection = VBox(5.0).apply {
                    children.addAll(
                        circleLabel,
                        GridPane().apply {
                            hgap = 10.0
                            vgap = 5.0
                            add(Label("Latitude:"), 0, 0)
                            add(circleLatTextField, 1, 0)
                            add(Label("Longitude:"), 0, 1)
                            add(circleLonTextField, 1, 1)
                            add(Label("Radius (km):"), 0, 2)
                            add(circleRadiusTextField, 1, 2)
                            add(queryCircleButton, 1, 3)
                        }
                    )
                }

                // Rectangle section
                val rectangleSection = VBox(5.0).apply {
                    children.addAll(
                        rectangleLabel,
                        GridPane().apply {
                            hgap = 10.0
                            vgap = 5.0
                            add(Label("Latitude 1:"), 0, 0)
                            add(rectLat1TextField, 1, 0)
                            add(Label("Longitude 1:"), 0, 1)
                            add(rectLon1TextField, 1, 1)
                            add(Label("Latitude 2:"), 0, 2)
                            add(rectLat2TextField, 1, 2)
                            add(Label("Longitude 2:"), 0, 3)
                            add(rectLon2TextField, 1, 3)
                            add(queryRectangleButton, 1, 4)
                        }
                    )
                }

                children.addAll(circleSection, rectangleSection)
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
        titleText.wrappingWidth = 363.0
        resultsTextArea.style = "-fx-background-color: #f5f5f5; -fx-border-color: #cccccc;"
    }

    private fun setupEventHandlers() {
        queryCircleButton.setOnAction {
            queryCircle()
        }

        queryRectangleButton.setOnAction {
            queryRectangle()
        }
    }

    fun queryCircle() {
        val latText = circleLatTextField.text.trim()
        val lonText = circleLonTextField.text.trim()
        val radiusText = circleRadiusTextField.text.trim()

        if (latText.isEmpty() || lonText.isEmpty() || radiusText.isEmpty()) {
            resultsTextArea.text = "Error: Please fill all circle fields"
            return
        }

        val lat = try {
            latText.toDouble()
        } catch (e: NumberFormatException) {
            resultsTextArea.text = "Error: Latitude must be a valid number"
            return
        }

        val lon = try {
            lonText.toDouble()
        } catch (e: NumberFormatException) {
            resultsTextArea.text = "Error: Longitude must be a valid number"
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

        // Validate latitude/longitude ranges
        if (lat < -90 || lat > 90) {
            resultsTextArea.text = "Error: Latitude must be between -90 and 90"
            return
        }

        if (lon < -180 || lon > 180) {
            resultsTextArea.text = "Error: Longitude must be between -180 and 180"
            return
        }

        // Check if controller is available
        if (controller == null) {
            resultsTextArea.text = "Error: Controller not initialized. Please restart the application."
            return
        }

        try {
            // Show processing message
            resultsTextArea.text = "Querying circle area:\n" +
                    "Center: ($lat, $lon)\n" +
                    "Radius: $radius km\n" +
                    "Processing..."

            // Extract filters from main UI
            val filters = extractFiltersFromMainView()

            // Query the database through controller
            val cells = controller!!.queryCellsInCircle(
                centerLat = lat,
                centerLon = lon,
                radiusKm = radius,
                filters = filters
            )

            // Store results for export
            lastQueryResults = cells

            // Format and display results
            resultsTextArea.text = formatResults(cells, "Circle", "Center: ($lat, $lon), Radius: $radius km", filters)

            println("Querying circle - Center: ($lat, $lon), Radius: $radius km")
            println("Filters applied: $filters")
            println("Found ${cells.size} cell(s)")

        } catch (e: IllegalArgumentException) {
            resultsTextArea.text = "Error: ${e.message}"
        } catch (e: Exception) {
            resultsTextArea.text = "Error querying database: ${e.message}"
            e.printStackTrace()
        }
    }

    fun queryRectangle() {
        val lat1Text = rectLat1TextField.text.trim()
        val lon1Text = rectLon1TextField.text.trim()
        val lat2Text = rectLat2TextField.text.trim()
        val lon2Text = rectLon2TextField.text.trim()

        if (lat1Text.isEmpty() || lon1Text.isEmpty() || lat2Text.isEmpty() || lon2Text.isEmpty()) {
            resultsTextArea.text = "Error: Please fill all rectangle fields"
            return
        }

        val lat1 = try {
            lat1Text.toDouble()
        } catch (e: NumberFormatException) {
            resultsTextArea.text = "Error: Latitude 1 must be a valid number"
            return
        }

        val lon1 = try {
            lon1Text.toDouble()
        } catch (e: NumberFormatException) {
            resultsTextArea.text = "Error: Longitude 1 must be a valid number"
            return
        }

        val lat2 = try {
            lat2Text.toDouble()
        } catch (e: NumberFormatException) {
            resultsTextArea.text = "Error: Latitude 2 must be a valid number"
            return
        }

        val lon2 = try {
            lon2Text.toDouble()
        } catch (e: NumberFormatException) {
            resultsTextArea.text = "Error: Longitude 2 must be a valid number"
            return
        }

        // Validate latitude/longitude ranges
        if (lat1 < -90 || lat1 > 90 || lat2 < -90 || lat2 > 90) {
            resultsTextArea.text = "Error: Latitudes must be between -90 and 90"
            return
        }

        if (lon1 < -180 || lon1 > 180 || lon2 < -180 || lon2 > 180) {
            resultsTextArea.text = "Error: Longitudes must be between -180 and 180"
            return
        }

        // Check if controller is available
        if (controller == null) {
            resultsTextArea.text = "Error: Controller not initialized. Please restart the application."
            return
        }

        try {
            // Show processing message
            resultsTextArea.text = "Querying rectangle area:\n" +
                    "Corner 1: ($lat1, $lon1)\n" +
                    "Corner 2: ($lat2, $lon2)\n" +
                    "Processing..."

            // Extract filters from main UI
            val filters = extractFiltersFromMainView()

            // Query the database through controller
            val cells = controller!!.queryCellsInRectangle(
                lat1 = lat1,
                lon1 = lon1,
                lat2 = lat2,
                lon2 = lon2,
                filters = filters
            )

            // Store results for export
            lastQueryResults = cells

            // Format and display results
            resultsTextArea.text = formatResults(cells, "Rectangle", "Corners: ($lat1, $lon1) to ($lat2, $lon2)", filters)

            println("Querying rectangle - Corner1: ($lat1, $lon1), Corner2: ($lat2, $lon2)")
            println("Filters applied: $filters")
            println("Found ${cells.size} cell(s)")

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
        cells: List<com.geocell.desktopanalyst.model.domain.Cell>,
        queryType: String,
        areaInfo: String,
        filters: FilterParams
    ): String {
        val header = "$queryType Query Results\n" +
                "Area: $areaInfo\n" +
                formatFiltersInfo(filters) +
                "Found ${cells.size} cell(s)\n" +
                "=".repeat(60) + "\n"

        if (cells.isEmpty()) {
            return header + "No cells found within the specified area and filters."
        }

        val results = cells.mapIndexed { index, cell ->
            "${index + 1}. CGI: ${cell.cgi ?: "N/A"}\n" +
                    "   Technology: ${getTechnologyName(cell.technology)}\n" +
                    "   Operator: ${cell.mccMnc?.operator ?: "N/A"}\n" +
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

    // Public methods for integration with controller and main view
    fun setController(controller: com.geocell.desktopanalyst.controller.MainController) {
        this.controller = controller
    }

    fun setMainView(mainView: MainView) {
        this.mainView = mainView
    }

    fun setResults(content: String) {
        resultsTextArea.text = content
    }

    fun getCircleLatTextField(): TextField = circleLatTextField
    fun getCircleLonTextField(): TextField = circleLonTextField
    fun getCircleRadiusTextField(): TextField = circleRadiusTextField
    fun getRectLat1TextField(): TextField = rectLat1TextField
    fun getRectLon1TextField(): TextField = rectLon1TextField
    fun getRectLat2TextField(): TextField = rectLat2TextField
    fun getRectLon2TextField(): TextField = rectLon2TextField
    fun getQueryCircleButton(): Button = queryCircleButton
    fun getQueryRectangleButton(): Button = queryRectangleButton
    fun getResultsTextArea(): TextArea = resultsTextArea
    fun getQueryCircle() = queryCircle()
    fun getLastQueryResults(): List<com.geocell.desktopanalyst.model.domain.Cell> = lastQueryResults
}