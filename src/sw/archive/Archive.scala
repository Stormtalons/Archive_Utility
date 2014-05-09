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
	getStylesheets.add("sw/archive/res/dft.css")
	private val name: Setting = new Setting(Setting.LABEL_AND_FIELD, "Name", n)
	add(name, 0, 0)

	private val path: Setting = new Setting(Setting.LABEL_AND_FIELD, "Path", if (p == null) "" else formatDir(p.toString))
	path.setPrefWidth(700)
	add(path, 0, 1)

	def setArchivePath(p: Path) = Main.fx(path.set(formatDir(p.toString)))

	def archive(from: MonitoredItem) =
		if (Files.isRegularFile(from.getFile) && !from.isExcluded && !from.getParentalExclusion)
		{
			val archivePath = path.get + formatDir(from.getRelativePath)
			if (!Files.exists(Paths.get(archivePath)))
				Files.createDirectories(Paths.get(archivePath))
			Files.copy(from.getFile, Paths.get(archivePath + from.getFile.getFileName), StandardCopyOption.REPLACE_EXISTING)
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
