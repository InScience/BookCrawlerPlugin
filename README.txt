This README file describes steps to set-up plug-in and configure Nutch to use it.

Documentation
-------------------

	* This tutorial describes the installation and use of Nutch 1.x.
		https://wiki.apache.org/nutch/NutchTutorial
	* AboutPlugins
		https://wiki.apache.org/nutch/AboutPlugins
	* Jaunt-api Java library for web-scrapping.
		http://jaunt-api.com/

Directories
-----------

%NUTCH_HOME% - directory where Nutch Crawler is installed.
%SOLR_HOME% - parent directory of SOLR.

Plug-in includes
----------------

	client.jar
	index-njp.jar
	jaunt1.1.1.jar
	plugin.xml
	start
	table.jar
	README.txt

	NOTE: while server is not configured to find Apache Phoenix library(phoenix-4.6.0-HBase-0.98-client.jar), it must be included into dependencies!!!

Description
-----------

	* client.jar - Java archive contains classes to work with database. 
	* index-njp.jar - this is a main Java archive. It contains Parser class with filter method 
		which is invoked by Nutch Crawler.
	* jaunt1.1.1.jar - Java archive contains web page scrapper.
	* plugin.xml - configuration file of plug-in.
	* start - file which invokes Nutch Crawler. Place it into %NUTCH_HOME% directory.
	* table.jar - Java archive contains classes to work with (in this case) authors, categories and books.
	
	*** phoenix-4.6.0-HBase-0.98-client.jar - added while server is not configured to find phoenix library on cluster. 
	*** Remove <library name="phoenix-4.6.0-HBase-0.98-client.jar"/> when server will be configured!!!

Installation
------------

	* Copy plug-in files to $NUTCH_HOME%/plugins.
	* Copy "start" file to $NUTCH_HOME% root directory.
	* Check plug-in configuration.
	* Configure Nutch.
	
WARNING!
-------

	"Jaunt is freely available for personal and commercial use under the following license. 
	Both the software and the license expire at the end of every month, at which point the most recent version should be downloaded. "
	Jaunt asks to frequently re-download their library.
	Check if new version is available or if Parser fails with error message: "JAUNT HAS EXPIRED! [http://jaunt-api.com]".
		
Plug-in configuration
---------------------

	plugin.xml should be checked before running Nutch Crawler!
	* Check file names and versions are correct.
		<library name="index-njp.jar">
			<export name="*"/>
		</library>
		<library name="jaunt1.1.1.jar"/>
		<library name="table.jar"/>
		<library name="client.jar"/>
	Temporary <library name="phoenix-4.6.0-HBase-0.98-client.jar"/> . Remove when server will be configured!!!

	* In case if Jaunt library is re-downloaded, change:
		<library name="jauntOLD_VERSION.jar"/> to <library name="jauntNEW_VERSION.jar"/>
	* DO NOT TOUCH this (it shows where is main class containing filter method):
		  <extension id="com.edgars.nutch.Parser"
			name="Nutch Jaunt Parser"
			point="org.apache.nutch.indexer.IndexingFilter">
				<implementation id="index-njp"
					class="com.edgars.nutch.Parser"/>
			</extension>
	
Nutch Configuration
-------------------

	* %NUTCH_HOME%/conf/nutch-site.xml - check if http.agent.name and http.robots.agents are set.
	* In nutch-site.xml at "plugin.includes" include plug-in "index-njp" (folder where is copied file should be the same name).
		Example: index-(basic|email|njp)
	* Nutch uses solr for indexing links. 
	* In nutch-site.xml at "plugin.includes" include "indexer-solr".
	* In nutch-site.xml at "plugin.includes" include "urlfilter-regex" if you want to filter urls (should be included by default).
	* Example line of "plugin.includes":
		<value>indexer-solr|urlfilter-regex|nutch-extensionpoints|protocol-(http|httpclient)|parse-(text|html)|index-(basic|email|njp)|query-(basic|site|url)</value>
	* %NUTCH_HOME%/conf/schema.xml must match schema.xml in solr. So far everything is working with these versions of Nutch & Solr. Better DO NOT
		change anything there if not sure.	

