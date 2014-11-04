package org.baderlab.wordcloud;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestCloudModel.class, 
	TestCloudWords.class,
	TestCommands.class,
	TestColumns.class
})
public class TestAll {
}
