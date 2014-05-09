package sw.archive

import java.nio.file._
import javafx.scene.layout.GridPane
import javafx.geometry.Insets

/*
	This class represents a location to archive files to, and
	facilitates archival of individual files as requested.
	
	TODO: Add support for archival via FTP.

	TODO: Add support for archival to a database.

	TODO: Add support for an experimental P2P archiving system, mediated via SQL.
		Preliminary design vision:

		- Consider 2 separate installations of this application - Instance A, and Instance B.
			Both of these are independent, but they each report activity and list of
			monitored files to a common SQL server.

		- Before archiving a file, Instance A first checks SQL to see if any peers already
			track/archive that file (determined based on hash matching). It finds that B does.

		- A determines, based on user configuration, that B's ownership of the file constitutes
			a valid, reliable backup, and decides not to archive the file itself.

		- A loses the file, and needs to restore it. It asks SQL for B's contact info
			(IP address, port, and some form of authentication), requests the file directly
			from B. B complies and transfers it directly to A. File is restored, the purpose
			of archival achieved, and A saves on disk space.
 */

object Archive
{
//For loading settings from file.
	def fromXML(l: XMLLine): Archive = new Archive(Paths.get(l.getAttr("path")), l.getAttr("name"))
}

class Archive(p: Path, n: String) extends GridPane
{
	setPadding(new Insets(10))
	setHgap(10)
	setVgap(10)
	getStyleClass.add("archive")
	getStylesheets.add("sw/archive/res/dft.css")

//Definition, getter & setter for the archive name.
	private val archiveName: Setting = new Setting(Setting.LABEL_AND_FIELD, "Name", n)
	def getName: String = archiveName.get
	def setName(name: String) = archiveName.set(name)
	add(archiveName, 0, 0)

//Definition, getter & setter for the archive directory.
	private val archivePath: Setting = new Setting(Setting.LABEL_AND_FIELD, "Path", if (p == null) "" else Main.formatFilePath(p.toString))
	archivePath.setPrefWidth(700)
	def getArchivePath: String = archivePath.get
	def setArchivePath(path: Path) = archivePath.set(Main.formatFilePath(path.toString))
	add(archivePath, 0, 1)
	
//Archives a single file, creating directories within its configured archive directory if needed.
	def archive(from: MonitoredItem) =
		if (!from.isDir && !from.isExcluded && !from.getParentalExclusion)
		{
			val archivePath = getArchivePath + Main.formatFilePath(from.getRelativePath)
			if (!Files.exists(Paths.get(archivePath)))
				Files.createDirectories(Paths.get(archivePath))
			Files.copy(from.getFile, Paths.get(archivePath + from.getFileName), StandardCopyOption.REPLACE_EXISTING)
		}
	
//For saving settings to file.
	def toXML: String = "<Archive name=\"" + getName + "\" path=\"" + getArchivePath + "\" />\r\n"
}
