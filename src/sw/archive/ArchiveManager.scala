package sw.archive

import java.nio.file.Path
import javafx.scene.layout.VBox

class ArchiveManager extends VBox
{
	var archiveLocations: Array[Archive] = Array()

	def createArchive(p: Path): Archive =
	{
		for (a <- archiveLocations)
			if (a.refersTo(p))
				return a
		val toAdd = new Archive(p)
		archiveLocations = archiveLocations :+ toAdd
		toAdd
	}
}
