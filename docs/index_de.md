---
description: >-
  Dies ist ein Administration Plugin für Goobi workflow. Es ermöglicht die Bearbeitung von Ruleset-Dateien direkt aus der Benutzeroberfläche von Goobi Workflow.
  
---

Regelsatzeditor
===========================================================================


Einführung
---------------------------------------------------------------------------
Dieses Plugin dient zur direkten Bearbeitung der Regelsatzdateien von Goobi workflow direkt aus der Benutzeroberfläche innerhalb des Webbrowsers.

Übersicht
---------------------------------------------------------------------------

Details             |  Erläuterung
------------------- | -----------------------------------------------------
Identifier          | intranda_administration_ruleset_editor
Source code         | [https://github.com/intranda/goobi-plugin-administration-ruleset-editor](https://github.com/intranda/goobi-plugin-administration-ruleset-editor)
Lizenz              | GPL 2.0 oder neuer 
Dokumentationsdatum | 06.11.2021


Arbeitsweise des Plugins
---------------------------------------------------------------------------

Nach der Installation ist das Plugin in einem eigenen Eintrag im Menü `Administration` zu finden, von wo es geöffnet werden kann.

![Geöffnetes Plugin ohne geladene Datei](intranda_administration_ruleset_editor3_de.png)

Nach dem Öffnen werden auf der linken Seite alle Regelsätze von Goobi aufgelistet. Diese kann man durch Anklicken des jeweiligen Icons öffnen, um sie zu bearbeiten.

![Geöffnetes Plugin mit geladener Datei](intranda_administration_ruleset_editor4_de.png)

Öffnet man eine Datei, erscheint auf der rechten Seite ein Texteditor, in dem die Datei bearbeitet werden kann. Bearbeitet und speichert man eine Datei, wird im definierten Backupverzeichnis automatisch ein Backup angelegt. 

![Gespeicherte Datei](intranda_administration_ruleset_editor5_de.png)

Entsprechend des eingestellten Wertes in der Konfigurationsdatei bleibt hier eine gewisse Anzahl an älteren Backups erhalten, bevor diese durch neuere ersetzt werden.

![Dateien innerhalb des Backup-Verzeichnisses](intranda_administration_ruleset_editor7.png)

Wurde eine Datei verändert und wird ohne zuvor zu speichern ein Wechsel zu einer anderen Datei versucht, bekommt der Beareiter eine Rückfrage, wie mit den Änderungen zu verfahren ist.

![Nachfrage bei ungespeicherten Daten](intranda_administration_ruleset_editor6_de.png)


Installation
---------------------------------------------------------------------------
Das Plugin besteht insgesamt aus den folgenden zu installierenden Dateien:

```text
plugin_intranda_administration_ruleset_editor.jar
plugin_intranda_administration_ruleset_editor-GUI.jar
plugin_intranda_administration_ruleset_editor.xml
```

Diese Dateien müssen in den richtigen Verzeichnissen installiert werden, so dass diese nach der Installation unter den folgenden Pfaden vorliegen:

```bash
/opt/digiverso/goobi/plugins/administration/plugin_intranda_administration_ruleset_editor.jar
/opt/digiverso/goobi/plugins/GUI/plugin_intranda_administration_ruleset_editor-GUI.jar
/opt/digiverso/goobi/config/plugin_intranda_administration_ruleset_editor.xml
```


Konfiguration
---------------------------------------------------------------------------
Die Konfiguration des Plugins erfolgt über die Konfigurationsdatei `plugin_intranda_administration_ruleset_editor.xml` und kann im laufenden Betrieb angepasst werden. Im folgenden ist eine beispielhafte Konfigurationsdatei aufgeführt:

```xml
<config_plugin>
	
	<rulesetDirectory>/opt/digiverso/goobi/rulesets/</rulesetDirectory>
	<!-- By editing a ruleset file in the browser GUI, a backup file will be stored in the backup directory -->
	<rulesetBackupDirectory>/opt/digiverso/goobi/rulesets/backup/</rulesetBackupDirectory>
	<!-- backup files will be stored as ruleset.xml.1, ruleset.xml.2, ..., ruleset.xml.n -->
	<numberOfBackupFiles>10</numberOfBackupFiles>
	
</config_plugin>
```

Die Parameter innerhalb dieser Konfigurationsdatei haben folgende Bedeutungen:

Parameter           |  Erläuterung
------------------- | ----------------------------------------------------- 
`rulesetDirectory`         | Dies ist der Pfad, an dem sich die Regelsatzdateien befinden.
`rulesetBackupDirectory`   | Hiermit wird der Pfad für die Backup-Dateien festgelegt, wo nach dem Bearbeiten die Backups der Regelsatzdateien gespeichert werden sollen.
`numberOfBackupFiles`         | Dieser ganzzahlige Wert gibt an, wie viele Backup-Dateien pro Regelsatzdatei gespeichert bleiben, bevor sie durch neue Backups überschrieben werden.


Einrichtung benötigter Berechtigungen
---------------------------------------------------------------------------
Dieses Plugin verfügt über eine eigene Berechtigungsstufe für die Verwendung. Aus diesem Grund müssen Nutzer über die erforderlichen Rechte verfügen. 

![Kein Zugriff ohne korrekte Rechte](intranda_administration_ruleset_editor1_de.png)

Bitte weisen Sie daher der Benutzergruppe der entsprechenden Nutzer das folgende Recht zu:

```
Plugin_administration_ruleset_editor
```

![Korrekt zugewiesenes Recht für die Nutzer](intranda_administration_ruleset_editor2_de.png)