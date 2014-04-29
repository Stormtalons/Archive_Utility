package sw.archive

import java.nio.file.{Paths, Files, Path}
import javafx.scene.control.{CheckBox, TreeItem}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.HBox
import javafx.scene.input.MouseEvent
import javafx.event.{ActionEvent, EventHandler}
import javafx.geometry.{Pos, Insets}
import javafx.beans.value.{ObservableValue, ChangeListener}
import java.lang

object Monitored
{
	val fileImg: Image = new Image(getClass.getResourceAsStream("res/file.png"))
	def fromXML(block: XMLBlock): Monitored =
	{
		val line = block.getLine(0)
		val toReturn = new Monitored(Paths.get(line.getAttr("path")), line.getAttr("relativePath"))
		if (line.getAttr("exclude") != null)
			toReturn.exclude.setSelected(!line.getAttr("exclude").toBoolean)
		if (line.getAttr("includeSubfolders") != null)
			toReturn.includeSubfolders = line.getAttr("includeSubfolders").toBoolean
		if (!line.isClosed)
		{
			var index = 1
			while (index < block.lines.length - 1)
			{
				val temp = block.getBlock(index)
				toReturn.add(Monitored.fromXML(temp))
				index += temp.lines.length
			}
		}
		toReturn
	}
}

class Monitored(f: Path, rp: String, p: Monitored, subfolders: Boolean, ex: Boolean, exViaParent: Boolean) extends TreeItem[String]
{
	var file: Path = f
	var relativePath = rp
	var parent: Monitored = p
	setValue(file.getFileName.toString)
	var includeSubfolders = subfolders
	var graphic: HBox = new HBox
	if (!Files.isDirectory(file) && Files.exists(file))
		graphic.getChildren.add(new ImageView(Monitored.fileImg))
	var exclude: CheckBox = new CheckBox
	exclude.setSelected(!ex)
	exclude.addEventFilter(ActionEvent.ACTION, new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = {toggleExcluded(exclude.isSelected); evt.consume}})
	graphic.getChildren.add(exclude)
	setGraphic(graphic)
	var excludeViaParent: Boolean = exViaParent
	var files: Array[Monitored] = Array()
	scan(null)

	def this(f: Path, rp: String, p: Monitored) = this(f, rp, p, true, false, false)
	def this(f: Path, rp: String, p: Monitored, subfolders: Boolean) = this(f, rp, p, subfolders, false, false)

	def add(file: Monitored): Unit =
	{
		for (i <- 0 to files.length - 1)
			if (files(i).equals(file))
			{
				files(i) = file
				return
			}
		files = files :+ file
	}

	def getMonitoredFile(p: Path): Monitored =
	{
		files.foreach(f =>
		{
			if (f.equals(p))
				return f
			val toReturn: Monitored = f.getMonitoredFile(p)
			if (toReturn != null)
				return toReturn
		})
		null
	}

	def scan(doFirst: (Monitored) => Unit, doLast: (Monitored) => Unit = null): Unit =
	{
		if (doFirst != null)
			doFirst(this)

		if (Files.isDirectory(file))
		{
			val it = Files.newDirectoryStream(file).iterator
			while (it.hasNext)
			{
				val p = it.next()
				if (getMonitoredFile(p) == null && (Files.isRegularFile(p) || includeSubfolders))
					files = files :+ new Monitored(p, relativePath + f.getFileName, this, includeSubfolders)
			}
			Main.fx(getChildren.clear)
			files.foreach(f =>
			{
				Main.fx(getChildren.add(f))
				f.scan(doFirst, doLast)
			})
		}

		if (doLast != null)
			doLast(this)
	}

	def doForAll(code: (Monitored) => Unit): Unit =
		if (code != null)
		{
			code(this)
			files.foreach(file => file.doForAll(code))
		}

	def toggleExcluded(ex: Boolean) =
	{
		exclude.setSelected(ex)

		def cascade(files: Array[Monitored], evp: Boolean): Unit =
		{
			files.foreach(file =>
			{
				file.excludeViaParent = evp
				file.exclude.setDisable(evp)
				if (file.exclude.isSelected)
					cascade(file.files, evp)
			})
		}
		cascade(files, !ex)
	}

	def equals(m: Monitored): Boolean = if (Files.exists(m.file) && Files.exists(file)) Files.isSameFile(m.file, file) else m.file.toString.equals(file.toString)
	def equals(p: Path): Boolean = if (Files.exists(p) && Files.exists(file)) Files.isSameFile(p, file) else p.toString.equals(file.toString)

	def toXML(indent: String): String =
	{
		val xml = new StringBuilder
		xml.append(indent + "<Monitored path=\"" + file + "\" ")
		xml.append("relativePath=\"" + relativePath + "\" ")
		xml.append("exclude=\"" + !exclude.isSelected + "\" ")
		xml.append("includeSubfolders=\"" + includeSubfolders + "\" ")
		if (files.length == 0)
			xml.append("/>\r\n")
		else
		{
			xml.append(">\r\n")
			files.foreach(file => xml.append(file.toXML(indent + "\t")) + "\r\n")
			xml.append(indent + "</Monitored>\r\n")
		}
		xml.toString
	}
}