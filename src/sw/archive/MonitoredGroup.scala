package sw.archive

import java.nio.file.{Paths, Path}
import javafx.scene.layout.VBox
import javafx.scene.control.{TreeItem, Button, TreeView, Label}
import javafx.geometry.Insets
import javafx.event.{EventHandler, ActionEvent}
import javafx.scene.input.{TransferMode, DataFormat, DragEvent}
import java.io.File
import javafx.scene.Node

object MonitoredGroup
{
	def fromXML(block: XMLBlock, getArchive: (String) => Archive, ac: (MonitoredGroup) => Unit = null): MonitoredGroup =
	{
		val line = block.getLine(0)
		val toReturn = new MonitoredGroup(ac)
		var temp = line.getAttr("name")
		if (temp != null && temp.length > 0)
			toReturn.name.set(temp)
		temp = line.getAttr("archive")
		if (temp != null && temp.length > 0)
			toReturn.archive.set(getArchive(temp))
		temp = line.getAttr("archiveInterval")
		if (temp != null && temp.length > 0)
			toReturn.archiveInterval.set(temp.toLong)
		temp = line.getAttr("lastArchived")
		if (temp != null && temp.length > 0)
			toReturn.lastArchived.set(temp.toLong)
		if (!line.isClosed)
		{
			var index = 1
			while (index < block.lines.length - 1)
			{
				val temp = block.getBlock(index)
				toReturn.include(Monitored.fromXML(temp))
				index += temp.lines.length
			}
		}
		toReturn
	}
}

class MonitoredGroup(ac: (MonitoredGroup) => Unit = null) extends VBox
{
	setPadding(new Insets(10))
	setSpacing(10)
	getStylesheets.add("sw/archive/dft.css")
	getStyleClass.add("group")
	var name: Setting[String] = new Setting(Setting.LABEL_AND_FIELD, "Name", "New Group")
	add(name)
	val archiveChooser: Button = new Button("Choose")
	archiveChooser.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = ac(MonitoredGroup.this)})
	val archiveNow: Button = new Button("Archive Now")
	archiveNow.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = archiveAll(true)})
	var archive: Setting[Archive] = new Setting(Setting.LABEL_AND_FIELD, "Archive")
	archive.addExtras(Array[Button](archiveChooser, archiveNow))
	add(archive)
	var archiveInterval: Setting[Long] = new Setting(Setting.LABEL_AND_FIELD, "Archive Interval")
	add(archiveInterval)
	var lastArchived: Setting[Long] = new Setting(Setting.LABEL_ONLY, Setting.DataType.DATETIME, "Last Archived")
	add(lastArchived)
	var files: Array[Monitored] = Array()
	var filesLabel: Label = new Label("Monitored Folders:")
	add(filesLabel)
	var filesTree: TreeView[String] = new TreeView(new TreeItem[String])
	filesTree.setStyle("-fx-border-width: 1px; -fx-border-style: solid")
	filesTree.setShowRoot(false)
	add(filesTree)
	var disp = new Button("disp")
	disp.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = displayAll(s => println(s))})
	add(disp)
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

	def add(n: Node) = getChildren.add(n)

	private def refreshTree =
	{
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
		files.foreach(file =>
		{
			Main.fx(filesTree.getRoot.getChildren.add(file))
			refreshTree(file)
		})
	}

	def include(file: Monitored): Unit =
	{
		for (i <- 0 to files.length - 1)
			if (files(i).equals(file))
			{
				files(i) = file
				return
			}
		files = files :+ file
		refreshTree
	}
	def include(path: Path, subfolders: Boolean = true): Unit =
		if (getMonitoredFile(path) == null)
			include(new Monitored(path, path.getFileName.toString + "/", subfolders))
	def includeAll(paths: Array[Path], subfolders: Boolean = true) = paths.foreach(p => include(p, subfolders))

	def getMonitoredFile(p: Path): Monitored =
	{
		files.foreach(f =>
		{
			if (f.equals(p))
				return f
			val toReturn: Monitored = f.getMonitoredFile(p)
			if (toReturn != null)
				return toReturn
		})
		null
	}

	def setArchive(a: Archive) = archive.set(a)

	def archiveAll: Unit = archiveAll(false)
	private def archiveAll(overrideInterval: Boolean): Unit =
		if (archive.get != null && (System.currentTimeMillis > lastArchived.get + archiveInterval.get || overrideInterval))
		{
			files.foreach(file => file.scan(file => archive.get.archive(file)))
			lastArchived.set(System.currentTimeMillis)
		}

	def displayAll(doWith: (String) => Unit) = files.foreach(f => f.scan(f => doWith(f.file.toString)))
	
	def toXML: String =
	{
		val xml = new StringBuilder
		xml.append("<MonitoredGroup name=\"" + name.get + "\" ")
		xml.append("archive=\"" + (if (archive.get != null) archive.get else "") + "\" ")
		xml.append("archiveInterval=\"" + (if (archiveInterval.get != null) archiveInterval.get else "") + "\" ")
		xml.append("lastArchived=\"" + (if (lastArchived.get != null) lastArchived.get else "") + "\" ")
		if (files.length == 0)
			xml.append("/>\r\n")
		else
		{
			xml.append(">\r\n")
			files.foreach(file => xml.append(file.toXML("\t")))
			xml.append("</MonitoredGroup>\r\n")
		}
		xml.toString
	}
}