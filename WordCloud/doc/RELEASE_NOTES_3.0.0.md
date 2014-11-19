WordCloud 3.0.0 Release Notes
=============================

* [WordCloud on the Cytoscape App Store](http://apps.cytoscape.org/apps/wordcloud) 
* [WordCloud on GitHub](https://github.com/BaderLab/WordCloudPlugin)

New Features
------------
* Cloud display panels can be undocked into their own frame.
* The configuration panel has been redesigned to be more usable.
* Word delimiters and word filters are now configured using
a separate dialog.
* All of the controls on the configuration panel will automatically
update the cloud.
* "Selected Nodes" setting constantly updates the cloud based on the currently selected nodes.
* The cloud list can be sorted alphabetically.
* Added a minimum word occurrences setting.
* WordCloud panels can be hidden when not in use.

Major Fixes
-----------
* Clouds are generated on a background thread so that the UI does not hang.
* Performance of cloud generation has been significantly improved.
* Word parsing algorithm has been rewritten to better handle delimiters.
* WordCloud 2.0 created a column in the default node table for each cloud. Clouds created
for separate subnetworks would overwrite each other's selections. WordCloud 3.0 now creates
columns in each subnetwork's local table. (Reading clouds from the default table is still
supported for backwards compatibility.)


Minor Fixes
-----------
* For a full list of fixed defects see the [GitHub issue tracker for WordCloud.](https://github.com/BaderLab/WordCloudPlugin/issues?q=is%3Aissue+is%3Aclosed.)