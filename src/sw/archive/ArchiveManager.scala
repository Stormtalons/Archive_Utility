package sw.archive

import java.nio.file.Path
import javafx.scene.layout.VBox
import javafx.application.Platform

class ArchiveManager extends VBox
{
//	var archiveLocations: Array[Archive] = Array()

	def createArchive(p: Path): Archive =
	{
		for (i <- 0 to getChildren.size - 1)
			if (getChildren.get(i).asInstanceOf[Archive].refersTo(p))
				return getChildren.get(i).asInstanceOf[Archive]
//		for (a <- archiveLocations)
//			if (a.refersTo(p))
//				return a
		val toAdd = new Archive(p)
		Platform.runLater(new Runnable {def run = getChildren.add(toAdd)})
//		archiveLocations = archiveLocations :+ toAdd

		toAdd
	}
}
