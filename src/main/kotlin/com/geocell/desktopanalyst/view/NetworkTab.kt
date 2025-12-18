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
 * Tab for querying cells by network attributes (LAC/TAC, eNB/gNB, Band).
 *
 * This tab allows users to:
 * - Search cells by LAC/TAC (Location Area Code / Tracking Area Code)
 * - Search cells by eNB/gNB identifier
 * - Search cells by frequency band
 * - Display results in a text area
 *
 * @see MainController
 * @since 1.0.0
 */
class NetworkTab : BorderPane() {

    // UI Components
    private val titleText = Text("Cells by network attributes")
    
    // Input fields
    private val lacTacTextField = TextField().apply {
        promptText = "LAC / TAC"
        prefWidth = 114.0
    }
    private val enbGnbTextField = TextField().apply {
        promptText = "eNB / gNB"
        prefWidth = 114.0
    }
    private val bandTextField = TextField().apply {
        promptText = "Band"
        prefWidth = 116.0
    }
    
    // Buttons
    private val queryLacTacButton = Button("Query by LAC/TAC").apply {
        prefHeight = 26.0
        prefWidth = 120.0
    }
    private val queryEnbGnbButton = Button("Query by eNB/gNB").apply {
        prefHeight = 26.0
        prefWidth = 120.0
    }
    private val queryBandButton = Button("Query by Band").apply {
        prefHeight = 26.0
        prefWidth = 100.0
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

        // Center panel with input fields and results
        val centerPanel = HBox(20.0).apply {
            // Left side - input fields
            val inputPanel = VBox(15.0).apply {
                children.addAll(
                    GridPane().apply {
                        hgap = 10.0
                        vgap = 10.0
                        add(Label("LAC/TAC:"), 0, 0)
                        add(lacTacTextField, 1, 0)
                        add(queryLacTacButton, 1, 1)
                        
                        add(Label("eNB/gNB:"), 0, 2)
                        add(enbGnbTextField, 1, 2)
                        add(queryEnbGnbButton, 1, 3)
                        
                        add(Label("Band:"), 0, 4)
                        add(bandTextField, 1, 4)
                        add(queryBandButton, 1, 5)
                    }
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
        titleText.wrappingWidth = 363.0
        resultsTextArea.style = "-fx-background-color: #f5f5f5; -fx-border-color: #cccccc;"
    }

    private fun setupEventHandlers() {
        queryLacTacButton.setOnAction {
            queryByLacTac()
        }
        
        queryEnbGnbButton.setOnAction {
            queryByEnbGnb()
        }
        
        queryBandButton.setOnAction {
            queryByBand()
        }
        
        // Allow Enter key to trigger queries
        lacTacTextField.setOnAction { queryByLacTac() }
        enbGnbTextField.setOnAction { queryByEnbGnb() }
        bandTextField.setOnAction { queryByBand() }
    }

    fun queryByLacTac() {
        val lacTac = lacTacTextField.text.trim()

        if (lacTac.isEmpty()) {
            resultsTextArea.text = "Error: Please enter a LAC/TAC"
            return
        }

        // TODO: Implement actual database query by LAC/TAC
        resultsTextArea.text = "Querying by LAC/TAC:\n" +
                              "LAC/TAC: $lacTac\n\n" +
                              "This feature is under development."
        
        println("Querying by LAC/TAC: $lacTac")
    }

    fun queryByEnbGnb() {
        val enbGnb = enbGnbTextField.text.trim()

        if (enbGnb.isEmpty()) {
            resultsTextArea.text = "Error: Please enter an eNB/gNB identifier"
            return
        }

        // Validate it's a number
        val enbGnbId = try {
            enbGnb.toLong()
        } catch (e: NumberFormatException) {
            resultsTextArea.text = "Error: eNB/gNB must be a valid number"
            return
        }

        if (enbGnbId <= 0) {
            resultsTextArea.text = "Error: eNB/gNB must be greater than 0"
            return
        }

        // TODO: Implement actual database query by eNB/gNB
        resultsTextArea.text = "Querying by eNB/gNB:\n" +
                              "eNB/gNB ID: $enbGnbId\n\n" +
                              "This feature is under development."
        
        println("Querying by eNB/gNB: $enbGnbId")
    }

    fun queryByBand() {
        val band = bandTextField.text.trim()

        if (band.isEmpty()) {
            resultsTextArea.text = "Error: Please enter a band"
            return
        }

        // TODO: Implement actual database query by band
        resultsTextArea.text = "Querying by Band:\n" +
                              "Band: $band\n\n" +
                              "This feature is under development."
        
        println("Querying by Band: $band")
    }

    // Public methods for integration with controller
    fun setResults(content: String) {
        resultsTextArea.text = content
    }

    fun getLacTacTextField(): TextField = lacTacTextField
    fun getEnbGnbTextField(): TextField = enbGnbTextField
    fun getBandTextField(): TextField = bandTextField
    fun getQueryLacTacButton(): Button = queryLacTacButton
    fun getQueryEnbGnbButton(): Button = queryEnbGnbButton
    fun getQueryBandButton(): Button = queryBandButton
    fun getResultsTextArea(): TextArea = resultsTextArea
}