package sw.archive

import java.nio.file._
import javafx.scene.layout.GridPane
import javafx.scene.control.TextField

class Archive(p: Path = null) extends GridPane
{
	private var archiveRoot: TextField = new TextField(if (p == null) "" else p.toString)
	add(archiveRoot, 0, 0)

	def setArchivePath(p: Path) =
	{
		if (!Files.exists(p))
			Files.createDirectories(p)
		archiveRoot = formatDir(p.toString)
	}
	
	def archive(from: Monitored)
	{
		val archivePath = archiveRoot.getText + formatDir(from.relativePath)
		if (!Files.exists(Paths.get(archivePath)))
			Files.createDirectories(Paths.get(archivePath))
		Files.copy(from.file, Paths.get(archivePath + from.file.getFileName), StandardCopyOption.REPLACE_EXISTING)
	}
	
	def formatDir(dir: String): String =
	{
		var toReturn = dir.replaceAll("\\\\", "/")
		if (!toReturn.endsWith("/"))
			toReturn += "/"
		toReturn
	}

	def refersTo(p: Path): Boolean = Files.isSameFile(p, Paths.get(archiveRoot.getText))
}
