package sw.archive

import java.nio.file.Path
import org.joda.time.DateTime

class MonitoredFile(p: Path, m: Boolean) extends Monitored
{
	var path: Path = p
}