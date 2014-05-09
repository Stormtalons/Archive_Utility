package sw.archive

import javafx.scene.layout.{Priority, HBox}
import javafx.scene.control.{SplitPane, ScrollPane}
import javafx.geometry.Orientation

/*
	Simple class that provides support for any actions that
	should happen in all defined MonitoredGroups, and handles
	layout management duties.
 */

class GroupManager extends SplitPane
{
	setOrientation(Orientation.VERTICAL)

	private var groups: Array[MonitoredGroup] = Array()
	def add(group: MonitoredGroup) =
	{
		HBox.setHgrow(group, Priority.ALWAYS)
		groups = groups :+ group
		Main.fx(
			if (getItems.size == 0 || getItems.get(getItems.size - 1).asInstanceOf[HBox].getChildren.size == 2)
			{
				val tempHBox = new HBox
				tempHBox.getChildren.add(group)
				getItems.add(tempHBox)
			}
			else
				getItems.get(getItems.size - 1).asInstanceOf[HBox].getChildren.add(group))
	}

	def refreshAll = groups.foreach(group => group.refreshContents)
	def archiveAll = groups.foreach(group => group.archive(null, false))
	def toXML(toWrite: StringBuilder) = groups.foreach(group => toWrite.append(group.toXML(group.getXML, "")))
}
