package sw.archive

import java.nio.file.Paths

object Main extends App
{
	val archives = new ArchiveManager

	val temp = new MonitoredGroup
	temp.archive = archives.createArchive(Paths.get("D:/Code/Java/IntelliJ/Archive Utility/TestArchiveLocation/"))
	temp.include(Paths.get("K:/Temp/Inspector Gadget 1 & 2 [DVDRip]"))
	val sb = new StringBuilder
	temp.displayAll(sb)
	new UI().launch
	
	var running: Boolean = true
	def run =
	{
		while (running)
		{

		}
	}
//	run
}