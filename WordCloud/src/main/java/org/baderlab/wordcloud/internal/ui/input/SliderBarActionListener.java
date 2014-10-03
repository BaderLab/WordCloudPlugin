/*
 File: SliderBarActionListener.java

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

package org.baderlab.wordcloud.internal.ui.input;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class SliderBarActionListener implements ChangeListener {

    private SliderBarPanel panel;

//	private SemanticSummaryManager cloudManager;
//	private UpdateCloudAction updateCloudAction;

    /**
     * Class constructor
     *
     * @param panel
     * @param params - cloud parameters for current cloud
     * @param attrib - attribute that the slider bar is specific to (i.e. network normalization)
     */
    public SliderBarActionListener(SliderBarPanel panel/*, SemanticSummaryManager cloudManager, UpdateCloudAction updateCloudAction */) {
//        this.panel = panel;
//        this.cloudManager = cloudManager;
//        this.updateCloudAction = updateCloudAction;
    }

    /**
     * Go through the current cloud and update the display as the parameter changes.  Also, resets
     * the list of selected nodes back to original.
     *
     * @param e
     */
    public void stateChanged(ChangeEvent e){
//        JSlider source = (JSlider)e.getSource();
//        Double value = source.getValue()/panel.getPrecision();
//
//        panel.setLabel(source.getValue());
//        
//        //Change Cloud Parameters with new value and update cloud
//        CloudParameters curCloud = cloudManager.getCurCloud();
//        
//        if (curCloud != cloudManager.getNullCloudParameters() && curCloud != null) {
//        	curCloud.setNetWeightFactor(value);
//        }
//       
//       //Update cloud
//       updateCloudAction.doRealAction();
   }
}

