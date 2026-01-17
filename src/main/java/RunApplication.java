import java.awt.*;
import java.awt.event.*;
import java.nio.file.Path;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

@SuppressWarnings("serial")
public class RunApplication extends JFrame {
	
	private JTextField fileName;
	private JTextField frameLabel;
	private JButton chooseButton, processButton;
	Path choosedFile;
	
	public RunApplication() {
		initializeGUI();
		createGUIComponents();
	}
	
	void initializeGUI(){
		setTitle("getAddressEGiB v.0.1");
        setMinimumSize(new Dimension(535, 145));
        setSize(new Dimension(535, 145));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
			SetPolishForGUI.setForFileChoosers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void createGUIComponents() {
		setType(Type.UTILITY);
		
		fileName = new JTextField();
		fileName.setEnabled(false);
		fileName.setHorizontalAlignment(SwingConstants.CENTER);
		fileName.setFont(new Font("Arial", Font.ITALIC, 12));
		fileName.setColumns(10);
		
		chooseButton = new JButton("Wybierz");
		chooseButton.setFont(new Font("Arial", Font.ITALIC, 12));
		chooseButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		LoadingFileProfile loader = new LoadingFileProfile();
        		choosedFile = loader.getPath();
        		if(choosedFile!=null)
        			fileName.setText(choosedFile.toAbsolutePath().toString());
	    		}
    	});
		
		processButton = new JButton("Przetwarzaj");
		processButton.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
		processButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if(choosedFile!=null) {
        			ProcessFile fileProcessing = new ProcessFile(choosedFile);
        			if(fileProcessing.run()) {
	        			int result=JOptionPane.showConfirmDialog(null,
	        						"Poprawnie przetworzono plik "+choosedFile.getFileName().toString(),
	        						"Poprawnie przetworzono",
	        						JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        				if(result==JOptionPane.OK_OPTION || result == JOptionPane.CANCEL_OPTION)
        					System.exit(0);
        			}
        		}
        	}
    	});
		
		frameLabel = new JTextField();
		frameLabel.setEnabled(false);
		frameLabel.setEditable(false);
		frameLabel.setFont(new Font("Arial Black", Font.BOLD | Font.ITALIC, 14));
		frameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		frameLabel.setText("Wybierz plik");
		frameLabel.setColumns(10);
		frameLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder());
		frameLabel.setBackground(new Color(0, 0, 0, 0));
		frameLabel.setOpaque(false);
		
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(fileName, GroupLayout.PREFERRED_SIZE, 381, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(chooseButton, GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
					.addGap(22))
				.addComponent(frameLabel, GroupLayout.DEFAULT_SIZE, 519, Short.MAX_VALUE)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(178)
					.addComponent(processButton, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(185, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(frameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(fileName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(chooseButton, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(processButton, GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
					.addContainerGap())
		);
		getContentPane().setLayout(groupLayout);
	}
	
	void showErrorMessage(){
		
	}
	
	public static void main(String[] args) {
		RunApplication frame=new RunApplication();
		frame.setVisible(true);
		
	}
}
