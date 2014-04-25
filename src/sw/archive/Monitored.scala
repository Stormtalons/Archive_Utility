package sw.archive

import org.joda.time.DateTime
import java.nio.file.{Files, Path}
import javafx.scene.control.TreeItem

class Monitored(f: Path, rp: String, subfolders: Boolean = true) extends TreeItem[String]
{
	var file: Path = f
	var relativePath = rp
	setValue(file.getFileName.toString)
	var includeSubfolders = subfolders
	var excluded = false
	var lastArchived: DateTime = null
	var files: Array[Monitored] = Array()
	scan(null)

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

	def scan(doWithFiles: (Monitored) => Unit): Unit =
	{
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
				if (Files.isRegularFile(f.file) && doWithFiles != null)
					doWithFiles(f)
				Main.fx(getChildren.add(f))
				f.scan(null)
			})
		}
	}

	def equals(p: Path): Boolean = Files.isSameFile(p, file)
	
	def disp(doWith: (String) => Unit): Unit =
	{
		doWith(file.toString)
		if (Files.isDirectory(file))
			files.foreach(f => f.disp(doWith))
	}
}