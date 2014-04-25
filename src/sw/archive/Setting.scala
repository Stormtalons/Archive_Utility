package sw.archive

import javafx.scene.layout.{StackPane, HBox}
import javafx.scene.control.{TextField, Label}
import javafx.scene.input.{MouseButton, MouseEvent}
import javafx.event.{ActionEvent, EventHandler}
import javafx.geometry.{Insets, Pos}
import javafx.beans.value.{ObservableValue, ChangeListener}
import javafx.scene.Node

object Setting
{
	val LABEL_AND_FIELD = 0
	val LABEL_ONLY = 1
	val FIELD_ONLY = 2
}

class Setting(st: Int, extra: Node*) extends HBox
{
	var settingType: Int = st

	val label: Label = new Label
	val value: StackPane = new StackPane
	value.setStyle("-fx-border-width: 1px; -fx-border-color: gray; -fx-border-style: solid")
	val valueLabel: Label = new Label
	StackPane.setAlignment(valueLabel, Pos.CENTER_LEFT)
	valueLabel.setPadding(new Insets(0, 0, 0, 10))
	val valueField: TextField = new TextField
	
	if (settingType == Setting.LABEL_AND_FIELD)
	{
		value.setOnMouseClicked(new EventHandler[MouseEvent]{def handle(evt: MouseEvent) = if (evt.getButton == MouseButton.PRIMARY && !valueField.isVisible) toggleEdit})
		valueField.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = toggleEdit})
		valueField.focusedProperty.addListener(new ChangeListener[java.lang.Boolean]{def changed(prop: ObservableValue[_ <: java.lang.Boolean], oldVal: java.lang.Boolean, newVal: java.lang.Boolean) = if (!newVal && valueField.isVisible) toggleEdit})
		valueField.setVisible(false)
	}
	if (settingType != Setting.FIELD_ONLY)
		value.getChildren.add(valueLabel)
	if (settingType != Setting.LABEL_ONLY)
		value.getChildren.add(valueField)

	setAlignment(Pos.CENTER_LEFT)
	getChildren.addAll(label, value)
	for (n <- extra)
		getChildren.add(n)

	def this(st: Int, n: String, extra: Node*) =
	{
		this(st, extra)
		label.setText(n + ":")
	}

	def this(st: Int, n: String, iv: String, extra: Node*) =
	{
		this(st, n, extra)
		setValue(iv)
	}

	def toggleEdit =
	{
		Main.fx(
		{
			val (vis, invis) = if (valueLabel.isVisible) (valueLabel, valueField) else (valueField, valueLabel)
			if (invis.getClass.equals(classOf[Label]))
				invis.asInstanceOf[Label].setText(vis.asInstanceOf[TextField].getText)
			else
				invis.asInstanceOf[TextField].setText(vis.asInstanceOf[Label].getText)
			vis.setVisible(false)
			invis.setVisible(true)
			invis.requestFocus
		})
	}
	
	def setValue(s: String) =
	{
		Main.fx(
		{
			valueLabel.setText(s)
			valueField.setText(s)
		})
	}
	
	def getValue: String = if (settingType == Setting.FIELD_ONLY) valueField.getText else valueLabel.getText
}
