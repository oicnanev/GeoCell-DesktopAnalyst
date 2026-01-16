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

/**
 * Tab for querying cells within a geographical area (circle or rectangle).
 *
 * This tab allows users to:
 * - Search cells within a circular area (center point + radius)
 * - Search cells within a rectangular area (two corner points)
 * - Display results in a text area
 *
 * @see MainController
 * @since 1.0.0
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

        // TODO: Implement actual database query for circle area
        resultsTextArea.text = "Querying circle area:\n" +
                              "Center: ($lat, $lon)\n" +
                              "Radius: $radius km\n\n" +
                              "This feature is under development."
        
        println("Querying circle - Center: ($lat, $lon), Radius: $radius km")
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

        // TODO: Implement actual database query for rectangle area
        resultsTextArea.text = "Querying rectangle area:\n" +
                              "Corner 1: ($lat1, $lon1)\n" +
                              "Corner 2: ($lat2, $lon2)\n\n" +
                              "This feature is under development."
        
        println("Querying rectangle - Corner1: ($lat1, $lon1), Corner2: ($lat2, $lon2)")
    }

    // Public methods for integration with controller
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
}