package sw.archive

import javafx.scene.layout.{Priority, StackPane, HBox}
import javafx.scene.control.{TextField, Label}
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.event.{ActionEvent, EventHandler}
import javafx.geometry.{Insets, Pos}
import javafx.beans.value.{ObservableValue, ChangeListener}
import javafx.scene.Node
import java.text.SimpleDateFormat

/*
	This class consolidates all the functionality and the look-and-feel
	I wanted to be common to all settings displayable in the UI.
 */

object Setting
{
//Allows both read-only and editable setting fields.
	val LABEL_AND_FIELD = 0
	val LABEL_ONLY = 1
	val FIELD_ONLY = 2
}

class Setting(display: Int, name: String, initialValue: String) extends HBox
{
	def this(display: Int, name: String) = this(display, name, "")

//These constructors allow for custom edit handling behavior.
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

//The type of setting field this instance will be.
	private val displayType: Int = display

//UI tedium.
	setSpacing(10)
	setAlignment(Pos.CENTER_LEFT)
	private val label: Label = new Label(name)
	private val valuePane: StackPane = new StackPane
	valuePane.setStyle("-fx-border-width: 1px; -fx-border-color: gray; -fx-border-style: solid")
	HBox.setHgrow(valuePane, Priority.ALWAYS)
	private val valueLabel: Label = new Label(if (initialValue == null) "" else initialValue.toString)
	StackPane.setAlignment(valueLabel, Pos.CENTER_LEFT)
	valueLabel.setPadding(new Insets(0, 0, 0, 10))
	private val valueField: TextField = new TextField
	valueField.setText(if (initialValue == null) "" else initialValue.toString)
	valuePane.getChildren.addAll(valueLabel, valueField)
	getChildren.addAll(label, valuePane)

//Hides either the label or the text field, depending on setting.
	(if (displayType == Setting.FIELD_ONLY) valueLabel else valueField).setVisible(false)

//Only bother to set the edit handlers if the field is actually editable.
	if (displayType == Setting.LABEL_AND_FIELD) setEditHandler(toggleEdit)
	def setEditHandler(edit: => Unit) =
	{
		valuePane.setOnMouseClicked(new EventHandler[MouseEvent]{def handle(evt: MouseEvent) = if (evt.getButton == MouseButton.PRIMARY && !valueField.isVisible) edit})
		valueField.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = edit})
		valueField.focusedProperty.addListener(new ChangeListener[java.lang.Boolean]{def changed(prop: ObservableValue[_ <: java.lang.Boolean], oldVal: java.lang.Boolean, newVal: java.lang.Boolean) = if (!newVal && valueField.isVisible) edit})
	}

//Default editing behavior.
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

//Allow a setting to contain custom controls.
	def addExtras(extras: Array[_ <: Node]) = if (extras != null) for (n <- extras) getChildren.add(n)

//Getter & setter for the actual setting value.
	def get = if (valueLabel.isVisible) valueLabel.getText else valueField.getText
	def set(v: String) =
		Main.fx(
		{
			valueLabel.setText(v)
			valueField.setText(v)
		})
}
