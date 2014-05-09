package sw.archive

import javafx.scene.layout.VBox

/*
	Provides support for any actions that should happen in all defined MonitoredGroups,
	and several convenience methods.
 */

class GroupManager extends VBox
{
	def count: Int = getChildren.size
	def add(group: MonitoredGroup) = Main.fx(getChildren.add(group))
	def get(i: Int): MonitoredGroup = getChildren.get(i).asInstanceOf[MonitoredGroup]
	def foreach(doWith: MonitoredGroup => Unit) = for (i <- 0 to count - 1) doWith(get(i))
	def refreshAll = foreach((group: MonitoredGroup) => group.refreshContents)
	def archiveAll = foreach((group: MonitoredGroup) => group.archive(null, false))
}
