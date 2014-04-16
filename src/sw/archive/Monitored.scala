package sw.archive

import org.joda.time.DateTime
import java.nio.file.Path

trait Monitored
{
	var path: Path
	var excluded = false
	var lastArchive: DateTime = null
	def archive(archive: => Boolean): Boolean =
	{
		if (archive)
		{
			lastArchive = new DateTime
			true
		}
		else
			false
	}
}