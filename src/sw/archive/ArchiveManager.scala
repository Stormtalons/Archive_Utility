package sw.archive

import java.nio.file.Path
import javafx.scene.layout.VBox
import javafx.event.{EventHandler, ActionEvent}
import javafx.scene.input.MouseEvent
import javafx.geometry.Insets

class ArchiveManager extends VBox
{
	setPadding(new Insets(10))
	setSpacing(10)

	def createArchive(p: Path, n: String = "New Archive"): Archive =
	{
		for (i <- 0 to getChildren.size - 1)
			if (getChildren.get(i).asInstanceOf[Archive].refersTo(p))
				return getChildren.get(i).asInstanceOf[Archive]
		implicit val name = n
		val toAdd = new Archive(p)
		Main.fx(getChildren.add(toAdd))
		toAdd
	}

	def choose(doWith: (Archive) => Unit, checkStatus: => Boolean) =
	{
		var loop = true
		var result: Archive = null
		for (a <- 0 to getChildren.size - 1)
		{
			val archive: Archive = getChildren.get(a).asInstanceOf[Archive]
			archive.setOnMouseClicked(new EventHandler[MouseEvent]
			{
				def handle(evt: MouseEvent) =
				{
					result = archive
					loop = false
				}
			})
			archive.getStyleClass.add("archiveHover")
		}
		Main.run(
		{
			while (loop && checkStatus)
				Thread.sleep(10)
			Main.fx(
			for (a <- 0 to getChildren.size - 1)
			{
				val archive: Archive = getChildren.get(a).asInstanceOf[Archive]
				archive.setOnMouseClicked(null)
				archive.getStyleClass.removeAll("archiveHover")
			})
			if (result != null)
				doWith(result)
		})
	}
}
