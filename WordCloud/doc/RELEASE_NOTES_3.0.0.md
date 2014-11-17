WordCloud 3.0.0 Release Notes
=============================


New Features
------------
* Cloud display panels can be undocked into their own frame.
* The configuration panel has been redesigned to be more usable.
* There is a new "sync with selection" feature that allows WordCloud 
to be used in passive way, without having to create clouds.

Major Fixes
-----------
* Clouds are generated on a background thread so that the UI does not hang.
* Performance of cloud generation has been significantly improved.
* WordCloud 2.0 created a column in the default node table for each cloud. Clouds created
for separate subnetworks would overwrite each other's selections. WordCloud 3.0 now creates
columns in each subnetwork's local table. (Reading clouds from the default table is still
supported for backwards compatibility.)


Minor Fixes
-----------
