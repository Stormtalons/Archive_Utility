package sw.archive

import java.text.SimpleDateFormat
import javafx.scene.layout.{Priority, StackPane, VBox}
import javafx.geometry.Insets
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.input.{TransferMode, DataFormat, DragEvent}
import java.io.File
import java.nio.file.{Files, Path, Paths}
import javafx.scene.control._
import javax.swing.JOptionPane

/*
	This class represents a collection of folders/files grouped
	by common settings.
 */

object MonitoredGroup extends Monitored
{
	//Derives an instance of MonitoredGroup from an XML representation.
	def fromXML(block: XMLBlock, getArchive: (String) => Archive, archiveSelectionRoutine: (MonitoredGroup) => Unit = null): MonitoredGroup =
	{
		//Define how to create a MonitoredGroup from an XMLLine, used by the parent fromXML function.
		implicit def getItem(line: XMLLine): MonitoredGroup =
		{
			val toReturn = new MonitoredGroup(archiveSelectionRoutine)
			toReturn.setName(line.getAttr("name"))
			toReturn.setArchiveInfo(getArchive(line.getAttr("archive")), Main.tryGet[Long](line.getAttr("archiveInterval").toLong, 0))
			toReturn.setLastArchived(Main.tryGet[Long](line.getAttr("lastArchived").toLong, 0))
			toReturn
		}
		fromXML[MonitoredGroup](block)
	}
}

class MonitoredGroup(archiveSelectionRoutine: (MonitoredGroup) => Unit) extends VBox with Monitored
{
	setMinHeight(400)
	setPadding(new Insets(10))
	setSpacing(10)
	getStylesheets.add("sw/archive/res/dft.css")
	getStyleClass.add("group")
	setStyle("-fx-border-width: 1px; -fx-border-style: solid")

//Provides a DnD interface for adding new folders/files to a group.
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
	//TODO: Provide an interface for the user to designate this setting. Currently only allowed to be 'true'.

//Definition, getter & setter for the group's name.
	private val name: Setting = new Setting(Setting.LABEL_AND_FIELD, "Name", "New Group")
	def getName: String = name.get
	def setName(_name: String) = name.set(_name)
	getChildren.add(name)

//Definition, getters & setters for the group's archive settings.
	private var archive: Archive = null
	def getArchive: Archive = archive

	//archiveName's behavior is currently duplicated by clicking the archiveChooser button.
	//	TODO: Decide which method is better, and remove the other.
	private val archiveName: Setting = new Setting(Setting.LABEL_AND_FIELD, "Archive", archiveSelectionRoutine(MonitoredGroup.this))
	def getArchiveName: String = archiveName.get
	def setArchive(_archive: Archive) =
		if (_archive != null)
		{
			archive = _archive
			archiveName.set(archive.getName)
		}

	//archiveChooser's behavior is currently duplicated by clicking on the archiveName field.
	//	TODO: Decide which method is better, and remove the other.
	private val archiveChooser: Button = new Button("Choose")
	archiveChooser.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = archiveSelectionRoutine(MonitoredGroup.this)})

	//Allow the user to manually archive a group off-schedule.
	private val archiveAllNow: Button = new Button("Archive All")
	archiveAllNow.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = archive(null, true)})

	//TODO: Include a 'Restore From Archive' right click option.

	archiveName.addExtras(Array[Button](archiveChooser, archiveAllNow))
	private val archiveInterval: Setting = new Setting(Setting.LABEL_AND_FIELD, "Archive Interval")
	def getArchiveInterval: Long = Main.tryGet[Long](archiveInterval.get.toLong, 0)
	def setArchiveInterval(interval: Long) = archiveInterval.set(if (interval == 0) "" else interval.toString)

	//Convenience method.
	def setArchiveInfo(a: Archive, interval: Long) =
	{
		setArchive(a)
		setArchiveInterval(interval)
	}

	//Separate the actual last archive time from the UI setting to allow easy date display.
	private var lastArchived: Long = 0
	private val lastArchivedLabel: Setting = new Setting(Setting.LABEL_ONLY, "Last Archived")
	def getLastArchived: Long = lastArchived
	def setLastArchived(time: Long = 0) =
	{
		lastArchived = time
		lastArchivedLabel.set(if (lastArchived == 0) "" else new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(time))
	}
	getChildren.addAll(archiveName, archiveInterval, lastArchivedLabel)
