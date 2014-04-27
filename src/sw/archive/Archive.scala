package sw.archive

import java.nio.file._
import javafx.scene.layout.GridPane
import javafx.geometry.Insets

object Archive
{
	def fromXML(l: XMLLine): Archive = new Archive(Paths.get(l.getAttr("path")), l.getAttr("name"))
}

class Archive(p: Path, n: String) extends GridPane
{
	setPadding(new Insets(10))
	setHgap(10)
	setVgap(10)
	getStyleClass.add("archive")
	getStylesheets.add("sw/archive/dft.css")
	val name: Setting[String] = new Setting(Setting.LABEL_AND_FIELD, "Name", n)
	add(name, 0, 0)

	val path: Setting[String] = new Setting(Setting.LABEL_AND_FIELD, "Path", if (p == null) "" else formatDir(p.toString))
	path.setPrefWidth(700)
	add(path, 0, 1)

	def setArchivePath(p: Path) =
	{
		if (!Files.exists(p))
			Files.createDirectories(p)
		Main.fx(path.set(formatDir(p.toString)))
	}
	
	def archive(from: Monitored)
	{
		if (Files.isRegularFile(from.file))
		{
			val archivePath = path.get + formatDir(from.relativePath)
			if (!Files.exists(Paths.get(archivePath)))
				Files.createDirectories(Paths.get(archivePath))
			Files.copy(from.file, Paths.get(archivePath + from.file.getFileName), StandardCopyOption.REPLACE_EXISTING)
		}
	}
	
	def formatDir(dir: String): String =
	{
		var toReturn = dir.replaceAll("\\\\", "/")
		if (!toReturn.endsWith("/"))
			toReturn += "/"
		toReturn
	}

	def refersTo(p: Path): Boolean = Files.isSameFile(p, Paths.get(path.get))

	def toXML: String = "<Archive name=\"" + name.get + "\" path=\"" + path.get + "\" />\r\n"
	override def toString: String = name.get
}
