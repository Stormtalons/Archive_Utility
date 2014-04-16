package sw.archive

import org.joda.time.DateTime
import java.nio.file.{Files, Path}

class Monitored(p: Path, subfolders: Boolean = true)
{
	var path: Path = p
	var includeSubfolders = subfolders
	var excluded = false
	var lastArchived: DateTime = null
	var files: Array[Monitored] = Array()
	scan

	def tracksFile(p: Path): Boolean =
	{
		for (f <- files)
			if (f.equals(p) || f.tracksFile(p))
				return true
		false
	}

	def scan: Unit =
	{
		if (Files.isDirectory(path))
		{
			val it = Files.newDirectoryStream(path).iterator
			while (it.hasNext)
			{
				val p = it.next()
				if (!tracksFile(p) && (Files.isRegularFile(p) || includeSubfolders))
					files = files :+ new Monitored(p, includeSubfolders)
			}
			files.foreach(f => f.scan)
		}
	}

	def equals(p: Path): Boolean = Files.isSameFile(p, path)
	
	def disp: Unit =
	{
		println(path)
		if (Files.isDirectory(path))
			files.foreach(f => f.disp)
	}
}