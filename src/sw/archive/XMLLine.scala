package sw.archive

class XMLLine(l: String)
{
	private val str = l.trim
	var tag: String = ""
	var isClosed: Boolean = str.endsWith("/>")
	if (str.startsWith("</"))
	{
		tag = str.substring(2, str.length - 1)
		isClosed = true
	}
	else
		tag = str.substring(1, str.indexOf(' '))
	var attributes: Map[String, String] = Map()
	private var index = 0
	while (index < str.length)
	{
		var item = ""
		var quoteCount = 0
		while (index < str.length && (str.charAt(index) != ' ' || quoteCount % 2 != 0))
		{
			if (str.charAt(index) == '\"')
				quoteCount += 1
			item += str.charAt(index)
			index += 1
		}
		if (item.contains("="))
		{
			val (attr, value) = item.splitAt(item.indexOf('='))
			attributes += (attr -> value.substring(2, value.length - 1))
		}
		index += 1
	}

	def getAttr(attr: String): String = if (attributes.contains(attr)) attributes.get(attr).get else null
}