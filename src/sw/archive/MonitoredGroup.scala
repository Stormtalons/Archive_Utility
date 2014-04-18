package sw.archive

import java.nio.file.{Paths, Path}
import org.joda.time.DateTime

class MonitoredGroup
{
	var monitoredFiles: Array[Monitored] = Array()
	var archive: Archive = null
	var scanInterval: Long = -1
	var lastScan: DateTime = null
	//TODO: Audit database (opt)

	def this(files: Path*) =
	{
		this
		includeAll(files.toArray)
	}

	def include(path: Path, subfolders: Boolean = true) = if (getMonitoredFile(path) == null) monitoredFiles = monitoredFiles :+ new Monitored(path, path.getFileName.toString + "/", subfolders)
	def includeAll(paths: Array[Path], subfolders: Boolean = true) = paths.foreach(p => include(p, subfolders))

	def getMonitoredFile(p: Path): Monitored =
	{
		monitoredFiles.foreach(f =>
		{
			if (f.equals(p))
				return f
			val toReturn: Monitored = f.getMonitoredFile(p)
			if (toReturn != null)
				return toReturn
		})
		null
	}

	def archive(file: String): Boolean =
	{
		val toArchive = getMonitoredFile(Paths.get(file))
		if (archive != null && toArchive != null)
			archive.archive(toArchive)
		true
	}

	def displayAll(sb: StringBuilder) = monitoredFiles.foreach(f => f.disp(sb))
}