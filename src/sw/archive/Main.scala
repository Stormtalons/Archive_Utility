package sw.archive

import java.nio.file.Paths
import javafx.application.Application
import javafx.stage.Stage
import javafx.scene.control.{Button, ListView, Tab, TabPane}
import javafx.scene.layout.{Priority, HBox}
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.Scene

object Main extends App {new Main().launch}
class Main extends Application
{
	def launch = javafx.application.Application.launch()

	var archives: ArchiveManager = null
	var temp: MonitoredGroup = null
	val sb = new StringBuilder

	var mainPanel: TabPane = null
	var monitoredTab: Tab = null
	var hb: HBox = null
	var list: ListView[String] = null
	var b: Button = null
	var archiveTab: Tab = null

	def start(stg: Stage) =
	{
		initData
		initUI
		stg.setScene(new Scene(mainPanel, 1200, 1000))
		stg.show
	}

	def initData =
	{
		archives = new ArchiveManager

		temp = new MonitoredGroup
		temp.include(Paths.get("K:/Temp/Inspector Gadget 1 & 2 [DVDRip]"))
		temp.archive = archives.createArchive(Paths.get("D:/Code/Java/IntelliJ/Archive Utility/TestArchiveLocation/"))
		temp.displayAll(sb)
	}

	def initUI =
	{
		monitoredTab = new Tab("Monitored Files")
		list = new ListView[String]
		HBox.setHgrow(list, Priority.ALWAYS)
		for (s <- sb.toString.split("\n"))
			list.getItems.add(s)
		b = new Button("Go")
		b.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = if (list.getSelectionModel.getSelectedItem != null) temp.archive(list.getSelectionModel.getSelectedItem)})
		b.setStyle("-fx-font-size: 30pt")
		hb = new HBox
		hb.getChildren.addAll(list, b)
		monitoredTab.setContent(hb)

		archiveTab = new Tab("Archive Settings")
		archiveTab.setContent(archives)

		mainPanel = new TabPane
		mainPanel.getTabs.addAll(monitoredTab, archiveTab)
	}
}