package sw.archive

import java.text.SimpleDateFormat
import javafx.scene.layout.VBox
import javafx.geometry.Insets
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.input.{TransferMode, DataFormat, DragEvent}
import java.io.File
import java.nio.file.{Files, Path, Paths}
import javafx.scene.control._
import javax.swing.JOptionPane

object MonitoredGroup extends Monitored
{
	def fromXML(block: XMLBlock, getArchive: (String) => Archive, ac: (MonitoredGroup) => Unit = null): MonitoredGroup =
	{
		implicit def getItem(line: XMLLine): MonitoredGroup =
		{
			val toReturn = new MonitoredGroup(ac)
			toReturn.setName(line.getAttr("name"))
			toReturn.setArchiveInfo(getArchive(line.getAttr("archive")), Main.tryGet[Long](line.getAttr("archiveInterval").toLong, 0))
			toReturn.setLastArchived(Main.tryGet[Long](line.getAttr("lastArchived").toLong, 0))
			toReturn
		}
		fromXML[MonitoredGroup](block)
	}
}
class MonitoredGroup(ac: (MonitoredGroup) => Unit) extends VBox with Monitored
{
	setPadding(new Insets(10))
	setSpacing(10)
	getStylesheets.add("sw/archive/res/dft.css")
	getStyleClass.add("group")
	setStyle("-fx-border-width: 1px; -fx-border-style: solid")
	setOnDragOver(new EventHandler[DragEvent]{def handle(evt: DragEvent) = if (evt.getDragboard.hasContent(DataFormat.FILES)) evt.acceptTransferModes(TransferMode.COPY)})
	setOnDragDropped(new EventHandler[DragEvent]
	{
		def handle(evt: DragEvent) =
		{
			val files = evt.getDragboard.getContent(DataFormat.FILES).asInstanceOf[java.util.List[File]]
			for (i <- 0 to files.size - 1)
				addChild(Paths.get(files.get(i).getPath), true)
		}
	})
	setIncludeSubfolders(true)

	private val name: Setting = new Setting(Setting.LABEL_AND_FIELD, "Name", "New Group")
	def getName: String = name.get
	def setName(n: String) = name.set(n)
	getChildren.add(name)

	private val archiveChooser: Button = new Button("Choose")
	archiveChooser.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = ac(MonitoredGroup.this)})
	private val archiveAllNow: Button = new Button("Archive All")
	archiveAllNow.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = archive(null, true)})
	private var archive: Archive = null
	def getArchive: Archive = archive
	private val archiveName: Setting = new Setting(Setting.LABEL_AND_FIELD, "Archive", ac(MonitoredGroup.this))
	archiveName.addExtras(Array[Button](archiveChooser, archiveAllNow))
	def getArchiveName: String = archiveName.get
	def setArchive(a: Archive) =
		if (a != null)
		{
			archive = a
			archiveName.set(archive.toString)
		}
	private val archiveInterval: Setting = new Setting(Setting.LABEL_AND_FIELD, "Archive Interval")
	def getArchiveInterval: Long = Main.tryGet[Long](archiveInterval.get.toLong, 0)
	def setArchiveInterval(interval: Long) = archiveInterval.set(if (interval == 0) "" else interval.toString)
	def setArchiveInfo(a: Archive, interval: Long) =
	{
		setArchive(a)
		setArchiveInterval(interval)
	}
	private var lastArchived: Long = 0
	private val lastArchivedLabel: Setting = new Setting(Setting.LABEL_ONLY, "Last Archived")
	def getLastArchived: Long = lastArchived
	def setLastArchived(time: Long = 0) =
	{
		lastArchived = time
		lastArchivedLabel.set(if (lastArchived == 0) "" else new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(time))
	}
	getChildren.addAll(archiveName, archiveInterval, lastArchivedLabel)

	private val filesLabel: Label = new Label("Monitored Folders:")
	getChildren.add(filesLabel)
	private val filesTree: TreeView[String] = new TreeView(new TreeItem[String])
	filesTree.getStylesheets.add("sw/archive/res/dft.css")
	filesTree.setShowRoot(false)
	private val popupMenu: ContextMenu = new ContextMenu
	private val archiveNow: MenuItem = new MenuItem("Archive")
	archiveNow.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = archive(filesTree.getSelectionModel.getSelectedItem.asInstanceOf[MonitoredItem], true)})
	private val removeItem: MenuItem = new MenuItem("Remove")
	removeItem.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = remove(filesTree.getSelectionModel.getSelectedItem.asInstanceOf[MonitoredItem])})
	popupMenu.getItems.addAll(archiveNow, removeItem)
	filesTree.setContextMenu(popupMenu)
	getChildren.add(filesTree)

	//TODO: Audit database (opt)

	override def addChild(newChild: MonitoredItem): Boolean =
		if (super.addChild(newChild))
		{
			Main.fx(filesTree.getRoot.getChildren.add(newChild))
			true
		}
		else false

	def addChild(path: Path, subfolders: Boolean): Unit =
		if (hasChild(path, true) != null)
			JOptionPane.showMessageDialog(null, "This " + (if (Files.isDirectory(path)) "folder" else "file") + " is already monitored by this group.")
		else
		{
			addChild(new MonitoredItem(path, "", subfolders, false, false, true))
			refreshContents
		}

	def remove(toRemove: MonitoredItem): Unit =
		if (children.contains(toRemove))
		{
			val (l, r) = children.span(child => child.isSameFile(toRemove))
			children = l.dropRight(1).union(r)
			filesTree.getRoot.getChildren.clear
			children.foreach(child => filesTree.getRoot.getChildren.add(child))
		}
		else
			JOptionPane.showMessageDialog(null, "You tried to remove a subdirectory - please exclude this item instead of removing.")



	def archive(child: MonitoredItem, userInitiated: Boolean): Unit =
	{
		if (getArchive == null)
			return
		if (userInitiated || System.currentTimeMillis > getLastArchived + getArchiveInterval)
			if (child == null)
			{
				forEachChild(child => getArchive.archive(child))
				setLastArchived(System.currentTimeMillis)
			}
			else
				child.forEachChild(child => getArchive.archive(child))
	}

	def getXML: (String, String) = ("MonitoredGroup", "name=\"" + getName + "\" archive=\"" + getArchiveName + "\" archiveInterval=\"" + getArchiveInterval + "\" lastArchived=\"" + getLastArchived + "\" ")
}
