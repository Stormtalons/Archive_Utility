package sw.archive

import java.nio.file.{Paths, Files, Path}
import javafx.scene.control.TreeItem

object Monitored
{
	def fromXML(block: XMLBlock): Monitored =
	{
		val line = block.getLine(0)
		val toReturn = new Monitored(Paths.get(line.getAttr("path")), line.getAttr("relativePath"))
		if (line.getAttr("exclude") != null)
			toReturn.exclude = line.getAttr("exclude").toBoolean
		if (line.getAttr("includeSubfolders") != null)
			toReturn.includeSubfolders = line.getAttr("includeSubfolders").toBoolean
		if (!line.isClosed)
		{
			var index = 1
			while (index < block.lines.length - 1)
			{
				val temp = block.getBlock(index)
				toReturn.add(Monitored.fromXML(temp))
				index += temp.lines.length
			}
		}
		toReturn
	}
}

class Monitored(f: Path, rp: String, subfolders: Boolean, ex: Boolean) extends TreeItem[String]
{
	var file: Path = f
	var relativePath = rp
	setValue(file.getFileName.toString)
	var includeSubfolders = subfolders
	var exclude = ex
	var files: Array[Monitored] = Array()
	scan(null)

	def this(f: Path, rp: String) =
	{
		this(f, rp, true, false)
	}

	def this(f: Path, rp: String, subfolders: Boolean) =
	{
		this(f, rp, subfolders, false)
	}

	def add(file: Monitored): Unit =
	{
		for (i <- 0 to files.length - 1)
			if (files(i).equals(file))
			{
				files(i) = file
				return
			}
		files = files :+ file
	}

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

	def scan(doFirst: (Monitored) => Unit, doLast: (Monitored) => Unit = null): Unit =
	{
		if (doFirst != null)
			doFirst(this)

		if (Files.isDirectory(file))
		{
			val it = Files.newDirectoryStream(file).iterator
			while (it.hasNext)
			{
				val p = it.next()
				if (getMonitoredFile(p) == null && (Files.isRegularFile(p) || includeSubfolders))
					files = files :+ new Monitored(p, relativePath + f.getFileName, includeSubfolders)
			}
			Main.fx(getChildren.clear)
			files.foreach(f =>
			{
				Main.fx(getChildren.add(f))
				f.scan(doFirst, doLast)
			})
		}

		if (doLast != null)
			doLast(this)
	}

	def equals(m: Monitored): Boolean = Files.isSameFile(m.file, file)
	def equals(p: Path): Boolean = Files.isSameFile(p, file)

	def toXML(indent: String): String =
	{
		val xml = new StringBuilder
		xml.append(indent + "<Monitored path=\"" + file + "\" ")
		xml.append("relativePath=\"" + relativePath + "\" ")
		xml.append("exclude=\"" + exclude + "\" ")
		xml.append("includeSubfolders=\"" + includeSubfolders + "\" ")
		if (files.length == 0)
			xml.append("/>\r\n")
		else
		{
			xml.append(">\r\n")
			files.foreach(file => xml.append(file.toXML(indent + "\t")) + "\r\n")
			xml.append(indent + "</Monitored>\r\n")
		}
		xml.toString
	}
}