Starting the Nutch
------------------

	Execute start file from %NUTCH_HOME% root directory.
	Example: ./start
	* If getting error JAVA_HOME is not set, then type: export JAVA_HOME=/usr/
		
Start
-----

	Check for commands in Nutch tutorial.
	NOTE: Solr must be running to use Nutch!!!
	    * To Start Solr, go to %SOLR_HOME%/example and in command line type "nohup java -jar start.jar > logfile 2>&1 &". This should
	    start Solr in background.
	
	Last line more in details:
		crawl <seedDir> <crawlDir> <solrURL> <numberOfRounds>
		bin/crawl urls manoknyga http://158.129.140.188:8983/solr/ 1 -filter
	
		bin/crawl - %NUTCH_HOME%/bin/crawl file.
		seedDir - Directory in which to look for a seeds file (in this case urls/seed.txt).
		crawlDir - Directory where the crawl/link/segments dirs are saved.
		solrURL - Url of working SOLR.
		numberOfRounds - The number of rounds to run this crawl for.
	
	If url of SOLR or anything changes, just open "start" file and change..

Build
-----

	Project build on:
	* IntelliJ IDEA 14.1.5
	* JDK 1.7

	Guides to build .jar for IntelliJ IDEA:
	* https://www.jetbrains.com/idea/help/opening-reopening-and-closing-projects.html#d1435176e136
	* https://www.jetbrains.com/idea/help/configuring-artifacts.html
	* https://www.jetbrains.com/idea/help/packaging-a-module-into-a-jar-file.html

	Step-by-step guide:
	* Open project
	1. On Welcome screen, click Open
	    * Or Main menu, choose File | Open
	2. In the Open Project dialog box, navigate to the desired *.ipr, .classpath, .project, or pom.xml, file
	    or to the folder marked with the IDEA icon, that contains project .idea directory.
    3. In the Select Path dialog box, select the directory that contains the desired project.
    4. Specify whether you want to open the project in a new frame, or close the current project and reuse the existing frame.

    * READ: https://www.jetbrains.com/idea/help/opening-reopening-and-closing-projects.html#d1435176e136

    * Manage the list of project artifacts
    1. Open the Project Structure settings.
    2. Click Artifacts to open the Artifacts page. The page shows all the artifacts that are available in the project.
        Manage the list using the toolbar buttons:
        * To create an artifact, click the New button + and choose the artifact type
            (JAR, WAR, EAR, Android, or an exploded directory) in the New drop-down list.
        * To remove an artifact, select it and click the Remove button -.
        * To view a list of artifacts in which the selected artifact is included, click the Find Usages button find.
        * To edit an artifact, select it and update its settings in the Artifact Layout pane that opens.

        ** Project Structure | Project Settings | Artifacts | Jar | Empty
        OR
        ** Project Structure | Project Settings | Artifacts | Jar | From modules with dependencies IF dependencies are needed

    * READ: https://www.jetbrains.com/idea/help/configuring-artifacts.html#d1640450e127

    * Configure an artifact
    1. In the list of artifacts, select the one to be configured. Its settings are displayed in the Artifact Layout pane.
    2. Specify the general settings of the artifact.
    3. Complete the artifact definition by following these general steps:
        * Configure the artifact structure.
        * Add resources to the artifact.
        * Arrange the elements included in the artifact.
        * If necessary, specify additional activities to be performed before and after building the artifact in the Pre-processing and Post-Processing tabs.

    * READ: https://www.jetbrains.com/idea/help/configuring-artifacts.html#d1640450e191

    * Build a JAR file from a module
    1. On the main menu, choose Build | Build Artifact.
    2. From the drop-down list, select the desired artifact of the type JAR.

    ** Build | Build Artifact

    READ: https://www.jetbrains.com/idea/help/packaging-a-module-into-a-jar-file.html

Contacts
--------

	* Author: Edgars Fjodorovs