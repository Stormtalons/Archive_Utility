package sw.archive

import javafx.scene.layout.VBox

class GroupManager extends VBox
{
	def count: Int = getChildren.size
	def add(group: MonitoredGroup) =
	{
		Main.fx(getChildren.add(group))
		//group.refreshContents
	}
	def get(i: Int): MonitoredGroup = getChildren.get(i).asInstanceOf[MonitoredGroup]
	def foreach(code: MonitoredGroup => Unit) = for (i <- 0 to count - 1) code(get(i))
	def archiveAll = foreach((g: MonitoredGroup) => g.archive(null, false))
}
