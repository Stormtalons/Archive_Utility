package sw.archive

import javafx.scene.image.{ImageView, Image}
import java.nio.file.{Files, Path, Paths}
import javafx.scene.control.{CheckBox, TreeItem}
import javafx.scene.layout.HBox
import javafx.event.{EventHandler, ActionEvent}

object MonitoredItem extends Monitored
{
	val fileImg: Image = new Image(getClass.getResourceAsStream("res/file.png"))
	private implicit def getItem(line: XMLLine): MonitoredItem = new MonitoredItem(Paths.get(line.getAttr("path")), line.getAttr("relativePath"), Main.tryGet[Boolean](line.getAttr("includeSubfolders").toBoolean, true), Main.tryGet[Boolean](line.getAttr("exclude").toBoolean, false), false)
	def fromXML(block: XMLBlock): MonitoredItem = fromXML[MonitoredItem](block)
}
class MonitoredItem(f: Path, rp: String, subfolders: Boolean, ex: Boolean, pe: Boolean) extends TreeItem[String] with Monitored
{
	private val file: Path = f
	def getFile: Path = file
	def getFilePath: String = file.toString
	def getFileName: String = file.getFileName.toString

	private val relativePath: String = rp
	def getRelativePath: String = relativePath

	includeSubfolders = subfolders
	private var parentalExclusion: Boolean = pe
	def getParentalExclusion: Boolean = parentalExclusion
	def setParentalExclusion(evp: Boolean): Unit = parentalExclusion = evp
	private val graphic: HBox = new HBox
	if (!Files.isDirectory(file) && Files.exists(file))
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
		doForEach(child =>
		{
			child.setParentalExclusion(!ex)
			child.setDisabled(!ex)
			!child.isExcluded
		})
	}

	def setDisabled(disabled: Boolean): Unit = exclude.setDisable(disabled)

	def equals(item: MonitoredItem): Boolean = if (Files.exists(item.getFile) && Files.exists(getFile)) Files.isSameFile(item.getFile, file) else item.getFilePath.equals(getFilePath)
	def equals(p: Path): Boolean = if (Files.exists(p) && Files.exists(file)) Files.isSameFile(p, file) else p.toString.equals(getFilePath)

	def getXML: (String, String) = ("MonitoredItem", "path=\"" + getFilePath + "\" relativePath=\"" + getRelativePath + "\" exclude=\"" + isExcluded + "\" includeSubfolders=\"" + getIncludeSubfolders + "\" ")
}