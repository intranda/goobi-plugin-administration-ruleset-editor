---
description: >-
  This is an Administration Plugin for Goobi workflow. It allows to edit ruleset xml files directly from the user interface of Goobi workflow.
  
---

Ruleset editor
===========================================================================


Introduction
---------------------------------------------------------------------------
This plugin is used to directly edit the ruleset files of Goobi workflow directly from the user interface within the web browser.


Overview
---------------------------------------------------------------------------

Details             |  Description
------------------- | -----------------------------------------------------
Identifier          | intranda_administration_ruleset_editor |
Source code         | [https://github.com/intranda/goobi-plugin-administration-ruleset-editor](https://github.com/intranda/goobi-plugin-administration-ruleset-editor)
License             | GPL 2.0 or newer 
Documentation date  | 06.11.2021


Arbeitsweise des Plugins
---------------------------------------------------------------------------

After installation, the plugin can be found in its own entry in the `Administration` menu, from where it can be opened.

![Open plugin without loaded file](intranda_administration_ruleset_editor3_en.png)

After opening, all Goobi ruleset files are listed on the left-hand side. These can be opened by clicking on the respective icon in order to edit them.

![Open plugin with loaded file](intranda_administration_ruleset_editor4_en.png)

If you open a file, a text editor appears on the right-hand side in which the file can be edited. If you edit and save a file, a backup is automatically created in the defined backup directory. 

![Saved file](intranda_administration_ruleset_editor5_en.png)

According to the value set in the configuration file, a certain number of older backups are retained here before they are replaced by newer ones.

![Files within the backup directory](intranda_administration_ruleset_editor7.png)

If a file has been changed and an attempt is made to change to another file without saving it, the operator is asked how to proceed with the changes.

![Demand for unsaved data](intranda_administration_ruleset_editor6_en.png)


Installation
---------------------------------------------------------------------------
The plugin consists in total of the following files to be installed:

```text
plugin_intranda_administration_ruleset_editor.jar
plugin_intranda_administration_ruleset_editor-GUI.jar
plugin_intranda_administration_ruleset_editor.xml
```

These files must be installed in the correct directories so that they are available under the following paths after installation:

```bash
/opt/digiverso/goobi/plugins/administration/plugin_intranda_administration_ruleset_editor.jar
/opt/digiverso/goobi/plugins/GUI/plugin_intranda_administration_ruleset_editor-GUI.jar
/opt/digiverso/goobi/config/plugin_intranda_administration_ruleset_editor.xml
```


Configuration
---------------------------------------------------------------------------
The plugin is configured via the configuration file `plugin_intranda_administration_ruleset_editor.xml` and can be adapted during operation. The following is an example configuration file:

```xml
<config_plugin>
	
	<rulesetDirectory>/opt/digiverso/goobi/rulesets/</rulesetDirectory>
	<!-- By editing a ruleset file in the browser GUI, a backup file will be stored in the backup directory -->
	<rulesetBackupDirectory>/opt/digiverso/goobi/rulesets/backup/</rulesetBackupDirectory>
	<!-- backup files will be stored as ruleset.xml.1, ruleset.xml.2, ..., ruleset.xml.n -->
	<numberOfBackupFiles>10</numberOfBackupFiles>
	
</config_plugin>
```

The parameters within this configuration file have the following meanings:

Parameter           |  Description
------------------- | ----------------------------------------------------- 
`rulesetDirectory`         | This is the path where the ruleset files are located.
`rulesetBackupDirectory`   | This sets the path for the backup files where the backups of the ruleset files are to be saved after editing.
`numberOfBackupFiles`         | This integer value specifies how many backup files remain stored per ruleset file before they are overwritten by new backups.


Setting up required rights
---------------------------------------------------------------------------
This plugin has its own permission level for use. For this reason, users must have the necessary rights. 

![Kein Zugriff ohne korrekte Rechte](intranda_administration_ruleset_editor1_en.png)

Therefore, please assign the following right to the user group of the corresponding users:

```
Plugin_administration_ruleset_editor
```

![Korrekt zugewiesenes Recht für die Nutzer](intranda_administration_ruleset_editor2_en.png)