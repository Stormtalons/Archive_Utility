package sw.archive

import java.nio.file.{Files, Path}

/*
	This trait provides the functionality common to both MonitoredGroups
	and MonitoredItems (particularly with respect to the ownership and
	management of children) while preserving JavaFX inheritence to allow
	for UI display.
 */

trait Monitored
{
//Definition, getter & setter for the flag to include or exclude subfolders.
	private var includeSubfolders: Boolean = true
	def getIncludeSubfolders: Boolean = includeSubfolders
	def setIncludeSubfolders(inc: Boolean) = includeSubfolders = inc
	//TODO: Provide an interface for the user to designate this setting. Currently only allowed to be 'true'.
	//TODO: Test subfolder inclusion & cascading.

	protected var children: Array[MonitoredItem] = Array()

//Allows for overwriting the settings/configuration of a child
//in addition to adding brand new children. The returned value
//is used to determine whether or not the TreeView needs to be refreshed.
	def addChild(newChild: MonitoredItem): Boolean =
	{
		for (i <- 0 to children.length - 1)
			if (children(i).isSameFile(newChild))
			{
				children(i) = newChild
				return false
			}
		children = children :+ newChild
		true
	}

//Checks for the inclusion of a file, allowing for topical search
//and full tree traversal.
	def hasChild(path: Path, checkAll: Boolean): MonitoredItem =
	{
		forEachChild(child => if (child.isSameFile(path)) return child, checkAll)
		null
	}
	def hasChild(item: MonitoredItem, checkAll: Boolean): MonitoredItem =
	{
		forEachChild(child => if (child.isSameFile(item)) return child, checkAll)
		null
	}

//Utility function to support performing dynamic operations
//on a whole tree (or just one branch). Allows the calling context
//to specify if the operation should recurse, or only apply to
//first-gen children.
	def forEachChild(code: (MonitoredItem) => Unit, recurse: Boolean = true): Unit =
		children.foreach(child =>
		{
			code(child)
			if (recurse)
				child.forEachChild(code)
		})
	
//Sorts the list of children according to normal file system sort
//order, so the TreeView looks natural.
	def sortChildren: Unit =
		children = children.sortWith((c1: MonitoredItem, c2: MonitoredItem) =>
		{
			if (c1.isDir && !c2.isDir)
				true
			else
				c1.getFileName < c2.getFileName
		})
	
//Scans monitored directories for changes and adds any new files found.
//Syncs changes with the TreeView.
	def refreshContents: Unit =
	{
		sortChildren
		forEachChild(child =>
		{
			if (Files.isDirectory(child.getFile))
			{
				var newChildren: Array[MonitoredItem] = Array()
				val it = Files.newDirectoryStream(child.getFile).iterator
				while (it.hasNext)
				{
					val path = it.next()
					if (hasChild(path, false) == null)
						newChildren = newChildren :+ new MonitoredItem(path, child.getRelativePath + child.getFileName, child.includeSubfolders, if (Files.isRegularFile(path)) false else !child.includeSubfolders, false)
				}
				child.children = newChildren
				child.sortChildren
				Main.fx(
				{
					child.getChildren.clear
					child.forEachChild(c => child.getChildren.add(c), false)
				})
			}
		})
	}

//Used for loading from file.
//Relies on an implicit function to derive an instance of MonitoredGroup
//or MonitoredItem via its XML representation, and encapsulates the boilerplate
//required for both.
	def fromXML[AnyRef <: Monitored](block: XMLBlock)(implicit createItem: (XMLLine) => AnyRef): AnyRef =
	{
		val line = block.getLine(0)
		val toReturn = createItem(line)
		if (!line.isClosed)
		{
			var index = 1
			while (index < block.lines.length - 1)
			{
				val temp = block.getBlock(index)
				toReturn.addChild(MonitoredItem.fromXML(temp))
				index += temp.lines.length
			}
		}
		toReturn
	}

//Used for saving to file.
//Functions almost exactly like fromXML (except for omitting the implicit definition),
//and serves the same purpose.
	def toXML(getXML: => (String, String), indent: String): String =
	{
		val xml = new StringBuilder
		val (tag, content) = getXML
		xml.append(indent + "<" + tag + " " + content)
		if (children.length == 0)
			xml.append("/>\r\n")
		else
		{
			xml.append(">\r\n")
			children.foreach(child => xml.append(child.toXML(child.getXML, indent + "\t")) + "\r\n")
			xml.append(indent + "</" + tag + ">\r\n")
		}
		xml.toString
	}
}