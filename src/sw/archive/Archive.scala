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
	def getName: String = name.get
	add(name, 0, 0)

	private val path: Setting = new Setting(Setting.LABEL_AND_FIELD, "Path", if (p == null) "" else Main.formatFilePath(p.toString))
	path.setPrefWidth(700)
	def getPath: String = path.get
	add(path, 0, 1)

	def setArchivePath(p: Path) = Main.fx(path.set(Main.formatFilePath(p.toString)))

	def archive(from: MonitoredItem) =
		if (Files.isRegularFile(from.getFile) && !from.isExcluded && !from.getParentalExclusion)
		{
			val archivePath = getPath + Main.formatFilePath(from.getRelativePath)
			if (!Files.exists(Paths.get(archivePath)))
				Files.createDirectories(Paths.get(archivePath))
			Files.copy(from.getFile, Paths.get(archivePath + from.getFileName), StandardCopyOption.REPLACE_EXISTING)
		}

	def toXML: String = "<Archive name=\"" + getName + "\" path=\"" + getPath + "\" />\r\n"
	override def toString: String = getName
}
