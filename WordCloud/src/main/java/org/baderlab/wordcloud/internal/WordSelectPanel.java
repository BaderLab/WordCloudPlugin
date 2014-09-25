package org.baderlab.wordcloud.internal;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListModel;

@SuppressWarnings("serial")
public class WordSelectPanel extends JPanel {
	
	
	//String Constants for Separators in remove word combo box
	private static final String addedSeparator   = "--Added Words--";
	private static final String flaggedSeparator = "--Flagged Words--";
	private static final String stopSeparator    = "--Stop Words--";
	
	private final SemanticSummaryParameters networkParams;
	
	
	public WordSelectPanel(SemanticSummaryParameters networkParams) {
		this.networkParams = networkParams;
		createPanel();
	}

	
	private void createPanel() {
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		Insets insets = new Insets(3, 3, 3, 3);
		
		JLabel title = new JLabel("Excluded words");
		GridBagConstraints c = new GridBagConstraints();
		c.insets = insets;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		add(title, c);
		
		
		ListModel<String> wordListModel = createListModel();
		final JList<String> wordList = new JList<String>(wordListModel);
		JScrollPane wordScroll = new JScrollPane();
		wordScroll.setPreferredSize(wordList.getPreferredSize());
		wordScroll.setViewportView(wordList);
		wordScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		wordScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		c = new GridBagConstraints();
		c.insets = insets;
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		add(wordScroll, c);
		
		JButton removeButton = new JButton("Remove");
		c = new GridBagConstraints();
		c.insets = insets;
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(removeButton, c);
		
		final JTextField addWordTextField = new JTextField();
		c = new GridBagConstraints();
		c.insets = insets;
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(addWordTextField, c);
		
		JButton addButton = new JButton("Add");
		c = new GridBagConstraints();
		c.insets = insets;
		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(addButton, c);
		
		JCheckBox checkBox = new JCheckBox("Exclude Numbers 0-999");
		c = new GridBagConstraints();
		c.insets = insets;
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		add(checkBox, c);
		
		// MKTODO
//		JButton restoreDefaultsButton = new JButton("Restore Defaults");
//		c = new GridBagConstraints();
//		c.insets = insets;
//		c.gridx = 0;
//		c.gridy = 4;
//		c.gridwidth = 2;
//		c.anchor = GridBagConstraints.WEST;
//		add(restoreDefaultsButton, c);
		
		JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
		c = new GridBagConstraints();
		c.insets = new Insets(0,0,0,0);
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(separator, c);
		
		
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WordFilter filter = networkParams.getFilter();
				for(String word : wordList.getSelectedValuesList()) {
					filter.remove(word);
				}
				wordList.setModel(createListModel());
			}
		});
		
		
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				WordFilter filter = networkParams.getFilter();
				String word = addWordTextField.getText().trim();
				if(word.length() == 0)
					return;
				
				if(word.matches("[\\w]*")) {
					filter.add(word.toLowerCase());
					addWordTextField.setText(null);
					wordList.setModel(createListModel());
				}
				else {
					String message = "Word must contain only letters and numbers (no spacdf aes).";
					JOptionPane.showMessageDialog(WordSelectPanel.this, message, "Cannot add word", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
	}
	
	
	private ListModel<String> createListModel() {
		WordFilter wordFilter = networkParams.getFilter();
		
		DefaultListModel<String> wordListModel = new DefaultListModel<String>();
		wordListModel.addElement(addedSeparator);
		for(String word : sorted(wordFilter.getAddedWords())) {
			wordListModel.addElement(word);
		}
		wordListModel.addElement(flaggedSeparator);
		for(String word : sorted(wordFilter.getFlaggedWords())) {
			wordListModel.addElement(word);
		}
		wordListModel.addElement(stopSeparator);
		for(String word : sorted(wordFilter.getStopWords())) {
			wordListModel.addElement(word);
		}
		return wordListModel;
	}
	
	
	private static <T extends Comparable<T>> List<T> sorted(Collection<T> collection) {
		List<T> result = new ArrayList<T>(collection);
		Collections.sort(result);
		return result;
	}
	
	
	public JDialog createDialog(Component parent) {
		JOptionPane optionPane = new JOptionPane();
		optionPane.setMessage(this);
		optionPane.setIcon(null);
		optionPane.setPreferredSize(new Dimension(300, 400));
		optionPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JDialog dialog = optionPane.createDialog(parent, networkParams.getNetworkName());
		return dialog;
	}
	
	
	public static interface Model {
		List<String> getCurrent();
		List<String> getAvailable();
		void add(String s);
		void remove(String s);
	}

}
