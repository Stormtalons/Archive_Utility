package sw.archive

import java.nio.file.Path
import javafx.scene.layout.VBox
import javafx.event.{EventHandler, ActionEvent}
import javafx.scene.input.MouseEvent

class ArchiveManager extends VBox
{
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

	def choose(doWith: (String) => Unit) =
	{
		var loop = true
		var result: String = ""
		for (a <- 0 to getChildren.size - 1)
		{
			val archive: Archive = getChildren.get(a).asInstanceOf[Archive]
			archive.setOnMouseClicked(new EventHandler[MouseEvent]
			{
				def handle(evt: MouseEvent) =
				{
					result = archive.archiveTitle.getValue
					loop = false
				}
			})
			archive.getStylesheets.add("sw/archive/dft.css")
		}
		Main.run(
		{
			while (loop)
				Thread.sleep(10)
			Main.fx(
			for (a <- 0 to getChildren.size - 1)
			{
				val archive: Archive = getChildren.get(a).asInstanceOf[Archive]
				archive.setOnMouseClicked(null)
				archive.getStylesheets.remove("sw/archive/dft.css")
			})
			doWith(result)
		})
	}
}
