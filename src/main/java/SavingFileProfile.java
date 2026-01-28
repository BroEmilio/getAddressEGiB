import java.io.File;
import java.nio.file.Path;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SavingFileProfile {
	Path savingFile;
	JFileChooser saveChooser;
	String nameSavingFile;
	String extensionForFile = "xlsx";	//set extension for saving file
	
	void setSavingFileProfile() {
		configureSaveChooser(saveFilter);
	}
	
	//set filter for files 
	FileNameExtensionFilter saveFilter = new FileNameExtensionFilter
										("Plik z adresami ("+extensionForFile+")", extensionForFile);

	JFileChooser configureSaveChooser(FileNameExtensionFilter filter) {
		String userDir = System.getProperty("user.home");
		JFileChooser chooser= new JFileChooser(userDir +"/Desktop/geoEGiBforms");
		chooser.setFileFilter(filter);
    	chooser.setDialogTitle("Zapisz plik w folderze");
    	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	chooser.setAcceptAllFileFilterUsed(false);
    	//chooser.setSelectedFile(new File("Adresy"+nameSavingFile));
    	int userSelection = chooser.showSaveDialog(null);
    	if (userSelection == JFileChooser.APPROVE_OPTION) {
    		savingFile = chooser.getSelectedFile().toPath().resolve("Adresy"+nameSavingFile);
        	
    	}
    	
    	return chooser;
	}
	
	public Path getPath() {
		return savingFile;
	}
	
	public void setNameLoadedFile(String nameLoadedFile) {
		//String rawName = nameLoadedFile.substring(0, nameLoadedFile.lastIndexOf("."));
		this.nameSavingFile = "."+extensionForFile;
	}
	
}
