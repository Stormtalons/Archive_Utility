package sw.archive

import java.nio.file.Paths

object Main extends App
{
	val temp = new MonitoredGroup
	temp.include(Paths.get("K:\\Temp\\Inspector Gadget 1 & 2 [DVDRip]"))
	temp.displayAll
}