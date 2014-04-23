package sw.archive

import javafx.application.{Platform, Application}
import javafx.stage.Stage
import javafx.scene.control.{Button, ScrollPane, Tab, TabPane}
import javafx.scene.layout.{HBox, VBox}
import javafx.scene.Scene
import javafx.event.{ActionEvent, EventHandler}

object Main extends App
{
	new Main().launch
	def fx(code: Unit) = Platform.runLater(new Runnable{def run = code})
}
class Main extends Application
{
	def launch = javafx.application.Application.launch()

	var mainPanel: TabPane = null

	var monitoredTab: Tab = null
	var scrollPane: ScrollPane = null
	var monitoredGroups: VBox = null
	var addGroup: Button = null

	var archiveTab: Tab = null
	var archives: ArchiveManager = null

	def start(stg: Stage) =
	{
		monitoredTab = new Tab("Monitored Files")
		scrollPane = new ScrollPane
		monitoredGroups = new VBox
		addGroup = new Button("+")
		addGroup.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = monitoredGroups.getChildren.add(new MonitoredGroup("New Group"))})
		monitoredGroups.getChildren.add(addGroup)
		scrollPane.setContent(monitoredGroups)
		monitoredTab.setContent(scrollPane)

		archiveTab = new Tab("Archive Settings")
		archives = new ArchiveManager
		archiveTab.setContent(archives)

		mainPanel = new TabPane
		mainPanel.getTabs.addAll(monitoredTab, archiveTab)
		stg.setScene(new Scene(mainPanel, 1200, 1000))
		stg.show
	}
}