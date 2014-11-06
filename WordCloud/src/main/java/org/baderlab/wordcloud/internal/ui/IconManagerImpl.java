package org.baderlab.wordcloud.internal.ui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

import org.baderlab.wordcloud.internal.CyActivator;

public class IconManagerImpl implements IconManager {

	private Font iconFont;

	public IconManagerImpl() {
		try {
			iconFont = Font.createFont(Font.TRUETYPE_FONT, CyActivator.class.getResourceAsStream("/fonts/fontawesome-webfont.ttf"));
		} catch (FontFormatException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Font getIconFont(float size) {
		return iconFont.deriveFont(size);
	}

}
