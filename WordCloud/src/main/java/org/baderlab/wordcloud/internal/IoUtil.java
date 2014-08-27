package org.baderlab.wordcloud.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.cytoscape.io.util.StreamUtil;

public class IoUtil {
	private StreamUtil streamUtil;

	public IoUtil(StreamUtil streamUtil) {
		this.streamUtil = streamUtil;
	}
	
	public String readAll(String path) throws IOException {
		return readAll(streamUtil.getInputStream(path));
	}

	public String readAll(InputStream inputStream) throws IOException {
		InputStreamReader reader = new InputStreamReader(inputStream);
		StringBuilder builder = new StringBuilder();
		char[] buffer = new char[16 * 1024];
		int totalRead = 0;
		while (true) {
			totalRead = reader.read(buffer, 0, buffer.length);
			if (totalRead == -1) {
				break;
			}
			builder.append(buffer, 0, totalRead);
		}
		return builder.toString();
	}

	public String readAll(URL url) throws IOException {
		return readAll(streamUtil.getInputStream(url));
	}
}
