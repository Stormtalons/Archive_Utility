package sw.archive

import javafx.scene.image.{ImageView, Image}
import java.nio.file.{Files, Path, Paths}
import javafx.scene.control.{CheckBox, TreeItem}
import javafx.scene.layout.HBox
import javafx.event.{EventHandler, ActionEvent}

/*
	This class represents one monitored item, be that a folder or
	a file, and all of the settings and management that demands.
 */

object MonitoredItem extends Monitored
{
//Static copy of a file icon for use in file (as opposed to folder) tree items only.
	val fileImg: Image = new Image(getClass.getResourceAsStream("res/file.png"))

//Define how to create a MonitoredGroup item.
	implicit def getItem(line: XMLLine): MonitoredItem = new MonitoredItem(Paths.get(line.getAttr("path")), line.getAttr("relativePath"), Main.tryGet[Boolean](line.getAttr("includeSubfolders").toBoolean, true), Main.tryGet[Boolean](line.getAttr("exclude").toBoolean, false), false, Main.tryGet[Int](line.getAttr("relativePath").length, 0) == 0)

//Rely on the parent definition for the rest of the operation, only redefining to supply the return type.
//TODO: Do more research on Scala's type system to see if there is a better way to accomplish this.
	def fromXML(block: XMLBlock): MonitoredItem = fromXML[MonitoredItem](block)
}

class MonitoredItem(_file: Path, _relativePath: String, includeSubfolders: Boolean, _exclude: Boolean, displayFullPath: Boolean = false) extends TreeItem[String] with Monitored
{
	setIncludeSubfolders(includeSubfolders)
	
//Definition, getters & setters for the item's underlying file.
	private val file: Path = _file
	def getFile: Path = file
	def getFilePath: String = Main.formatFilePath(getFile.toString)
	def getFileName: String = getFile.getFileName.toString + (if (isDir) "/" else "")
	
//Convenience methods
	def isDir: Boolean = Files.isDirectory(getFile)
	def exists: Boolean = Files.exists(file)
	def isSameFile(toCheck: MonitoredItem): Boolean = if (toCheck.exists && exists) Files.isSameFile(toCheck.getFile, getFile) else toCheck.getFilePath.equals(getFilePath)
	def isSameFile(toCheck: Path): Boolean = if (Files.exists(toCheck) && exists) Files.isSameFile(toCheck, getFile) else toCheck.toString.equals(getFilePath)
	
//Sets the node value
	setValue(if (displayFullPath) getFilePath else getFileName)

//Definition, getters & setters for the item's relative path as shown
//in the TreeView. Used during archival for determining the destination
//file path dynamically based on its location in the tree.
	private val relativePath: String = _relativePath
	def getRelativePath: String = relativePath
	
//Creates the node's custom graphic box to support icons & checkboxes.
	private val graphic: HBox = new HBox
	if (!isDir && exists)
		graphic.getChildren.add(new ImageView(MonitoredItem.fileImg))
	
//Definition, getters & setters for the item's 'excluded' flag, and its
//associated CheckBox for the UI. To note: the value 'excluded' is the
//inverse of the checkbox - a box being unchecked will disqualify it
//from any and all operations.
	private val exclude: CheckBox = new CheckBox
	exclude.setSelected(!_exclude)
	exclude.addEventFilter(ActionEvent.ACTION, new EventHandler[ActionEvent]
	{
		def handle(evt: ActionEvent) =
		{
			exclude.setSelected(exclude.isSelected)
			forEachChild(child => child.checkParentalExclusion)
			evt.consume
		}
	})
	def isExcluded: Boolean = !exclude.isSelected
	
//A check for whether the item should be disabled by virtue of one of its parents being
//excluded from operation. This is distinct from being the item being disabled itself.
	def checkParentalExclusion =
	{
		var parent: TreeItem[String] = getParent
		while (parent != null && parent.isInstanceOf[MonitoredItem])
		{
			if (parent.asInstanceOf[MonitoredItem].isExcluded)
				exclude.setDisable(true)
			parent = parent.getParent
		}
		exclude.setDisable(false)
	}
	graphic.getChildren.add(exclude)
	setGraphic(graphic)

//Defines the XML representation of a MonitoredItem.
	def getXML: (String, String) = ("MonitoredItem", "path=\"" + getFilePath + "\" relativePath=\"" + getRelativePath + "\" exclude=\"" + isExcluded + "\" includeSubfolders=\"" + getIncludeSubfolders + "\" ")
}