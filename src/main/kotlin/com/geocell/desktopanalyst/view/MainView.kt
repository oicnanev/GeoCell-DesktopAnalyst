package com.geocell.desktopanalyst.view

import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.DatePicker
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class MainView(): VBox() {
    // Main components
    //private val menuBar = createMenuBar()
    private val menuBar: MenuBar? = null
    private val tabPane = TabPane().apply {
        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
    }
    private val filterSection = FilterSection()

    init {
        setupLayout()
        setupTabs()
        setupStyles()
        setupTabChangeListener()
    }

    private fun setupLayout() {
        spacing = 10.0
        padding = Insets(10.0)

        if (menuBar != null) children.add(menuBar)
        children.addAll(tabPane, filterSection)
        setVgrow(tabPane, Priority.ALWAYS)
    }

    private fun setupTabs() {
        val timestampTab = Tab("Timestamp").apply {
            content = TimestampTab()
        }

        val neighborsTab = Tab("Neighbors").apply {
            content = NeighborsTab()
        }

        val geographicTab = Tab("Geographic").apply {
            content = GeographicTab()
        }

        val administrativeTab = Tab("Administrative").apply {
            content = AdministrativeTab()
        }

        val networkTab = Tab("Network").apply {
            content = NetworkTab()
        }

        tabPane.tabs.addAll(
            timestampTab,
            neighborsTab,
            geographicTab,
            administrativeTab,
            networkTab
        )
    }

    private fun createMenuBar(): MenuBar =
        MenuBar().apply {
            menus.addAll(
                Menu("File").apply {
                    items.addAll(
                        MenuItem("New"),
                        MenuItem("Open…"),
                        Menu("Open Recent"),
                        SeparatorMenuItem(),
                        MenuItem("Close"),
                        MenuItem("Save"),
                        MenuItem("Save As…"),
                        MenuItem("Revert"),
                        SeparatorMenuItem(),
                        MenuItem("Preferences…"),
                        SeparatorMenuItem(),
                        MenuItem("Quit")
                    )
                },
                Menu("Edit").apply {
                    items.addAll(
                        MenuItem("Undo"),
                        MenuItem("Redo"),
                        SeparatorMenuItem(),
                        MenuItem("Cut"),
                        MenuItem("Copy"),
                        MenuItem("Paste"),
                        MenuItem("Delete"),
                        SeparatorMenuItem(),
                        MenuItem("Select All"),
                        MenuItem("Unselect All")
                    )
                },
                Menu("Help").apply {
                    items.addAll(
                        MenuItem("About")
                    )
                }
            )
        }

    private fun setupStyles() {
        // To add styles
    }

    private fun setupTabChangeListener() {
        tabPane.selectionModel.selectedItemProperty().addListener { _, _, newTab ->
            updateFiltersBasedOnTab(newTab)
        }
        // Initialize filters for the first tab
        updateFiltersBasedOnTab(tabPane.selectionModel.selectedItem)
    }

    private fun updateFiltersBasedOnTab(selectedTab: Tab?) {
        val isTimestampTab = selectedTab?.text == "Timestamp"

        // Disable/enable technology and operator filters based on tab
        filterSection.getTechnologyCheckboxes().forEach { it.isDisable = isTimestampTab }
        filterSection.getOperatorCheckboxes().forEach { it.isDisable = isTimestampTab }

        // Clear selections when disabled
        if (isTimestampTab) {
            filterSection.getTechnologyCheckboxes().forEach { it.isSelected = false }
            filterSection.getOperatorCheckboxes().forEach { it.isSelected = false }
        }
    }

    // Public Methods to integrate with the Controller
    fun getTimestampTab(): TimestampTab {
        return tabPane.tabs[0].content as TimestampTab
    }

    fun getNeighborsTab(): NeighborsTab {
        return tabPane.tabs[1].content as NeighborsTab
    }

    fun getGeographicTab(): GeographicTab {
        return tabPane.tabs[2].content as GeographicTab
    }

    fun getAdministrativeTab(): AdministrativeTab {
        return tabPane.tabs[3].content as AdministrativeTab
    }

    fun getNetworkTab(): NetworkTab {
        return tabPane.tabs[4].content as NetworkTab
    }

    fun getTabPane(): TabPane = tabPane

    fun getTechnologyCheckboxes(): List<CheckBox> = filterSection.getTechnologyCheckboxes()

    fun getOperatorCheckboxes(): List<CheckBox> = filterSection.getOperatorCheckboxes()

    fun getDatePickers(): List<DatePicker> = filterSection.getDatePickers()

    fun getExportButton(): Button = filterSection.getExportButton()
}