package sw.archive

import java.nio.file.Path
import javafx.scene.layout.VBox

class ArchiveManager extends VBox
{
	def createArchive(p: Path): Archive =
	{
		for (i <- 0 to getChildren.size - 1)
			if (getChildren.get(i).asInstanceOf[Archive].refersTo(p))
				return getChildren.get(i).asInstanceOf[Archive]
		val toAdd = new Archive(p)
		Main.fx(getChildren.add(toAdd))
		toAdd
	}
}
