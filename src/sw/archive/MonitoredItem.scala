package sw.archive

import javafx.scene.image.{ImageView, Image}
import java.nio.file.{Files, Path, Paths}
import javafx.scene.control.{CheckBox, TreeItem}
import javafx.scene.layout.HBox
import javafx.event.{EventHandler, ActionEvent}

object MonitoredItem extends Monitored
{
	val fileImg: Image = new Image(getClass.getResourceAsStream("res/file.png"))
	implicit def getItem(line: XMLLine): MonitoredItem = new MonitoredItem(Paths.get(line.getAttr("path")), line.getAttr("relativePath"), Main.tryGet[Boolean](line.getAttr("includeSubfolders").toBoolean, true), Main.tryGet[Boolean](line.getAttr("exclude").toBoolean, false), false, Main.tryGet[Int](line.getAttr("relativePath").length, 0) == 0)
	def fromXML(block: XMLBlock): MonitoredItem = fromXML[MonitoredItem](block)
}
class MonitoredItem(f: Path, rp: String, subfolders: Boolean, ex: Boolean, pe: Boolean, dfp: Boolean = false) extends TreeItem[String] with Monitored
{
	private val file: Path = f
	def getFile: Path = file
	def getFilePath: String = Main.formatFilePath(getFile.toString)
	def getFileName: String = getFile.getFileName.toString + (if (isDir) "/" else "")
	def isDir: Boolean = Files.isDirectory(getFile)
	def exists: Boolean = Files.exists(file)
	def isSameFile(toCheck: MonitoredItem): Boolean = if (toCheck.exists && exists) Files.isSameFile(toCheck.getFile, getFile) else toCheck.getFilePath.equals(getFilePath)
	def isSameFile(toCheck: Path): Boolean = if (Files.exists(toCheck) && exists) Files.isSameFile(toCheck, getFile) else toCheck.toString.equals(getFilePath)

	setValue(if (dfp) getFilePath else getFileName)

	private val relativePath: String = rp
	def getRelativePath: String = relativePath

	setIncludeSubfolders(subfolders)

	private var parentalExclusion: Boolean = pe
	def getParentalExclusion: Boolean = parentalExclusion
	def setParentalExclusion(evp: Boolean): Unit = parentalExclusion = evp
	private val graphic: HBox = new HBox
	if (!isDir && exists)
		graphic.getChildren.add(new ImageView(MonitoredItem.fileImg))
	private val exclude: CheckBox = new CheckBox
	exclude.setSelected(!ex)
	exclude.addEventFilter(ActionEvent.ACTION, new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = {toggleExcluded(exclude.isSelected); evt.consume}})
	def isExcluded: Boolean = !exclude.isSelected
	graphic.getChildren.add(exclude)
	setGraphic(graphic)

	def toggleExcluded(ex: Boolean) =
	{
		exclude.setSelected(ex)
		forEachChild(child =>
		{
			child.setParentalExclusion(!ex)
			child.setDisabled(!ex)
			!child.isExcluded
		})
	}

	def setDisabled(disabled: Boolean): Unit = exclude.setDisable(disabled)

	def getXML: (String, String) = ("MonitoredItem", "path=\"" + getFilePath + "\" relativePath=\"" + getRelativePath + "\" exclude=\"" + isExcluded + "\" includeSubfolders=\"" + getIncludeSubfolders + "\" ")
}