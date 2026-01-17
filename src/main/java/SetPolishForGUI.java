import java.awt.event.KeyEvent;

import javax.swing.UIManager;

public class SetPolishForGUI {
	public static void setForFileChoosers() {
		UIManager.put("FileChooser.lookInLabelText","Szukaj w");
        UIManager.put("FileChooser.lookInLabelMnemonic",""+KeyEvent.VK_W);
 
        UIManager.put("FileChooser.saveInLabelText","Zapisz w");
        UIManager.put("FileChooser.saveInLabelMnemonic",""+KeyEvent.VK_W);
 
        UIManager.put("FileChooser.fileNameLabelText","Nazwa pliku:");
        UIManager.put("FileChooser.fileNameLabelMnemonic",""+KeyEvent.VK_N);
 
        UIManager.put("FileChooser.folderNameLabelText","Nazwa katalogu:");
        UIManager.put("FileChooser.folderNameLabelMnemonic",""+KeyEvent.VK_N);      
 
        UIManager.put("FileChooser.filesOfTypeLabelText","Pliki typu:");
        UIManager.put("FileChooser.filesOfTypeLabelMnemonic",""+KeyEvent.VK_P);
 
        UIManager.put("FileChooser.upFolderToolTipText","PrzejdŸ wy¿ej");
        UIManager.put("FileChooser.homeFolderToolTipText","Pulpit");
        UIManager.put("FileChooser.newFolderToolTipText","Nowy katalog");
        UIManager.put("FileChooser.listViewButtonToolTipText","Lista");
        UIManager.put("FileChooser.detailsViewButtonToolTipText","Szczegó³y");
 
        UIManager.put("FileChooser.fileNameHeaderText","Nazwa");
        UIManager.put("FileChooser.fileSizeHeaderText","Rozmiar");
        UIManager.put("FileChooser.fileTypeHeaderText","Typ");
        UIManager.put("FileChooser.fileDateHeaderText","Modyfikacja");
        UIManager.put("FileChooser.fileAttrHeaderText","Atrybuty");
 
        UIManager.put("FileChooser.newFolderErrorText","B³¹d podczas tworzenia katalogu");
 
        UIManager.put("FileChooser.saveButtonText","Zapisz");
        UIManager.put("FileChooser.saveButtonMnemonic",""+KeyEvent.VK_Z);
 
        UIManager.put("FileChooser.openButtonText","Otwórz");
        UIManager.put("FileChooser.openButtonMnemonic",""+KeyEvent.VK_O);
 
        UIManager.put("FileChooser.cancelButtonText","Anuluj");
        UIManager.put("FileChooser.openButtonMnemonic",""+KeyEvent.VK_R);
 
        UIManager.put("FileChooser.openDialogTitleText","Otwieranie");
        UIManager.put("FileChooser.saveDialogTitleText","Zapisywanie");
 
        UIManager.put("FileChooser.saveButtonToolTipText","Zapisanie pliku");
        UIManager.put("FileChooser.openButtonToolTipText","Otwarcie pliku");
        UIManager.put("FileChooser.cancelButtonToolTipText","Anuluj");
        UIManager.put("FileChooser.acceptAllFileFilterText","Wszystkie pliki");
	}
}
