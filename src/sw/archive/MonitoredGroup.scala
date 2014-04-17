package sw.archive

import java.nio.file.Path
import org.joda.time.DateTime

class MonitoredGroup
{
	var monitoredFiles: Array[Monitored] = Array()
	//TODO: Backup location/handler
	var scanInterval: Long = -1
	var lastScan: DateTime = null
	//TODO: Audit database (opt)

	def this(files: Path*)
	{
		this
		includeAll(files.toArray)
	}

	def include(path: Path, subfolders: Boolean = true) = if (!tracksFile(path)) monitoredFiles = monitoredFiles :+ new Monitored(path, subfolders)
	def includeAll(paths: Array[Path], subfolders: Boolean = true) = paths.foreach(p => include(p, subfolders))

	def tracksFile(p: Path): Boolean =
	{
		monitoredFiles.foreach(f => if (f.equals(p) || f.tracksFile(p)) return true)
		false
	}

	def displayAll = monitoredFiles.foreach(f => f.disp)
}