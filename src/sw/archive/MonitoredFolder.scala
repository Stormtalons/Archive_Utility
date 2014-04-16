package sw.archive

import java.nio.file.Path

class MonitoredFolder(p: Path, subfolders: Boolean = true)
{
	var path: Path = p
	var files: Array[MonitoredFile] = Array()

}
