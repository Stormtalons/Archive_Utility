package sw.archive

import javafx.scene.layout.VBox

class GroupManager extends VBox
{
	def count: Int = getChildren.size
	def add(group: MonitoredGroup) = Main.fx(getChildren.add(group))
	def get(i: Int): MonitoredGroup = getChildren.get(i).asInstanceOf[MonitoredGroup]
	def foreach(code: MonitoredGroup => Unit) = for (i <- 0 to count - 1) code(get(i))
	def refreshAll = foreach((group: MonitoredGroup) => group.refreshContents)
	def archiveAll = foreach((g: MonitoredGroup) => g.archive(null, false))
}
