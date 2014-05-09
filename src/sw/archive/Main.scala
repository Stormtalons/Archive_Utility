package sw.archive

import javafx.application.{Platform, Application}
import javafx.stage.{WindowEvent, Stage}
import javafx.scene.control.{Button, ScrollPane, Tab, TabPane}
import javafx.scene.layout.{Priority, HBox, VBox}
import javafx.scene.Scene
import javafx.event.{ActionEvent, EventHandler}
import java.nio.file.{StandardOpenOption, Files, Paths}

/*
	Application entry point. This class defines and manages the UI, is responsible for
	any functionality used globally, and performs all thread management.
 */

object Main extends App
{
	new Main().launch

//Provides a simple interface for asynchronously executing any section(s) of code.
//TODO: Learn how to replace this with Akka Actors.
	def run(code: => Unit) = new Thread(new Runnable {def run = code}).start

//Provides an easy interface for modifying UI elements, ensuring that all such
//modification is done on the JavaFX Application thread.
	def fx(code: => Unit) = if (Platform.isFxApplicationThread) code else Platform.runLater(new Runnable{def run = code})

//Provides a mechanism for safely attempting to get a value from any section of code,
//and allows the calling context to specify a desired default value should the code fail.
	def tryGet[Any](code: => Any, default: Any): Any = try{code}catch{case _: Throwable => default}

//Ensures global consistency in file path formatting.
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

//Code for selecting an archive, and assigning the selection to whichever
//MonitoredGroup called it. It is used during group creation. This resolves
//the difference in scope between the ArchiveManager and the GroupManager
//while still providing loose coupling.
	def selectArchive(group: MonitoredGroup) =
	{
		tabPane.getSelectionModel.select(archiveTab)
		archives.choose(
			//What should be done with the selected archive.
			(result: Archive) =>
			{
				group.setArchive(result)
				Main.fx(tabPane.getSelectionModel.select(monitoredTab))
			},
			//Cancel selection if the tab is changed before selection occurs.
			tabPane.getSelectionModel.isSelected(1))
	}

	def start(stg: Stage) =
	{
	//UI creation
		contentPane = new VBox
		contentPane.setStyle("-fx-font-size: 16pt; -fx-font-family: Verdana")

	//Misc. tools group. These are designed to be temporary - any functionality
	//desired post-development should be moved to a more natural location.
		toolBar = new HBox
		addGroup = new Button("Add New Group")
		addGroup.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = groups.add(new MonitoredGroup((group: MonitoredGroup) => selectArchive(group)))})
		toggleRunning = new Button("Enable Activity Loop")
		toggleRunning.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = toggleActivity})
		toolBar.getChildren.addAll(addGroup, toggleRunning)
	//End misc. tools

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
	//End UI creation

	//On window close, save current status before exiting.
		stg.addEventHandler(WindowEvent.WINDOW_HIDING, new EventHandler[WindowEvent]{def handle(evt: WindowEvent) = quit})
		stg.setScene(new Scene(contentPane, 1200, 1000))
		stg.show

	//Load previous settings from file. This is done after the stage is
	//shown to prevent any issues that might occur from attempting to add
	//UI elements to a UI that doesn't exist yet.
		load
	}

//Main activity loop. Responsible for initiating archival on schedule for all groups.
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

//Saves all settings to file.
	def save =
	{
		val toWrite = new StringBuilder
		archives.foreach((archive: Archive) => toWrite.append(archive.toXML))
		groups.toXML(toWrite)
		Files.write(Paths.get("settings.xml"), toWrite.substring(0, toWrite.length - 2).getBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
	}

//Load all settings from file.
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

		//After load, ensure that all monitored items are displayed properly.
		groups.refreshAll
	}
}