package sw.archive

import javafx.application.{Platform, Application}
import javafx.stage.{WindowEvent, Stage}
import javafx.scene.control.{Button, ScrollPane, Tab, TabPane}
import javafx.scene.layout.{Priority, HBox, VBox}
import javafx.scene.Scene
import javafx.event.{ActionEvent, EventHandler}
import java.nio.file.{StandardOpenOption, OpenOption, Files, Paths}

object Main extends App
{
	new Main().launch
	def run(code: => Unit) = new Thread(new Runnable {def run = code}).start
	def fx(code: => Unit) = if (Platform.isFxApplicationThread) code else Platform.runLater(new Runnable{def run = code})
	def tryGet[Any](code: => Any, default: Any): Any = try{code}catch{case _: Throwable => default}
	def formatFilePath(dir: String): String =
	{
		var toReturn = dir.replaceAll("\\\\", "/")
		if (!toReturn.endsWith("/") && Files.isDirectory(Paths.get(toReturn)))
			toReturn += "/"
		toReturn
	}
}
class Main extends Application
{
	def launch = javafx.application.Application.launch()

	var contentPane: VBox = null
	
	var toolBar: HBox = null
	var addGroup: Button = null
	var toggleRunning: Button = null
	
	var tabPane: TabPane = null

	var monitoredTab: Tab = null
	var scrollPane: ScrollPane = null
	var groups: GroupManager = null

	var archiveTab: Tab = null
	var archives: ArchiveManager = null

	def selectArchive(group: MonitoredGroup) =
	{
		tabPane.getSelectionModel.select(archiveTab)
		archives.choose((result: Archive) =>
		{
			group.setArchive(result)
			Main.fx(tabPane.getSelectionModel.select(monitoredTab))
		}, tabPane.getSelectionModel.isSelected(1))
	}

	def start(stg: Stage) =
	{
		contentPane = new VBox
		contentPane.setStyle("-fx-font-size: 16pt")
		
		toolBar = new HBox
		addGroup = new Button("Add New Group")
		addGroup.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = groups.add(new MonitoredGroup((group: MonitoredGroup) => selectArchive(group)))})
		toggleRunning = new Button("Enable Activity Loop")
		toggleRunning.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = toggleActivity})
		toolBar.getChildren.addAll(addGroup, toggleRunning)

		tabPane = new TabPane
		VBox.setVgrow(tabPane, Priority.ALWAYS)
		archiveTab = new Tab("Archive Settings")
		archives = new ArchiveManager
		archiveTab.setContent(archives)
		monitoredTab = new Tab("Monitored Files")
		scrollPane = new ScrollPane
		scrollPane.setFitToWidth(true)
		scrollPane.setFitToHeight(true)
		groups = new GroupManager
		scrollPane.setContent(groups)
		monitoredTab.setContent(scrollPane)
		tabPane.getTabs.addAll(monitoredTab, archiveTab)
		
		contentPane.getChildren.addAll(toolBar, tabPane)
		
		stg.addEventHandler(WindowEvent.WINDOW_HIDING, new EventHandler[WindowEvent]{def handle(evt: WindowEvent) = quit})
		stg.setScene(new Scene(contentPane, 1200, 1000))
		stg.show

		load
	}

	var running = false
	def toggleActivity =
	{
		toggleRunning.setText((if (running) "Enable" else "Disable") + " Activity Loop")
		running = !running
		Main.run(while (running)
		{
			groups.archiveAll
			Thread.sleep(1000)
		})
	}

	def quit =
	{
		running = false
		save
	}

	def save =
	{
		val toWrite = new StringBuilder
		archives.foreach((a: Archive) => toWrite.append(a.toXML))
		groups.foreach((g: MonitoredGroup) => toWrite.append(g.toXML(g.getXML, "")))
		Files.write(Paths.get("settings.xml"), toWrite.substring(0, toWrite.length - 2).getBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
	}

	def load =
	{
		val xmlblock = new XMLBlock(Files.readAllLines(Paths.get("settings.xml")).toArray[String](Array[String]()))
		var index = 0
		while (index < xmlblock.lines.length)
		{
			val temp = xmlblock.getBlock(index)
			if (temp.getBlockTag == "MonitoredGroup")
				groups.add(MonitoredGroup.fromXML(temp, n => archives.get(n), group => selectArchive(group)))
			else if (temp.getBlockTag == "Archive")
				archives.add(Archive.fromXML(temp.getLine(0)))
			index += temp.lines.length
		}
		groups.refreshAll
	}
}