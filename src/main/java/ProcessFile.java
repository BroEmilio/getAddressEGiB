import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;


public class ProcessFile {
	Path loadedFile, tempFile, savingFile;
	SavingFileProfile saver;
	ArrayList<FieldData> listFieldsData = new ArrayList<FieldData>();
	String KERG ="";
	String installFolder="";
	
	public ProcessFile(Path file) {
		this.loadedFile = file;
	}
	
	boolean run() {
		FieldData fieldData = null;
		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(this.loadedFile, null);
			org.jsoup.nodes.Element paragraphKERG = doc.select("p[align=center]").get(0);
			org.jsoup.select.Elements tbodysOwners = doc.select("tbody:contains(Lp)");
			org.jsoup.select.Elements tbodysNumbers = doc.select("tbody:contains(Nr działki)");
			org.jsoup.select.Elements tbodysObreb = doc.select("tbody:contains(Nazwa obrębu)");
			KERG = paragraphKERG.text().split(":")[1];
			for(int i=0; i<tbodysOwners.size(); i++){
				fieldData = new FieldData();
				org.jsoup.select.Elements tRows = tbodysOwners.get(i).select("tr");
				fieldData.setOwnersList(getNamesAndParticipations(tRows));
				
				//get FieldNumber, FieldId and KW
				org.jsoup.nodes.Element tbody = tbodysNumbers.get(i);
				org.jsoup.select.Elements tcolumn = tbody.select("tr").get(1).select("td");
				ArrayList<String> fieldNameList = new ArrayList<String>(Arrays.asList(tcolumn.get(0).text().split(" ")));
				fieldData.setFieldNumber(fieldNameList.get(0));
				fieldData.setFieldId(fieldNameList.get(4));
				fieldData.setKW(tcolumn.get(tcolumn.size()-1).text());
				if(fieldData != null && fieldData.getKW()!= null)
					listFieldsData.add(fieldData);
			}
			
			for(int i=0; i<listFieldsData.size(); i++) {
				if(listFieldsData.size()==tbodysObreb.size()) {
					fieldData = listFieldsData.get(i);
					org.jsoup.nodes.Element tbodyObreb = tbodysObreb.get(i);
					fieldData.setObreb(getObreb(tbodyObreb, fieldData));
				}
			}
			
			
			
		} catch (FileNotFoundException e) {
			displayErrorFrame(e.toString());
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			displayErrorFrame(e.toString());
			e.printStackTrace();
			return false;
		} 
		return true;
	}
	
	void displayErrorFrame(String errorMessege) {
		JOptionPane.showMessageDialog(null,
				"Wystąpił błąd: \r\n"+errorMessege,
		        "Wystąpił błąd",
		        JOptionPane.ERROR_MESSAGE);
	}
	
	ArrayList<Owner> getNamesAndParticipations(org.jsoup.select.Elements tRows){
		ArrayList<Owner> ownersAndSharesList = new ArrayList<Owner>();
		Owner owner = null;
		String ownerName = null;
		for(int i=1; i<tRows.size(); i++){
			org.jsoup.select.Elements tColumns = tRows.get(i).select("td");
			org.jsoup.nodes.Element tName = tColumns.get(1);
			
			owner = new Owner();
			ownerName = "";
			String ownershipType = tColumns.get(2).text();
			String participation = tColumns.get(3).text();
			String longName = tName.toString();
			longName = longName.substring(4);
			String[] nameList = longName.split("<br>");
			
			// get marriage names
			if(nameList[0].contains("małżeństwo")) {
				ownerName +="MAŁŻ.";
				boolean isFirst = true;
				for(int j=0; j<nameList.length; j++){
					String line = nameList[j];
					if(line.contains("Rodzice")){
						String nameMeriage = getNameIndyvidual(line);
						nameMeriage = nameMeriage.substring(1);
						ownerName += nameMeriage;
						if(isFirst){
							ownerName += "i \n   ";
							String AddressFull = nameList[2];
							setAddress(AddressFull, owner);
							isFirst=false;
						}
						else {
							String AddressFull = nameList[j+1];
							setAddress2(AddressFull, owner);
						}
					}
				}
				
			}
			
			// get individual person name
			if(nameList[0].contains("Rodzice")){
				ownerName += getNameIndyvidual(nameList[0]);
				setAddress(nameList[1], owner);
			} else
				// get institutions names
				if(! nameList[0].contains("małżeństwo")) {
					
					
					String nameInstitution = "";
					if(nameList[0].contains("</td>")) {
						nameInstitution = nameList[0].split("</td>")[0].toString();
					} else {
						nameInstitution = nameList[0].split("\n")[0].toString();
					}
					ownerName = nameInstitution;
					
					//set institutions address
					org.jsoup.select.Elements NameAndAdress = tName.select("td");
					String[] splittedColumn = NameAndAdress.toString().split("<br>\n");
					if(splittedColumn.length>2 && splittedColumn[1]!=null){
						if(owner.getAddressStreet()==null)
							setAddress(splittedColumn[1], owner);
					} else {
						if(splittedColumn.length>1 && splittedColumn[1]!=null){
							setAddress2(splittedColumn[1], owner);
						}
					}
				} 
				
			owner.setName(ownerName);
			owner.setOwnershipType(ownershipType);
			owner.setParticipation(participation);
			ownersAndSharesList.add(owner);
			System.out.println(owner);
		}
		
		return ownersAndSharesList;
	}
	
	String getNameIndyvidual (String input){
		String name = null;
		name = input.split("Rodzice")[0];
		return name;
	}
	
	boolean setAddress(String fullAddress, Owner owner){
		String withoutTD = fullAddress.split("</td>")[0];
		String[] splitAddress= withoutTD.split(";");
		if(splitAddress[0]!=null){
			owner.setAddressStreet(splitAddress[0]);
			if(splitAddress.length>1 && splitAddress[1]!=null)
				owner.setAddressPostCode(splitAddress[1]);
			return true;
		} else return false;
	}
	
	boolean setAddress2(String fullAddress, Owner owner){
		String withoutTD = fullAddress.split("</td>")[0];
		String[] splitAddress= withoutTD.split(";");
		if(splitAddress[0]!=null){
			owner.setAddress2St(splitAddress[0]);
			if(splitAddress.length>1 && splitAddress[1]!=null)
				owner.setAddress2Code(splitAddress[1]);
			return true;
		} else return false;
	}
	
	String getObreb(org.jsoup.nodes.Element tbodyObreb, FieldData fieldData) {
		String obrebName="";
		org.jsoup.select.Elements rowsObreb= tbodyObreb.select("tr");
			for(org.jsoup.nodes.Element obrebRow : rowsObreb ) {
				if(obrebRow.text().contains("Nazwa obrębu")) {
					obrebName=obrebRow.text().split(":")[1];
				}
				if(obrebRow.text().contains("Numer obrębu")) {
					String[] splittedNumber = obrebRow.text().split(":");
					String fieldObreb = fieldData.getFieldId().split("\\.")[1];
					if(fieldObreb.equals(splittedNumber[1]));{
						return obrebName;
					}
					
				}
			}
		return "";
	}
	
	boolean exportToXLSX(ArrayList<FieldData> selectedFieldsData) {
		try {
			String fullPath = GUI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(fullPath);
            installFolder = jarFile.getParent();
            System.out.println("installFolder: "+installFolder);
            //FileInputStream fis = new FileInputStream(installFolder+"\\Adresy-szablon.xlsm");
            //Workbook workbook = new XSSFWorkbook(fis);
            //Sheet sheet = workbook.getSheetAt(0);
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Właściciele");
			Row headerRow = sheet.createRow(0);
			CellStyle styleHeader = workbook.createCellStyle();
	        Font font = workbook.createFont();
	        font.setFontName("Arial");
	        font.setFontHeightInPoints((short) 11);
	        styleHeader.setFont(font);
	        styleHeader.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
	        styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	        
	        CellStyle styleRest = workbook.createCellStyle();
	        Font fontRest = workbook.createFont();
	        fontRest.setFontName("Arial");
	        fontRest.setFontHeightInPoints((short) 9);
	        styleRest.setFont(fontRest);
	        
			String[] columnsNames = {"Lp", "Imię i Nazwisko", "Adres", "Kod pocztowy i poczta", "Obręb", "Nr działki", "Ark mapy", "KW", "KERG", "NR Roboty", "przedmiotowa" };
	        for (int i = 0; i < columnsNames.length; i++) {
	            Cell cell = headerRow.createCell(i);
	            cell.setCellValue(columnsNames[i]);
	            cell.setCellStyle(styleHeader);
	            sheet.autoSizeColumn(i);
	        }
	        int rowCount=1;
	        for(int j=0; j<selectedFieldsData.size(); j++) {
	        	Row firstRow = sheet.createRow(rowCount);
	        	Cell cell0 = firstRow.createCell(0);
	        	cell0.setCellValue(j+1);
	        	FieldData currentField = selectedFieldsData.get(j);
	        	int sizeOwners = currentField.getOwnersList().size();
	        	for(int k=0; k<sizeOwners; k++) {
	        		Owner currentOwner = currentField.getOwnersList().get(k);
	        		checkWhiteSpaces(currentOwner.getName());
	        		Row nextRow=null;
	        		if(sheet.getRow(rowCount) == null) {
	        			nextRow = sheet.createRow(rowCount);
	        		} else nextRow=firstRow;
	        		rowCount++;
	        		Cell cell = nextRow.createCell(1);
	        		cell.setCellValue(currentOwner.getName());
	        		if(currentOwner.getAddress2St() == null) { //if second Address is empty
	            		Cell cell2 = nextRow.createCell(2);
	            		cell2.setCellValue(currentOwner.getAddressStreet());
	            		Cell cell3 = nextRow.createCell(3);
	            		cell3.setCellValue(currentOwner.getAddressPostCode());
	        		} else { //if has second Address
	        			if(currentOwner.getAddress2St().equals(currentOwner.getAddressStreet())) { // if Addresses are the same
	        				Cell cell2 = nextRow.createCell(2);
		            		cell2.setCellValue(currentOwner.getAddress2St());
		            		Cell cell3 = nextRow.createCell(3);
		            		cell3.setCellValue(currentOwner.getAddress2Code());
	        			} else { // if addreses are different
	        				if(currentOwner.getAddressStreet()==null && currentOwner.getAddress2St()!=null) {
	        					Cell cell2 = nextRow.createCell(2);
	    	            		cell2.setCellValue(currentOwner.getAddress2St());
	    	            		Cell cell3 = nextRow.createCell(3);
	    	            		cell3.setCellValue(currentOwner.getAddress2Code());
	        				} else {
	        					Cell cell2 = nextRow.createCell(2);
	    	            		cell2.setCellValue(currentOwner.getAddressStreet());
	    	            		Cell cell3 = nextRow.createCell(3);
	    	            		cell3.setCellValue(currentOwner.getAddressPostCode());
	        					Row addedRow = sheet.createRow(rowCount);
	        					rowCount++;
	        					Cell cell2a = addedRow.createCell(2);
	    	            		cell2a.setCellValue(currentOwner.getAddress2St());
	    	            		Cell cell3a = addedRow.createCell(3);
	    	            		cell3a.setCellValue(currentOwner.getAddress2Code());
	        				}
	        			}
	        			
	        		}
	        		Cell cell4 = nextRow.createCell(4);
	        		cell4.setCellValue(currentField.getObreb());
	        		Cell cell5 = nextRow.createCell(5);
	        		cell5.setCellValue(currentField.getFieldNumber());
	        		Cell cell7 = nextRow.createCell(7);
	        		cell7.setCellValue(currentField.getKW());
	        		Cell cell8 = nextRow.createCell(8);
	        		cell8.setCellValue(KERG);
	        	}
	        }
	        
	        CellRangeAddressList addressList = new CellRangeAddressList(1, rowCount-1, 10, 13);
	        XSSFSheet xssfSheet = null;
	        if (sheet instanceof XSSFSheet) {
	            xssfSheet = (XSSFSheet) sheet;
	        }
            DataValidationHelper validationHelper = new XSSFDataValidationHelper(xssfSheet);
            String[] options = {"[ ] NIE", "[X] TAK"};
            DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(options);
            DataValidation validation = validationHelper.createValidation(constraint, addressList);
            validation.setShowErrorBox(true);
            validation.setEmptyCellAllowed(false);
            xssfSheet.addValidationData(validation);
	        
	        for(int j=1; j<rowCount; j++) {
	        	//sheet.getRow(j).setHeight((short)9);
	        	for(int k=0; k<9; k++) {
	        		Cell cell=sheet.getRow(j).getCell(k);
	        		if(cell != null) {
	        			cell.setCellStyle(styleRest);
	        		}
	        	//for(k=10; k<=13; k++) {
	        		//Cell cellCheckbox = sheet.getRow(j).createCell(k);
	        		//cellCheckbox=sheet.getRow(j).getCell(k);
	        		//cellCheckbox.setCellValue(options[0]);
	        	//}
	        		Cell cellCheckbox = sheet.getRow(j).createCell(10);
	        		cellCheckbox=sheet.getRow(j).getCell(10);
	        		cellCheckbox.setCellValue(options[0]);
	        	}
	        	
	        } 
	        
	        for(int j=0; j<9; j++) {
	        	sheet.autoSizeColumn(j);
	        }
			saver = new SavingFileProfile();
			saver.setNameLoadedFile(loadedFile.getFileName().toString());
			saver.setSavingFileProfile();
			savingFile = saver.getPath();
			File saveFile = savingFile.toFile();
			FileOutputStream fileOut = new FileOutputStream(saveFile);
	        workbook.write(fileOut);
	        workbook.close();
		} catch (FileNotFoundException e) {
			displayErrorFrame(e.toString());
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			displayErrorFrame(e.toString());
			e.printStackTrace();
			return false;
		} catch (URISyntaxException e) {
			displayErrorFrame(e.toString());
			e.printStackTrace();
			return false;
		} 
	return true;
	}
	
	boolean checkWhiteSpaces(String checkingString) {
		boolean find= false;
		for (int i = 0; i < checkingString.length(); i++) {
            char c = checkingString.charAt(i);
            String opis="";
            if (Character.isWhitespace(c)) {
                // Mapowanie znaku na czytelną nazwę
                    switch (c) {
                    case ' ' : continue;
                    case '\n' : opis="\\n (nowa linia)";
                    case '\t' : opis= "\\t (tabulacja)";
                    case '\r' : opis= "\\r (powrót karetki)";
                    default : opis= "[inny znak biały]";
                };
                find = true;
                System.out.println("Pozycja " + i + ": " + opis);
            }
        }
		if(find) {
			System.out.println(checkingString);
			return true;
		} else return false;
	}
}
	
