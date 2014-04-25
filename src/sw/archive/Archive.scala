package sw.archive

import java.nio.file._
import javafx.scene.layout.{StackPane, GridPane}
import javafx.scene.control.{Label, TextField}
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.input.MouseEvent
import javafx.geometry.Insets

class Archive(p: Path = null)(implicit n: String = "New Archive") extends GridPane
{
	setPadding(new Insets(10))
	setHgap(10)
	setVgap(10)
	getStyleClass.add("archive")
	getStylesheets.add("sw/archive/dft.css")
	val archiveTitle: Setting = new Setting(Setting.LABEL_AND_FIELD, "Name", n, null)
	add(archiveTitle, 0, 0)

	val archiveRoot: Setting = new Setting(Setting.LABEL_AND_FIELD, "Path", if (p == null) "" else formatDir(p.toString), null)
	archiveRoot.setPrefWidth(700)
	add(archiveRoot, 0, 1)

	def getName: String = archiveTitle.getValue

	def setArchivePath(p: Path) =
	{
		if (!Files.exists(p))
			Files.createDirectories(p)
		Main.fx(archiveRoot.setValue(formatDir(p.toString)))
	}
	
	def archive(from: Monitored)
	{
		val archivePath = archiveRoot.getValue + formatDir(from.relativePath)
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

	def refersTo(p: Path): Boolean = Files.isSameFile(p, Paths.get(archiveRoot.getValue))
}
