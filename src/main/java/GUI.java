import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class GUI extends JFrame{
	private static final long serialVersionUID = 1L;
	Font style1 = new Font("Arial", Font.ITALIC, 14);
	Font style2 = new Font("Arial", Font.ITALIC, 12);
    JFrame frame;
    Path choosedFile;
    ProcessFile fileProcessing = null;
    JList<ListItem> listFieldsNumbers = null;
    DefaultListModel<ListItem> listModel;
    JTextPane textPane = null;
    ArrayList<FieldData> listFieldsData = new ArrayList<FieldData>();
    ArrayList<FieldData> selectedFieldsData = new ArrayList<FieldData>();
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    JTextArea textBox_Clipboard = null;
    JScrollPane scrollClipboard = null;
    JLabel lblAktualnieWSchowku = null;
	
    
    /**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
		createMenuBar();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void createMenuBar() {
		JMenu menuFile = new JMenu("  Wczytaj plik  ");
		menuFile.setFont(style1);
		
		JMenu menuInfo = new JMenu("Info");
		menuInfo.setFont(style1);
		
		JMenuItem loadItem = new JMenuItem("html z danymi EGiB - GEOPORTAL2");
		loadItem.setFont(style2);
		loadItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		LoadingFileProfile loader = new LoadingFileProfile();
        		choosedFile = loader.getPath();
        		if(choosedFile!=null) {
        			fileProcessing = new ProcessFile(choosedFile);
        			if(fileProcessing.run()) {
        				listFieldsData = fileProcessing.listFieldsData;
        				updateList();
        			}
        		
        		}
        	}		
    	});
		
		JMenuItem infoItem = new JMenuItem("Instrukcja obsługi");
		final String info = "1. Wyświetl dane opisowe EGiB w geoportalu (tak żeby było widać właścicieli, numery KW, itp)\n"+
					  "2. Naciśnij prawy przycisk i wybierz Zapisz stronę lub coś podobnego (w zależności od przeglądarki)\n"+
					  "3. Zapisaną stronę wczytaj w programie\n"+
					  "4. Podwójne kliknięcie w numer działki skopiuje wybrane dane właścicieli do schowka\n"+
					  "5. Prawym przyciskiem można zaznaczyć lub odznaczyć działkę której adres wyeskportujesz do pliku xlsx \n"+
					  " (można wykorzystać to do zawiadomień w korespondencji seryjnej) \n\n\n"+
					  "Ewentualne uwagi proszę kierować na adres: bro.emilio.1.1@gmail.com";
		infoItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		JOptionPane.showMessageDialog(null,info,"Instrukcja obsługi",1);
        	}
		});
		menuFile.add(loadItem);
		menuInfo.add(infoItem);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuFile);
		menuBar.add(menuInfo);
		menuBar.setFont(style1);
		frame.setJMenuBar(menuBar);
	}
	
	private void initialize() {
		frame = new JFrame("geoEGiB v0.99");
		frame.setBounds(100, 100, 345, 393);
		frame.setMinimumSize(new Dimension(300,350));
		frame.setMaximumSize(new Dimension(600,700));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			SetPolishForGUI.setForFileChoosers();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, 0.5};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0};
		gridBagLayout.columnWidths = new int[]{245, 89};
		gridBagLayout.rowHeights = new int[]{80, 120, 22, 90};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JButton exportButton = new JButton();
		exportButton.setLayout(new GridLayout(2, 1));
		JLabel label1 = new JLabel("Eksport adresów do xlsx", SwingConstants.CENTER);
		JLabel label2 = new JLabel("(zaznaczonych prawym przyciskiem)", SwingConstants.CENTER);
		exportButton.add(label1);
		exportButton.add(label2);
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(fileProcessing!=null) {
					fileProcessing.exportToXLSX(getSelectedList());
					selectedFieldsData.clear();
				}
				
			}
		});
		JButton selectAllButton = new JButton("Zaznacz/Odznacz wszystkie");
		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(fileProcessing!=null) {
					DefaultListModel<ListItem> model = listModel;
					if(model.get(0).isSelected()) { //uncheck all
						for(int i=0;i<model.getSize();i++) {
							ListItem item = model.get(i);
							item.setSelected(false);
						}
					} else { // select all
						for(int i=0;i<model.getSize();i++) {
							ListItem item = model.get(i);
							item.setSelected(true);
						}
					}
					frame.repaint();
					
				}
				
			}
		});
		
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.NORTH;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 0;
		frame.getContentPane().add(exportButton, gbc_btnNewButton);
		
		GridBagConstraints gbc_selectButton = new GridBagConstraints();
		gbc_selectButton.anchor = GridBagConstraints.SOUTH;
		gbc_selectButton.insets = new Insets(0, 0, 5, 5);
		gbc_selectButton.gridx = 0;
		gbc_selectButton.gridy = 0;
		frame.getContentPane().add(selectAllButton, gbc_selectButton);
			
		JLabel lblInfoFieldNum = new JLabel(
				"<html><div style='text-align:center;'>Podwójne kliknięcie w nr działki spowoduje "
				+ "skopiowanie danych do schowka</div></html>");
		lblInfoFieldNum.setFont(new Font("Arial Narrow", Font.ITALIC, 10));
		GridBagConstraints gbc_lblInfoFieldNum = new GridBagConstraints();
		gbc_lblInfoFieldNum.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblInfoFieldNum.insets = new Insets(0, 0, 5, 0);
		gbc_lblInfoFieldNum.gridx = 1;
		gbc_lblInfoFieldNum.gridy = 0;
		gbc_lblInfoFieldNum.weightx=0;
		gbc_lblInfoFieldNum.weighty=0;
		lblInfoFieldNum.setMaximumSize(new Dimension(90,70));
		frame.getContentPane().add(lblInfoFieldNum, gbc_lblInfoFieldNum);
		
		textPane = new JTextPane();
		GridBagConstraints gbc_FieldData = new GridBagConstraints();
		gbc_FieldData.fill = GridBagConstraints.BOTH;
		gbc_FieldData.insets = new Insets(0, 0, 5, 5);
		gbc_FieldData.gridx = 0;
		gbc_FieldData.gridy = 1;
		gbc_FieldData.weightx=1;
		gbc_FieldData.weighty=1;
		frame.getContentPane().add(textPane, gbc_FieldData);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 1;
		frame.getContentPane().add(scrollPane, gbc_scrollPane);
		listFieldsNumbers = new JList<ListItem>();
		scrollPane.setViewportView(listFieldsNumbers);
		
		
		
		lblAktualnieWSchowku = new JLabel("Aktualnie w schowku");
		GridBagConstraints gbc_lblAktualnieWSchowku = new GridBagConstraints();
		gbc_lblAktualnieWSchowku.gridwidth = 2;
		gbc_lblAktualnieWSchowku.insets = new Insets(0, 0, 5, 0);
		gbc_lblAktualnieWSchowku.gridx = 0;
		gbc_lblAktualnieWSchowku.gridy = 2;
		gbc_lblAktualnieWSchowku.fill = GridBagConstraints.HORIZONTAL;
		frame.getContentPane().add(lblAktualnieWSchowku, gbc_lblAktualnieWSchowku);
		lblAktualnieWSchowku.setMinimumSize(new Dimension(90, 20));
		lblAktualnieWSchowku.setAlignmentX(CENTER_ALIGNMENT);
		
		scrollClipboard = new JScrollPane();
		textBox_Clipboard = new JTextArea();
		scrollClipboard.add(textBox_Clipboard);
		scrollClipboard.setViewportView(textBox_Clipboard);
		textBox_Clipboard.setEditable(false);
		GridBagConstraints gbc_Clipboard = new GridBagConstraints();
		gbc_Clipboard.gridwidth = 2;
		gbc_Clipboard.fill = GridBagConstraints.BOTH;
		gbc_Clipboard.gridx = 0;
		gbc_Clipboard.gridy = 3;
		frame.getContentPane().add(scrollClipboard, gbc_Clipboard);
		
	}
	
	private void updateList() {
		ListItem[] items = new ListItem[listFieldsData.size()];
		for(int i=0; i<items.length; i++) {
			items[i] = new ListItem(listFieldsData.get(i));
		}
		
		listModel = new DefaultListModel<ListItem>();
		for(ListItem oneField : items) {
			listModel.addElement(oneField);
		}
		listFieldsNumbers.setModel(listModel);
		listFieldsNumbers.setCellRenderer(new RightCheckBoxRenderer());
		listFieldsNumbers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listFieldsNumbers.setVisibleRowCount(-1);
		frame.repaint();
		
		listFieldsNumbers.addListSelectionListener (new ListSelectionListener()
	    {
	        public void valueChanged (ListSelectionEvent e)
	        {
	            if (e.getValueIsAdjusting ( ) == false)
	            {   
	                int currentIndex = listFieldsNumbers.getSelectedIndex();
	                FieldData currentFieldData = listFieldsData.get(currentIndex);
	                textPane.setText(currentFieldData.toString());
	                SimpleAttributeSet sas = new SimpleAttributeSet();
	                StyleConstants.setBold(sas, true);
	                StyleConstants.setFontSize(sas, 14);
	                int stringLength = currentFieldData.getFieldId().length() + currentFieldData.getFieldNumber().length() + 3;
	                textPane.getStyledDocument().setCharacterAttributes(0, stringLength, sas, false);
	                
	                Transferable content = clipboard.getContents(null);
	                if(content !=  null){
	                	textBox_Clipboard.setText(getStringFromTransferable(content));
	                	textBox_Clipboard.updateUI();
	                	textBox_Clipboard.setCaretPosition(0);
	                }
	            }
	        }

	    });
		
		listFieldsNumbers.addMouseListener(new MouseAdapter() {
		    @SuppressWarnings("unchecked")
			public void mouseClicked(MouseEvent evt) {
		    	listFieldsNumbers = (JList<ListItem>)evt.getSource();
		    	if (SwingUtilities.isRightMouseButton(evt)) {
		                int index = listFieldsNumbers.locationToIndex(evt.getPoint());
		                if (index != -1) {
		                    ListItem item = listFieldsNumbers.getModel().getElementAt(index);
		                    item.setSelected(!item.isSelected());
		                    listFieldsNumbers.repaint(listFieldsNumbers.getCellBounds(index, index));
		                }
		    	}
		    	
		        if (evt.getClickCount() == 2 || evt.getClickCount() == 3) {
		        	// Double and Triple click detected
		        	int currentIndex = listFieldsNumbers.getSelectedIndex();
		        	String displaingText = listFieldsData.get(currentIndex).toString();
		        	String clipboardText = "";
		        	String[] clipboardArray = displaingText.split("\\n");
		        	displaingText = "<html>W schowku: <B>" +clipboardArray[0]+"</B>\n</html>";
		        	lblAktualnieWSchowku.setText(displaingText);
		        	lblAktualnieWSchowku.updateUI();
		        	for(int i=1; i<clipboardArray.length; i++){
		        		clipboardText += clipboardArray[i]+"\n";
		        	}
		        	StringSelection selection = new StringSelection(clipboardText);
	                clipboard.setContents(selection, selection);
		            Transferable content = clipboard.getContents(null);
		            textBox_Clipboard.setText(getStringFromTransferable(content));
		            textBox_Clipboard.setCaretPosition(0);
		        } 
		    }
		});
		
	}
	
	private String getStringFromTransferable(Transferable contents){
		String result = null;
		boolean hasStringText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		if (hasStringText) {
		    try {
		        result = (String)contents.getTransferData(DataFlavor.stringFlavor);
		    } catch (UnsupportedFlavorException | IOException ex) {
		        System.out.println(ex); ex.printStackTrace();
		    }	
		}
		return result;
	}
	
	class ListItem {
	    private FieldData field;
	    private boolean selected;

	    public ListItem(FieldData field) { this.field = field; }
	    public boolean isSelected() { return selected; }
	    public void setSelected(boolean selected) { this.selected = selected; }
	    @Override
	    public String toString() { return field.getFieldNumber(); }
	}
	
	class RightCheckBoxRenderer extends JCheckBox implements ListCellRenderer<ListItem> {
		private static final long serialVersionUID = 1L;

		public RightCheckBoxRenderer() {
	        setHorizontalTextPosition(SwingConstants.LEADING);
	        setHorizontalAlignment(SwingConstants.LEFT); 
	    }

	    @Override
	    public Component getListCellRendererComponent(JList<? extends ListItem> list, 
	            ListItem value, int index, boolean isSelected, boolean cellHasFocus) {
	        setOpaque(true);
	        //setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
	        //setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
	        // Dane
	        setText(value.toString());
	        setSelected(value.isSelected());
	        
	        if (cellHasFocus) {
                setBackground(Color.LIGHT_GRAY);
                setForeground(list.getForeground());
            } else {
                setBackground(Color.WHITE);
                setForeground(list.getForeground());
            }
	        return this;
	    }
	}
	
	ArrayList<FieldData> getSelectedList(){
		DefaultListModel<ListItem> model = listModel;
		for(int i=0;i<model.getSize();i++) {
			ListItem item = model.get(i);
			if(item.isSelected()) {
				selectedFieldsData.add(item.field);
			}
		}
		System.out.println("SFD:"+selectedFieldsData.size());
		return selectedFieldsData;
	}
}