//End of archive settings.

//TreeView & right click menu definition.
	private val filesLabel: Label = new Label("Monitored Items:")
	getChildren.add(filesLabel)
	private val filesTree: TreeView[String] = new TreeView(new TreeItem[String])
	filesTree.getStylesheets.add("sw/archive/res/dft.css")
	filesTree.setShowRoot(false)
	private val popupMenu: ContextMenu = new ContextMenu

	//Allow the user to archive just one branch of the tree via right click menu.
	//Does not update lastArchived.
	private val archiveNow: MenuItem = new MenuItem("Archive")
	archiveNow.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = archive(filesTree.getSelectionModel.getSelectedItem.asInstanceOf[MonitoredItem], true)})
	private val removeItem: MenuItem = new MenuItem("Remove")
	removeItem.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = remove(filesTree.getSelectionModel.getSelectedItem.asInstanceOf[MonitoredItem])})
	popupMenu.getItems.addAll(archiveNow, removeItem)
	filesTree.setContextMenu(popupMenu)
	private val dropFilesHere: Label = new Label("Drop Files Here")
	dropFilesHere.setStyle("-fx-text-fill: gray; -fx-font-size: 18pt; -fx-font-family: Verdana; -fx-font-weight: bold")
	private val stackPane: StackPane = new StackPane
	VBox.setVgrow(stackPane, Priority.ALWAYS)
	stackPane.getChildren.addAll(filesTree, dropFilesHere)
	getChildren.add(stackPane)
//End of TreeView def.

//TODO: Add a section for database connectivity info for potential activity logging.

//TODO: Add a section for defining email notification settings.

//Override default addChild routine to provide the necessary scope for
//accessing the root of the tree.
	override def addChild(newChild: MonitoredItem): Boolean =
		if (super.addChild(newChild))
		{
			dropFilesHere.setVisible(false)
			Main.fx(filesTree.getRoot.getChildren.add(newChild))
			true
		}
		else false

//Add new child from a path (currently only applies to DnD operations)
	def addChild(path: Path, subfolders: Boolean): Unit =

		//Do not allow duplicate branches in the same group.
		if (hasChild(path, true) != null)
			JOptionPane.showMessageDialog(null, "This " + (if (Files.isDirectory(path)) "folder" else "file") + " is already monitored by this group.")
		else
		{
			addChild(new MonitoredItem(path, "", subfolders, false, true))
			refreshContents
		}

//Removes an item from the list of children and the tree.
	def remove(toRemove: MonitoredItem): Unit =

		//Do not allow the removal of non-root folders, since they would be re-added upon rescanning anyway.
		if (hasChild(toRemove, false) == null)
			JOptionPane.showMessageDialog(null, "You tried to remove a subdirectory - please exclude this item instead of removing.")
		else
		{
			val (l, r) = children.span(child => child.isSameFile(toRemove))
			children = l.dropRight(1).union(r)
			Main.fx(
			{
				filesTree.getRoot.getChildren.clear
				children.foreach(child => filesTree.getRoot.getChildren.add(child))
				if (filesTree.getRoot.getChildren.size == 0)
					dropFilesHere.setVisible(true)
			})
		}

//Main archive routine. Recursive, applies to all children.
	def archive(child: MonitoredItem, userInitiated: Boolean): Unit =
	{
		if (getArchive == null)
			return
		if (userInitiated || System.currentTimeMillis > getLastArchived + getArchiveInterval)

			//If the child parameter is null, archive all children.
			if (child == null)
			{
				forEachChild(child => getArchive.archive(child))
				setLastArchived(System.currentTimeMillis)
			}

			//Otherwise, only archive the specified branch.
			else
				child.forEachChild(child => getArchive.archive(child))
	}

//Defines the XML representation of a MonitoredGroup.
	def getXML: (String, String) = ("MonitoredGroup", "name=\"" + getName + "\" archive=\"" + getArchiveName + "\" archiveInterval=\"" + getArchiveInterval + "\" lastArchived=\"" + getLastArchived + "\" ")
}
