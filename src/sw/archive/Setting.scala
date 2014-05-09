package sw.archive

import javafx.scene.layout.{Priority, StackPane, HBox}
import javafx.scene.control.{TextField, Label}
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.event.{ActionEvent, EventHandler}
import javafx.geometry.{Insets, Pos}
import javafx.beans.value.{ObservableValue, ChangeListener}
import javafx.scene.Node
import java.text.SimpleDateFormat

object Setting
{
	val LABEL_AND_FIELD = 0
	val LABEL_ONLY = 1
	val FIELD_ONLY = 2

	object DataType
	{
		val NORMAL = 0
		val DATETIME = 1
	}
}

class Setting(display: Int, name: String, initialValue: String) extends HBox
{
	def this(display: Int, name: String) = this(display, name, "")
	def this(display: Int, name: String, edit: => Unit) =
	{
		this(display, name)
		setEditHandler(edit)
	}
	def this(display: Int, name: String, initialValue: String, edit: => Unit) =
	{
		this(display, name, initialValue)
		setEditHandler(edit)
	}

	var displayType: Int = display

	private val label: Label = new Label(name)
	private val valuePane: StackPane = new StackPane
	valuePane.setStyle("-fx-border-width: 1px; -fx-border-color: gray; -fx-border-style: solid")
	HBox.setHgrow(valuePane, Priority.ALWAYS)
	private val valueLabel: Label = new Label(if (initialValue == null) "" else initialValue.toString)
	StackPane.setAlignment(valueLabel, Pos.CENTER_LEFT)
	valueLabel.setPadding(new Insets(0, 0, 0, 10))
	private val valueField: TextField = new TextField
	valueField.setText(if (initialValue == null) "" else initialValue.toString)

	(if (displayType == Setting.FIELD_ONLY) valueLabel else valueField).setVisible(false)
	if (displayType == Setting.LABEL_AND_FIELD) setEditHandler(toggleEdit)

	valuePane.getChildren.addAll(valueLabel, valueField)

	setSpacing(10)
	setAlignment(Pos.CENTER_LEFT)
	getChildren.addAll(label, valuePane)

	def addExtras(extras: Array[_ <: Node]) = if (extras != null) for (n <- extras) getChildren.add(n)

	def setEditHandler(edit: => Unit) =
	{
		valuePane.setOnMouseClicked(new EventHandler[MouseEvent]{def handle(evt: MouseEvent) = if (evt.getButton == MouseButton.PRIMARY && !valueField.isVisible) edit})
		valueField.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = edit})
		valueField.focusedProperty.addListener(new ChangeListener[java.lang.Boolean]{def changed(prop: ObservableValue[_ <: java.lang.Boolean], oldVal: java.lang.Boolean, newVal: java.lang.Boolean) = if (!newVal && valueField.isVisible) edit})
	}

	def toggleEdit =
		Main.fx(
		{
			if (valueLabel.isVisible)
			{
				valueLabel.setVisible(false)
				valueField.setVisible(true)
				valueField.requestFocus
			}
			else
			{
				set(valueField.getText)
				valueField.setVisible(false)
				valueLabel.setVisible(true)
			}
		})

	def get = if (valueLabel.isVisible) valueLabel.getText else valueField.getText
	def set(v: String) =
		Main.fx(
		{
			valueLabel.setText(v)
			valueField.setText(v)
		})
}
