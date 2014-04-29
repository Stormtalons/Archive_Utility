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

class Setting[Any](display: Int, name: String, initialVal: Any, data: Int = Setting.DataType.NORMAL) extends HBox
{
	var displayType: Int = display
	var dataType: Int = data
	var value: Any = initialVal

	val label: Label = new Label(name)
	val valuePane: StackPane = new StackPane
	valuePane.setStyle("-fx-border-width: 1px; -fx-border-color: gray; -fx-border-style: solid")
	HBox.setHgrow(valuePane, Priority.ALWAYS)
	val valueLabel: Label = new Label(if (initialVal == null) "" else initialVal.toString)
	StackPane.setAlignment(valueLabel, Pos.CENTER_LEFT)
	valueLabel.setPadding(new Insets(0, 0, 0, 10))
	val valueField: TextField = new TextField
	valueField.setText(if (initialVal == null) "" else initialVal.toString)

	if (displayType == Setting.FIELD_ONLY)
		valueLabel.setVisible(false)
	else
		valueField.setVisible(false)

	if (displayType == Setting.LABEL_AND_FIELD)
	{
		valuePane.setOnMouseClicked(new EventHandler[MouseEvent]{def handle(evt: MouseEvent) = if (evt.getButton == MouseButton.PRIMARY && !valueField.isVisible) toggleEdit})
		valueField.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = toggleEdit})
		valueField.focusedProperty.addListener(new ChangeListener[java.lang.Boolean]{def changed(prop: ObservableValue[_ <: java.lang.Boolean], oldVal: java.lang.Boolean, newVal: java.lang.Boolean) = if (!newVal && valueField.isVisible) toggleEdit})
	}
	valuePane.getChildren.addAll(valueLabel, valueField)

	setSpacing(10)
	setAlignment(Pos.CENTER_LEFT)
	getChildren.addAll(label, valuePane)

	def addExtras(extras: Array[_ <: Node]) = if (extras != null) for (n <- extras) getChildren.add(n)

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
				set((if (value.isInstanceOf[Long]) java.lang.Long.parseLong(valueField.getText) else valueField.getText).asInstanceOf[Any])
				valueField.setVisible(false)
				valueLabel.setVisible(true)
			}
		})

	def get: Any = value
	def set(v: Any) =
	{
		value = v
		Main.fx(
		{
			val toSet = if (dataType == Setting.DataType.DATETIME) new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(value) else if (value != null) value.toString else ""
			valueLabel.setText(toSet)
			valueField.setText(toSet)
		})
	}
}
