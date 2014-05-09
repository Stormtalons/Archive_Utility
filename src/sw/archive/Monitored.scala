package sw.archive

import java.nio.file.{Files, Path}

trait Monitored
{
	private var includeSubfolders: Boolean = true
	def getIncludeSubfolders: Boolean = includeSubfolders
	def setIncludeSubfolders(inc: Boolean) = includeSubfolders = inc

	protected var children: Array[MonitoredItem] = Array()

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

	def hasChild(p: Path, checkAll: Boolean = false): MonitoredItem =
	{
		forEachChild(child => if (child.isSameFile(p)) return child, checkAll)
		null
	}

	def forEachChild(code: (MonitoredItem) => Unit, recurse: Boolean = true): Unit =
		children.foreach(child =>
		{
			code(child)
			if (recurse) child.forEachChild(code)
		})

	def sortChildren =
	{
		children = children.sortWith((c1: MonitoredItem, c2: MonitoredItem) =>
		{
			if (c1.isDir && !c2.isDir)
				true
			else
				c1.getFileName < c2.getFileName
		})
	}

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
					if (hasChild(path) == null)
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

	def fromXML[AnyRef <: Monitored](block: XMLBlock)(implicit getItem: (XMLLine) => AnyRef): AnyRef =
	{
		val line = block.getLine(0)
		val toReturn = getItem(line)
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