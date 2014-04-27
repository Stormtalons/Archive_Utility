package sw.archive

import java.nio.file.Path
import javafx.scene.layout.VBox
import javafx.event.EventHandler
import javafx.scene.input.MouseEvent
import javafx.geometry.Insets
import scala.util.Random
import javafx.application.Platform

class ArchiveManager extends VBox
{
	setPadding(new Insets(10))
	setSpacing(10)

	def count: Int = getChildren.size

	def foreach(code: Archive => Unit) = for (i <- 0 to count - 1) code(get(i))

	def add(a: Archive) = Main.fx(getChildren.add(a))
	def createArchive(p: Path, n: String = "New Archive") = add(new Archive(p, n))
	def get(i: Int): Archive = if (i < count) getChildren.get(i).asInstanceOf[Archive] else null
	def get(n: String): Archive =
	{
		foreach((a: Archive) => if (a.toString.equals(n)) return a)
		null
	}

	def choose(doWith: (Archive) => Unit, checkStatus: => Boolean) =
	{
		var loop = true
		var result: Archive = null
		foreach((a: Archive) =>
		{
			a.setOnMouseClicked(new EventHandler[MouseEvent]
			{
				def handle(evt: MouseEvent) =
				{
					result = a
					loop = false
				}
			})
			a.getStyleClass.add("archiveHover")
		})
		Main.run(
		{
			while (loop && checkStatus)
				Thread.sleep(10)
			foreach((a: Archive) =>
			{
				Main.fx(
				{
					a.setOnMouseClicked(null)
					a.getStyleClass.removeAll("archiveHover")
				})
			})
			if (result != null)
				doWith(result)
		})
	}
}
