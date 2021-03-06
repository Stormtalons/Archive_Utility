package sw.archive

/*
	This class, in concert with XMLLine, facilitates the parsing of settings
	saved in XML. This custom API, as opposed to an existing third-party
	library, was created to eliminate the need for a dependency,
	because the functionality needed is very specific and limited, and to increase
	the control I had over the save format.
 */

class XMLBlock(l: Array[String])
{
	val lines = l
	for (i <- 0 to lines.length - 1)
		lines(i) = lines(i).trim

	def getBlockTag: String = new XMLLine(lines(0)).tag
	def getLine(i: Int): XMLLine = new XMLLine(lines(i))
	def getBlock(i: Int): XMLBlock =
	{
		val temp = lines.splitAt(i)._2
		val firstLine = new XMLLine(temp(0))
		var depth = 0
		var index = 0
		do
		{
			if (temp(index).startsWith("<" + firstLine.tag))
				depth += 1
			if (temp(index).equals("</" + firstLine.tag + ">") || (temp(index).startsWith("<" + firstLine.tag) && temp(index).endsWith("/>")))
				depth -= 1
			index += 1
		} while (index < temp.length && depth > 0)
		new XMLBlock(temp.splitAt(index)._1)
	}
}
