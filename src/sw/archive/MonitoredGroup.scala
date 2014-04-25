package sw.archive

import java.nio.file.{Paths, Path}
import org.joda.time.DateTime
import javafx.scene.layout.VBox
import javafx.scene.control.{TreeItem, Button, TreeView, Label}
import javafx.geometry.Insets
import javafx.event.{EventHandler, ActionEvent}
import javafx.scene.input.{TransferMode, DataFormat, DragEvent}
import java.io.File

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
	val archiveNow: Button = new Button("Archive Now")
	archiveNow.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = archiveAll})
	var archiveName: Setting = new Setting(Setting.LABEL_AND_FIELD, "Archive", Array[Button](archiveChooser, archiveNow))
	getChildren.add(archiveName)
	var monitoredFiles: Array[Monitored] = Array()
	var filesLabel: Label = new Label("Monitored Folders:")
	getChildren.add(filesLabel)
	var filesTree: TreeView[String] = new TreeView(new TreeItem[String])
	filesTree.setStyle("-fx-border-width: 1px; -fx-border-style: solid")
	getChildren.add(filesTree)
	var disp = new Button("disp")
	disp.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = displayAll(s => println(s))})
	getChildren.add(disp)
	var archive: Archive = null
	var lastScan: DateTime = null
	//TODO: Audit database (opt)
	setStyle("-fx-border-width: 1px; -fx-border-style: solid")

	setOnDragOver(new EventHandler[DragEvent]{def handle(evt: DragEvent) = if (evt.getDragboard.hasContent(DataFormat.FILES)) evt.acceptTransferModes(TransferMode.COPY)})
	setOnDragDropped(new EventHandler[DragEvent]
	{
		def handle(evt: DragEvent) =
		{
			val files = evt.getDragboard.getContent(DataFormat.FILES).asInstanceOf[java.util.List[File]]
			for (i <- 0 to files.size - 1)
				include(Paths.get(files.get(i).getPath))
		}
	})

	def include(path: Path, subfolders: Boolean = true) =
	{
		if (getMonitoredFile(path) == null)
			monitoredFiles = monitoredFiles :+ new Monitored(path, path.getFileName.toString + "/", subfolders)
		Main.fx(filesTree.getRoot.getChildren.clear)
		def refreshTree(node: Monitored): Unit =
		{
			Main.fx(node.getChildren.clear)
			node.files.foreach(file =>
			{
				Main.fx(node.getChildren.add(file))
				refreshTree(file)
			})
		}
		monitoredFiles.foreach(file =>
		{
			Main.fx(filesTree.getRoot.getChildren.add(file))
			refreshTree(file)
		})
	}
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

	def setArchive(a: Archive) =
	{
		archive = a
		archiveName.setValue(archive.getName)
	}

	def archiveAll =
		if (archive != null)
			monitoredFiles.foreach(file => file.scan((file: Monitored) => archive.archive(file)))

	def displayAll(doWith: (String) => Unit) = monitoredFiles.foreach(f => f.disp(doWith))
}