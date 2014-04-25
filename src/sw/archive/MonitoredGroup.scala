package sw.archive

import java.nio.file.{Paths, Path}
import org.joda.time.DateTime
import javafx.scene.layout.VBox
import javafx.scene.control.{Button, TreeView, Label}
import javafx.geometry.Insets
import javafx.event.{EventHandler, ActionEvent}

class MonitoredGroup(ac: (MonitoredGroup) => Unit = null) extends VBox
{
	setPadding(new Insets(10))
	setSpacing(10)
	getStylesheets.add("sw/archive/dft.css")
	var name: Setting = new Setting(Setting.LABEL_AND_FIELD, "Name", "New Group", null)
	getChildren.add(name)
	var scanInterval: Setting = new Setting(Setting.LABEL_AND_FIELD, "Scan interval", null)
	getChildren.add(scanInterval)
	val archiveChooser: Button = new Button("Choose")
	archiveChooser.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = ac(MonitoredGroup.this)})
	var archive2: Setting = new Setting(Setting.LABEL_AND_FIELD, "Archive", Array[Button](archiveChooser))
	getChildren.add(archive2)
	var monitoredFiles: Array[Monitored] = Array()
	var filesLabel: Label = new Label("Monitored Folders:")
	getChildren.add(filesLabel)
	var filesTree: TreeView[String] = new TreeView
	filesTree.setStyle("-fx-border-width: 1px; -fx-border-style: solid")
	getChildren.add(filesTree)
	var archive: Archive = null
	var lastScan: DateTime = null
	//TODO: Audit database (opt)
	setStyle("-fx-border-width: 1px; -fx-border-style: solid")

	def include(path: Path, subfolders: Boolean = true) = if (getMonitoredFile(path) == null) monitoredFiles = monitoredFiles :+ new Monitored(path, path.getFileName.toString + "/", subfolders)
	def includeAll(paths: Array[Path], subfolders: Boolean = true) = paths.foreach(p => include(p, subfolders))

	def getMonitoredFile(p: Path): Monitored =
	{
		monitoredFiles.foreach(f =>
		{
			if (f.equals(p))
				return f
			val toReturn: Monitored = f.getMonitoredFile(p)
			if (toReturn != null)
				return toReturn
		})
		null
	}

	def archive(file: String): Boolean =
	{
		if (archive == null) return false
		val toArchive = getMonitoredFile(Paths.get(file))
		if (toArchive == null) return false
		archive.archive(toArchive)
		true
	}

	def displayAll(sb: StringBuilder) = monitoredFiles.foreach(f => f.disp(sb))
}