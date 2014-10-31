/*
 File: CloudDisplayStyles.java

 Copyright 2010 - The Cytoscape Consortium (www.cytoscape.org)
 
 Code written by: Layla Oesper
 Authors: Layla Oesper, Ruth Isserlin, Daniele Merico
 
 This library is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this project.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.baderlab.wordcloud.internal.cluster;

/**
 * The CloudDisplayStyles class contains information on the display styles
 * available for the SemanticSummary Cloud.
 */
public enum CloudDisplayStyles 
{
	CLUSTERED_STANDARD("Clustered-Standard"),
	CLUSTERED_BOXES("Clustered-Boxes"),
	NO_CLUSTERING("No Clustering");
	
	private final String name;
	
	private CloudDisplayStyles(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	
	public static CloudDisplayStyles getDefault() {
		return CLUSTERED_STANDARD;
	}
	
	public static CloudDisplayStyles fromString(String s) {
		for(CloudDisplayStyles style : values()) {
			if(style.name.equals(s)) {
				return style;
			}
		}
		return null;
	}
}
