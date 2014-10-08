package org.baderlab.wordcloud.internal.ui.input;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListModel;

import org.baderlab.wordcloud.internal.model.WordDelimiters;
import org.baderlab.wordcloud.internal.model.WordFilter;

@SuppressWarnings("serial")
public class WordSelectPanel extends JPanel {
	

	public static interface Model {
		List<String> getCurrent();
		List<String> getAvailable();
		void add(String s);
		void remove(String s);
		boolean validate(Component parent, String s);
	}
	
	private final Model model;
	
	
	public WordSelectPanel(Model model) {
		this.model = model;
		createPanel();
	}

	
	public WordSelectPanel(final WordFilter filter) {
		this(new Model() {
			
			public void remove(String word) {
				filter.remove(word);
			}
			
			public void add(String word) {
				filter.add(word);
			}
			
			public List<String> getCurrent() {
				List<String> words = new ArrayList<String>();
				words.add("--Added Words--");
				words.addAll(sorted(filter.getAddedWords()));
				words.add("--Flagged Words--");
				words.addAll(sorted(filter.getFlaggedWords()));
				words.add("--Stop Words--");
				words.addAll(sorted(filter.getStopWords()));
				return words;
			}
			
			public List<String> getAvailable() {
				return null;
			}
			
			public boolean validate(Component parent, String word) {
				if(word.matches("[\\w]*")) {
					return true;
				} else {
					String message = "Word must contain only letters and numbers (no spaces).";
					JOptionPane.showMessageDialog(parent, message, "Cannot add word", JOptionPane.WARNING_MESSAGE);
					return false;
				}
			}
			
		});
	}
	
	
	public WordSelectPanel(final WordDelimiters delimeters) {
		this(new Model() {
			
			public void remove(String s) {
				delimeters.removeDelimiter(s);
			}
			
			public void add(String s) {
				delimeters.addDelimToUse(s);
			}
			
			public List<String> getCurrent() {
				List<String> delims = new ArrayList<String>();
				delims.add("--Common Delimiters--");
				delims.addAll(delimeters.getDelimsInUse());
				delims.add("--User Defined--");
				delims.addAll(delimeters.getUserDelims());
				return delims;
			}
			
			public List<String> getAvailable() {
				List<String> delims = new ArrayList<String>();
				delims.add("--Common Delimiters--");
				delims.addAll(delimeters.getDelimsToAdd());
				return delims;
			}
			
			public boolean validate(Component parent, String s) {
				return true;
			}
			
		});
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
		
		
		final JList wordList = new JList(createListModel());
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
		
		
		// create either a text field or a combo box for the add field
		JComponent text;
		final JComboBox addWordCombo;
		final JTextField addWordTextField;
		List<String> available = model.getAvailable();
		if(available == null) {
			text = addWordTextField = new JTextField();
			addWordCombo = null;
		}
		else {
			text = addWordCombo = new JComboBox(createComboModel());
			addWordCombo.setEditable(true);
			addWordTextField = null;
		}
		c = new GridBagConstraints();
		c.insets = insets;
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(text, c);
		
		JButton addButton = new JButton("Add");
		c = new GridBagConstraints();
		c.insets = insets;
		c.gridx = 1;
		c.gridy = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(addButton, c);
		
//		JCheckBox checkBox = new JCheckBox("Exclude Numbers 0-999");
//		c = new GridBagConstraints();
//		c.insets = insets;
//		c.gridx = 0;
//		c.gridy = 3;
//		c.gridwidth = 2;
//		c.anchor = GridBagConstraints.WEST;
//		add(checkBox, c);
		
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
				for(Object selected : wordList.getSelectedValues()) {
					String word = (String) selected;
					model.remove(word);
				}
				wordList.setModel(createListModel());
			}
		});
		
		
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String word;
				if(addWordTextField != null)
					word = addWordTextField.getText().trim();
				else
					word = (String) addWordCombo.getSelectedItem();
				
				if(word.length() == 0)
					return;
				if(model.validate(WordSelectPanel.this, word)) {
					model.add(word.toLowerCase());
					wordList.setModel(createListModel());
					if(addWordCombo != null)
						addWordCombo.setModel(createComboModel());
					else
						addWordTextField.setText(null);
				}
			}
		});
	}
	
	
	private ListModel createListModel() {
		DefaultListModel listModel = new DefaultListModel();
		for(String word : model.getCurrent()) {
			listModel.addElement(word);
		}
		return listModel;
	}
	
	private ComboBoxModel createComboModel() {
		DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
		comboModel.addElement("");
		for(String s : model.getAvailable()) {
			comboModel.addElement(s);
		}
		return comboModel;
	}
	
	private static <T extends Comparable<T>> List<T> sorted(Collection<T> collection) {
		List<T> result = new ArrayList<T>(collection);
		Collections.sort(result);
		return result;
	}
	
	
	public JDialog createDialog(Component parent, String title) {
		JOptionPane optionPane = new JOptionPane();
		optionPane.setMessage(this);
		optionPane.setIcon(null);
		optionPane.setPreferredSize(new Dimension(350, 400));
		optionPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JDialog dialog = optionPane.createDialog(parent, title);
		return dialog;
	}
	
	
	

}
