package sw.archive

import java.nio.file._
import javafx.scene.layout.{StackPane, GridPane}
import javafx.scene.control.{Label, TextField}
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.input.MouseEvent

class Archive(p: Path = null) extends GridPane
{
	private val archiveTitle: StackPane = new StackPane
	private def toggleLabelEdit =
	{
		if (archiveTitleLabel.isVisible)
			archiveTitleEdit.setText(archiveTitleLabel.getText)
		else
			archiveTitleLabel.setText(archiveTitleEdit.getText)
		archiveTitleLabel.setVisible(!archiveTitleLabel.isVisible)
		archiveTitleEdit.setVisible(!archiveTitleEdit.isVisible)
	}
	private val archiveTitleLabel: Label = new Label("New Archive")
	archiveTitleLabel.setOnMouseClicked(new EventHandler[MouseEvent] {def handle(evt: MouseEvent) = toggleLabelEdit})
	private val archiveTitleEdit: TextField = new TextField
	archiveTitleEdit.setOnAction(new EventHandler[ActionEvent] {def handle(evt: ActionEvent) = toggleLabelEdit})
	archiveTitleEdit.setVisible(false)
	archiveTitle.getChildren.addAll(archiveTitleLabel, archiveTitleEdit)
	private val archiveRoot: TextField = new TextField(if (p == null) "" else formatDir(p.toString))
	archiveRoot.setPrefWidth(700)
	add(archiveTitle, 0, 0)
	add(archiveRoot, 0, 1)

	def setArchivePath(p: Path) =
	{
		if (!Files.exists(p))
			Files.createDirectories(p)
		archiveRoot.setText(formatDir(p.toString))
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
