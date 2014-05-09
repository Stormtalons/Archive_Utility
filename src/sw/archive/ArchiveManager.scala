package sw.archive

import java.nio.file.Path
import javafx.scene.layout.VBox
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.geometry.Insets
import scala.util.Random
import javafx.application.Platform

/*
	Represents the panel that houses the list of defined archive locations.
	This class exists primarily to encapsulate the behavior behind archive selection.
 */

class ArchiveManager extends VBox
{
	setPadding(new Insets(10))
	setSpacing(10)

	def count: Int = getChildren.size

//Custom foreach function to allow Scala-style use on JavaFX ObservableLists.
	def foreach(doWith: Archive => Unit) = for (i <- 0 to count - 1) doWith(get(i))

//Adds an archive to its list. Separated from createArchive to allow for loading from file.
	def add(archive: Archive) = Main.fx(getChildren.add(archive))

//Creates a new archive from a given file path.
	def createArchive(path: Path, name: String = "New Archive") = add(new Archive(path, name))

	def get(i: Int): Archive = if (i < count) getChildren.get(i).asInstanceOf[Archive] else null
	def get(name: String): Archive =
	{
		foreach((archive: Archive) => if (archive.toString.equals(name)) return archive)
		null
	}

//This routine facilitates the selection and return of an archive from its
//list when requested by a MonitoredGroup.
	def choose(doWith: (Archive) => Unit, shouldContinueWaiting: => Boolean) =
	{
		var waitingForSelection = true
		var selection: Archive = null

		//Provide new CSS settings to signify selection mode.
		foreach((archive: Archive) =>
		{
			archive.setOnMouseClicked(new EventHandler[MouseEvent]
			{
				def handle(evt: MouseEvent) =
				{
					//Select an archive and stop waiting.
					selection = archive
					waitingForSelection = false
				}
			})
			archive.getStyleClass.add("archiveHover")
		})

		//Spawn a new thread to wait for selection to end
		//before returning the selected archive.
		Main.run(
		{
			//Allow the selection period to be halted both by the user picking an archive
			//or any other exit conditions defined by the calling context.
			while (waitingForSelection && shouldContinueWaiting)
				Thread.sleep(10)

			//Revert CSS styling to signify the end of selection mode.
			foreach((archive: Archive) =>
			{
				Main.fx(
				{
					archive.setOnMouseClicked(null)
					archive.getStyleClass.removeAll("archiveHover")
				})
			})

			//Perform whatever action the calling context has designated for the selected archive.
			if (selection != null)
				doWith(selection)
		})
	}
}
