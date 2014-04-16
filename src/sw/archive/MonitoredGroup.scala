package sw.archive

import java.nio.file.{Files, Path}
import org.joda.time.DateTime
import java.util.function.Consumer

class MonitoredGroup(files: Array[Path]) extends Monitored
{
	var monitoredFiles: Array[_ >: Monitored] = if (files == null) Array() else files
	//Backup location
	var scanInterval: Long = -1
	//Audit database (opt)

	def include(path: Path, subfolders: Boolean = true): Unit =
	{
		def inc(path: Path, depth: Int): Unit =
		{
			if (Files.isDirectory(path) && (subfolders || depth == 0))
				Files.newDirectoryStream(path).iterator.forEachRemaining(new Consumer[Path] {def accept(p: Path): Unit = if (subfolders) inc(p, depth + 1)})
			else
				monitoredFiles = monitoredFiles :+ path
		}
		inc(path, 0)
		monitoredFiles.foreach(p => println(p))
	}
	def includeAll(paths: Array[Path], subfolders: Boolean = true) = paths.foreach(p => include(p, subfolders))

	def exclude(path: Path*)(includeSubs: Boolean = true) =
	{

	}
}