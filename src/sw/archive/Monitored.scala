package sw.archive

import java.nio.file.{Files, Path}

trait Monitored
{
	protected var includeSubfolders: Boolean = true
	def getIncludeSubfolders: Boolean = includeSubfolders

	protected var children: Array[MonitoredItem] = Array()

	def addChild(newChild: MonitoredItem): Boolean =
	{
		for (i <- 0 to children.length - 1)
			if (children(i).equals(newChild))
			{
				children(i) = newChild
				false
			}
		children = children :+ newChild
		true
	}

	def tracksChild(p: Path, checkAll: Boolean = false): MonitoredItem =
	{
		doForEach(child =>
		{
			if (child.equals(p)) return child
			checkAll
		})
		null
	}

	def refreshContents: Unit =
	{
		doForEach(child =>
		{
			println("Adding - " + child.getFile.toString)
			if (Files.isDirectory(child.getFile))
			{
				var newChildren: Array[MonitoredItem] = Array()
				val it = Files.newDirectoryStream(child.getFile).iterator
				while (it.hasNext)
				{
					val p = it.next()
					val newChild = tracksChild(p)
					newChildren = newChildren :+ (if (newChild != null) newChild else new MonitoredItem(p, child.getRelativePath + p.getFileName + "/", child.includeSubfolders, if (Files.isRegularFile(p)) false else !child.includeSubfolders, false))
				}
				children = newChildren
				Main.fx(
				{
					child.getChildren.clear
					children.foreach(child => child.getChildren.add(child))
				})
			}
			true
		})
	}

	def doForEach(code: (MonitoredItem) => Boolean): Unit =
		if (code != null)
			children.foreach(child =>
			{
				if (code(child))
					child.doForEach(code)
			})

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