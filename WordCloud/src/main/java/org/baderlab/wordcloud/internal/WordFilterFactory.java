package org.baderlab.wordcloud.internal;


public class WordFilterFactory {
	private IoUtil ioUtil;

	public WordFilterFactory(IoUtil ioUtil) {
		this.ioUtil = ioUtil;
	}
	
	public WordFilter createWordFilter() {
		return new WordFilter(ioUtil);
	}
}
