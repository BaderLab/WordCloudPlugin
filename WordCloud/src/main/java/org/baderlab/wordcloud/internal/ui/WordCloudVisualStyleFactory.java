/*
 File: WordCloudVisualStyle.java

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

package org.baderlab.wordcloud.internal.ui;

import java.awt.Color;

import org.baderlab.wordcloud.internal.command.CreateCloudNetworkAction;
import org.baderlab.wordcloud.internal.model.next.CloudParameters;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

/**
 * This contains all of the visual style information for the associated network.
 * @author Layla Oesper
 * @version 1.0
 */

public class WordCloudVisualStyleFactory 
{
	VisualStyleFactory styleFactory;
	VisualMappingFunctionFactory continuousMappingFactory;
	VisualMappingFunctionFactory passthroughMappingFactory;
	
	//CONSTRUCTORS
	
	/**
	 * Basic Constructor
	 * @param string - name of the visual style to create.
	 * @param  name - name of the network this visual style pertains to
	 */
	public WordCloudVisualStyleFactory(VisualStyleFactory styleFactory, VisualMappingFunctionFactory continuousMappingFactory, VisualMappingFunctionFactory passthroughMappingFactory)
	{
		this.styleFactory = styleFactory;
		this.continuousMappingFactory = continuousMappingFactory;
		this.passthroughMappingFactory = passthroughMappingFactory;
	}
	
	/**
	 * Creates visual style for this WordCloud network.
	 * @param cloudParams 
	 * @param network - network to apply this visual style
	 * @param name - name of the network, to be appended to attribute names
	 */
	public VisualStyle createVisualStyle(String visualStyleName, CloudParameters cloudParams)
	{
		VisualStyle style = styleFactory.createVisualStyle(visualStyleName);
		
		setEdgeStyles(style);
		setNodeStyles(style, cloudParams);
		return style;
	}
	
	/**
	 * Create edge appearances for this WordCloud network.  Specify edge thickness mapped
	 * to the inputted word co occurence probability.
	 * 
	 * @param network - network to apply this visual style
     * @param name - name to be appended to each of the attribute names
	 */
	private void setEdgeStyles(VisualStyle style)
	{
		style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, new Color(100, 200, 0));
		
		//Continuous Mapping - set edge line thickness based on the probability ratio
		ContinuousMapping<Double, Double> edgeWidth = (ContinuousMapping<Double, Double>) continuousMappingFactory.createVisualMappingFunction(CreateCloudNetworkAction.CO_VAL, Double.class, BasicVisualLexicon.EDGE_WIDTH);
		
		Double under_width = 0.5;
		Double min_width = 1.0;
		Double max_width = 8.0;
		Double over_width = 9.0;
		
		edgeWidth.addPoint(1.0,  new BoundaryRangeValues<Double>(under_width, min_width, min_width));
		edgeWidth.addPoint(40.0,  new BoundaryRangeValues<Double>(max_width, max_width, over_width));
		
		style.addVisualMappingFunction(edgeWidth);
	}
	
	/**
	 * Create node appearances for this WordCloud network.  Specify node size and label size
	 * based on the word probability values.
	 * 
	 * @param network - network to apply this visual style
     * @param name - name to be appended to each of the attribute names
	 */
	private void setNodeStyles(VisualStyle style, CloudParameters cloudParams)
	{
		//set the default appearance
		style.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.gray);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, Color.gray);
		style.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		style.setDefaultValue(BasicVisualLexicon.NODE_SIZE, 35.0);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 4.0);
		
        //Continuous Mapping - set node size based on the probability value
		ContinuousMapping<Double, Double> nodeSize = (ContinuousMapping<Double, Double>) continuousMappingFactory.createVisualMappingFunction(CreateCloudNetworkAction.WORD_VAL, Double.class, BasicVisualLexicon.NODE_SIZE);
		{
	        double min = 20.0;
	        double max = 65.0;
	        
	        nodeSize.addPoint(cloudParams.getMinRatio(), new BoundaryRangeValues<Double>(min, min, min));
	        nodeSize.addPoint(cloudParams.getMaxRatio(), new BoundaryRangeValues<Double>(max, max, max));
		}
		
        //Label size
		ContinuousMapping<Double, Integer> labelSize = (ContinuousMapping<Double, Integer>) continuousMappingFactory.createVisualMappingFunction(CreateCloudNetworkAction.WORD_VAL, Double.class, BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
		{
	        int min = 12;
	        int max = 56;
	        
	        labelSize.addPoint(cloudParams.getMinRatio(), new BoundaryRangeValues<Integer>(min, min, min));
	        labelSize.addPoint(cloudParams.getMaxRatio(), new BoundaryRangeValues<Integer>(max, max, max));
		}
		
		VisualMappingFunction<String, String> nodeLabel = passthroughMappingFactory.createVisualMappingFunction(CyNetwork.NAME, String.class, BasicVisualLexicon.NODE_LABEL);
		
		style.addVisualMappingFunction(nodeSize);
		style.addVisualMappingFunction(labelSize);
		style.addVisualMappingFunction(nodeLabel);
	}
}
