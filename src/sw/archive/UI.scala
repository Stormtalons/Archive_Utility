package sw.archive

import javafx.application.Application
import javafx.stage.Stage
import javafx.scene.control.{Button, ListView}
import javafx.scene.layout.{Priority, HBox}
import javafx.event.{ActionEvent, EventHandler}
import javafx.scene.Scene

class UI extends Application
{
	def launch = javafx.application.Application.launch()

	def start(stg: Stage) =
	{
		val list = new ListView[String]
		HBox.setHgrow(list, Priority.ALWAYS)
		for (s <- Main.sb.toString.split("\n"))
			list.getItems.add(s)
		val b = new Button("Go")
		b.setOnAction(new EventHandler[ActionEvent]{def handle(evt: ActionEvent) = if (list.getSelectionModel.getSelectedItem != null) Main.temp.archive(list.getSelectionModel.getSelectedItem)})
		b.setStyle("-fx-font-size: 30pt")
		val hb = new HBox
		hb.getChildren.addAll(list, b)
		stg.setScene(new Scene(hb, 1200, 1000))
		stg.show
	}
}