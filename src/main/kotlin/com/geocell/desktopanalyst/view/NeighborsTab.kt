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
 *
 * This tab allows users to:
 * - Enter a CGI (Cell Global Identity) to search for
 * - Specify a search radius in kilometers
 * - Query the database for neighboring cells
 * - Display results in a text area
 *
 * @see MainController
 * @since 1.0.0
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

        // TODO: Implement actual database query for neighbors
        // This should call a method in MainController to query neighbors
        resultsTextArea.text = "Querying neighbors for CGI: $cgi\nRadius: $radius km\n\nThis feature is under development."

        println("Querying neighbors - CGI: $cgi, Radius: $radius km")

        // Example of what the controller method might look like:
        // controller.queryNeighbors(cgi, radius) { results ->
        //     resultsTextArea.text = formatResults(results)
        // }
    }

    private fun formatResults(results: List<String>): String {
        return if (results.isEmpty()) {
            "No neighbors found within the specified radius."
        } else {
            results.joinToString("\n")
        }
    }

    // Public methods for integration with controller
    fun setResults(content: String) {
        resultsTextArea.text = content
    }

    fun getCgiTextField(): TextField = cgiTextField
    fun getRadiusTextField(): TextField = radiusTextField
    fun getQueryButton(): Button = queryButton
    fun getResultsTextArea(): TextArea = resultsTextArea
}
