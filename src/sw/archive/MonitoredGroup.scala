package sw.archive

import java.nio.file.{Paths, Path}
import org.joda.time.DateTime
import javafx.scene.layout.GridPane
import javafx.scene.control.{TreeView, Label}
import javafx.geometry.Insets

class MonitoredGroup(n: String) extends GridPane
{
	setPadding(new Insets(10))
	getStylesheets.add("sw/archive/dft.css")
	var name: Setting = new Setting("Name", n)
	add(name, 0, 0)
	var monitoredFiles: Array[Monitored] = Array()
	var filesLabel: Label = new Label("Monitored Folders:")
	add(filesLabel, 0, 1)
	var filesTree: TreeView[String] = new TreeView
	filesTree.setStyle("-fx-border-width: 1px; -fx-border-style: solid")
	add(filesTree, 0, 2)
	var archive: Archive = null
	var scanInterval: Long = -1
	var lastScan: DateTime = null
	//TODO: Audit database (opt)
	setStyle("-fx-border-width: 1px; -fx-border-style: solid")

	def this(n: String, files: Path*) =
	{
		this(n)
		includeAll(files.toArray)
	}

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