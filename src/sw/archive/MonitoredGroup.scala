package sw.archive

import java.nio.file.{Paths, Path}
import javafx.scene.layout.VBox
import javafx.scene.control._
import javafx.geometry.Insets
import javafx.event.{EventHandler, ActionEvent}
import javafx.scene.input.{TransferMode, DataFormat, DragEvent}
import java.io.File
import javafx.scene.Node
import java.util

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
	getStylesheets.add("sw/archive/res/dft.css")
	getStyleClass.add("group")
	var name: Setting[String] = new Setting(Setting.LABEL_AND_FIELD, "Name", "New Group")
	add(name)
	val archiveChooser: Button = new Button("Choose")
	archiveChooser.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = ac(MonitoredGroup.this)})
	val archiveAllNow: Button = new Button("Archive All")
	archiveAllNow.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = archiveAll(true)})
	var archive: Setting[Archive] = new Setting(Setting.LABEL_AND_FIELD, "Archive", null)
	archive.addExtras(Array[Button](archiveChooser, archiveAllNow))
	add(archive)
	var archiveInterval: Setting[Long] = new Setting(Setting.LABEL_AND_FIELD, "Archive Interval", 0)
	add(archiveInterval)
	var lastArchived: Setting[Long] = new Setting(Setting.LABEL_ONLY, "Last Archived", 0, Setting.DataType.DATETIME)
	add(lastArchived)
	var files: Array[Monitored] = Array()
	var filesLabel: Label = new Label("Monitored Folders:")
	add(filesLabel)
	var filesTree: TreeView[String] = new TreeView(new TreeItem[String])
	filesTree.getStylesheets.add("sw/archive/res/dft.css")
	filesTree.setShowRoot(false)
	var popupMenu: ContextMenu = new ContextMenu
	var archiveNow: MenuItem = new MenuItem("Archive")
	archiveNow.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = archive(filesTree.getSelectionModel.getSelectedItem.asInstanceOf[Monitored])})
	var removeItem: MenuItem = new MenuItem("Remove")
	removeItem.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = remove(filesTree.getSelectionModel.getSelectedItem.asInstanceOf[Monitored])})
	popupMenu.getItems.addAll(archiveNow, removeItem)
	filesTree.setContextMenu(popupMenu)
	add(filesTree)
//	var disp = new Button("disp")
//	disp.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = displayAll(s => println(s))})
//	add(disp)
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

	//TODO: This method needs fixing.
	def remove(toRemove: Monitored): Unit =
	{
		toRemove.getParent.getChildren.removeAll(toRemove)
		files.foreach(file =>
			file.doForAll(f =>
				if (f.equals(toRemove))
				{
					val (l, r) = file.files.span(f => f.equals(file))
					println("Left:")
					l.foreach(f => print(f.toString + ", "))
					println
					println("Right:")
					r.foreach(f => print(f.toString + ", "))
					file.files = l union r.drop(1)
					return
				}))
	}

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
				println("returning this")
				files(i) = file
				return
			}
		files = files :+ file
		refreshTree
	}
	def include(path: Path, subfolders: Boolean = true): Unit =
		if (getMonitoredFile(path) == null)
			include(new Monitored(path, path.getFileName.toString + "/", null, subfolders))
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

	def archiveAll(overrideInterval: Boolean) =
		if (archive.get != null && (System.currentTimeMillis > lastArchived.get + archiveInterval.get || overrideInterval))
		{
			files.foreach(file => archive(file))
			lastArchived.set(System.currentTimeMillis)
		}

	def archive(file: Monitored): Unit =
		if (archive.get != null)
			file.doForAll(file => archive.get.archive(file))

	def displayAll(doWith: (String) => Unit) = files.foreach(f => f.scan(f => doWith(f.file.toString)))
	
	def toXML: String =
	{
		val xml = new StringBuilder
		xml.append("<MonitoredGroup name=\"" + name.get + "\" ")
		xml.append("archive=\"" + (if (archive.get != null) archive.get else "") + "\" ")
		xml.append("archiveInterval=\"" + archiveInterval.get + "\" ")
		xml.append("lastArchived=\"" + lastArchived.get + "\" ")
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