package sw.archive

import org.joda.time.DateTime
import java.nio.file.{Files, Path}

class Monitored(f: Path, rp: String, subfolders: Boolean = true)
{
	var file: Path = f
	var relativePath = rp
	var includeSubfolders = subfolders
	var excluded = false
	var lastArchived: DateTime = null
	var files: Array[Monitored] = Array()
	scan

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

	def scan: Unit =
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
			files.foreach(f => f.scan)
		}
	}

	def equals(p: Path): Boolean = Files.isSameFile(p, file)
	
	def disp(sb: StringBuilder): Unit =
	{
		sb.append(file + "\n")
		if (Files.isDirectory(file))
			files.foreach(f => f.disp(sb))
	}
}