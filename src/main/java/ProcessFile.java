import java.awt.Desktop;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
				ownerName +="małż. ";
				boolean isFirst = true;
				for(int j=0; j<nameList.length; j++){
					String line = nameList[j];
					if(line.contains("Rodzice")){
						String nameMeriage = getNameIndyvidual(line);
						nameMeriage = nameMeriage.substring(0);
						ownerName += nameMeriage;
						if(isFirst){
							ownerName += " i \r\n        ";
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
				if(nameList.length>1)
					if(nameList[1] != null)
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
					nameInstitution = toTitleCase(nameInstitution);
					ownerName = nameInstitution;
					
					//set institutions address
					org.jsoup.select.Elements NameAndAdress = tName.select("td");
					String[] splittedColumn = NameAndAdress.toString().split("<br>\n");
					if(splittedColumn.length>1 && splittedColumn[1]!=null){
						if(owner.getAddressStreet()==null)
							setAddress(splittedColumn[1], owner);
					} /*else {
						if(splittedColumn.length>1 && splittedColumn[1]!=null){
							setAddress2(splittedColumn[1], owner);
						}
					}*/
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
		name =  toTitleCase(name);
		return name;
	}
	
	boolean setAddress(String fullAddress, Owner owner){
		String withoutTD = fullAddress.split("</td>")[0];
		String[] splitAddress= withoutTD.split(";");
		if(splitAddress[0]!=null){
			owner.setAddressStreet(toTitleCase(splitAddress[0]));
			if(splitAddress.length>1 && splitAddress[1]!=null)
				owner.setAddressPostCode(toTitleCase(splitAddress[1]));
			return true;
		} else return false;
	}
	
	boolean setAddress2(String fullAddress, Owner owner){
		String withoutTD = fullAddress.split("</td>")[0];
		String[] splitAddress= withoutTD.split(";");
		if(splitAddress[0]!=null){
			owner.setAddress2St(toTitleCase(splitAddress[0]));
			if(splitAddress.length>1 && splitAddress[1]!=null)
				owner.setAddress2Code(toTitleCase(splitAddress[1]));
			return true;
		} else return false;
	}
	
	String getObreb(org.jsoup.nodes.Element tbodyObreb, FieldData fieldData) {
		String obrebName="";
		org.jsoup.select.Elements rowsObreb= tbodyObreb.select("tr");
			for(org.jsoup.nodes.Element obrebRow : rowsObreb ) {
				if(obrebRow.text().contains("Nazwa obrębu")) {
					obrebName=obrebRow.text().split(":")[1];
					obrebName = toTitleCase(obrebName);
				}
				if(obrebRow.text().contains("Numer obrębu")) {
					String[] splittedNumber = obrebRow.text().split(":");
					String fieldObreb = fieldData.getFieldId().split("\\.")[1];
					if(fieldObreb.equals(splittedNumber[1]));{
						obrebName = toTitleCase(obrebName);
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
	        Font boldFont = workbook.createFont();
	        boldFont.setBold(true);
	        styleHeader.setFont(font);
	        styleHeader.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
	        styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	        
	        CellStyle styleRest = workbook.createCellStyle();
	        Font fontRest = workbook.createFont();
	        fontRest.setFontName("Arial");
	        fontRest.setFontHeightInPoints((short) 9);
	        styleRest.setFont(fontRest);
	        styleRest.setVerticalAlignment(VerticalAlignment.CENTER);
	        
	        CellStyle styleBoldAndCenter = workbook.createCellStyle();
	        DataFormat fmt = workbook.createDataFormat();
	        styleBoldAndCenter.setFont(boldFont);
	        styleBoldAndCenter.setAlignment(HorizontalAlignment.CENTER);
	        styleBoldAndCenter.setVerticalAlignment(VerticalAlignment.CENTER);
	        styleBoldAndCenter.setDataFormat(fmt.getFormat("@"));
	        
	        CellStyle styleWrapText = workbook.createCellStyle();
	        styleWrapText.setWrapText(true);
	        styleWrapText.setAlignment(HorizontalAlignment.CENTER);
	        styleWrapText.setVerticalAlignment(VerticalAlignment.CENTER);
	        
			String[] columnsNames = {"przedmiotowa", "Imie_Nazwisko", "Nr_dzialek_Obreb", "Adres", "Kod_pocztowy", "KW", "KERG" };
	        for (int i = 0; i < columnsNames.length; i++) {
	            Cell cell = headerRow.createCell(i);
	            cell.setCellValue(columnsNames[i]);
	            cell.setCellStyle(styleHeader);
	            sheet.autoSizeColumn(i);
	        }
	        int rowCount=1;
	        Map<Owner, List<String>> uniqueOwners = getUniqueOwnersList(selectedFieldsData);
	        uniqueOwners = sortUniqueOwnersMap(uniqueOwners);
	        Set<String> fieldNumbers = new TreeSet<>();
	        for (Map.Entry<Owner, List<String>> entry : uniqueOwners.entrySet()) {
	            Owner owner = entry.getKey();
	            List<String> stringList = entry.getValue();
	            Row nextRow = sheet.createRow(rowCount);
	            Cell cell1 = nextRow.createCell(1);
	            //String cleanName = owner.getName().trim().replaceAll("\\s+", " "); 
        		cell1.setCellValue(owner.getName());
        		
        		Cell cell2 = nextRow.createCell(2);
        		String obreb_nr="";
        		String KW="";
        		for(String fullField : stringList) {
        			String[] splittedField = fullField.split(";");
        			if(splittedField.length>2) {
        				obreb_nr += splittedField[1]+" obręb: "+splittedField[0]+"\n";
        				fieldNumbers.add(splittedField[1]);
        				KW += splittedField[2]+"\n";
        			}
        		}
        		cell2.setCellValue(obreb_nr);
        		cell2.setCellStyle(styleRest);
        		
        		Cell cell3 = nextRow.createCell(3);
        		if(owner.getAddressStreet() != null) {
	        		String cleanAddress = cleanWhiteSpaces(owner.getAddressStreet());
	        		if(owner.getAddress2St() != null) {
	        			String secondAdress = cleanWhiteSpaces(owner.getAddress2St());
	        			if(! cleanAddress.equals(secondAdress)) {
	        				cleanAddress += "\r\n" + secondAdress;
	        			}
	        		}
	        		cell3.setCellValue(cleanAddress);
        		}
        		
        		Cell cell4 = nextRow.createCell(4);
        		String allPostCode = cleanWhiteSpaces(owner.getAddressPostCode());
        			if(allPostCode != null && owner.getAddress2Code() != null){
        				String secondPostCode = cleanWhiteSpaces(owner.getAddress2Code());
        				if(! allPostCode.equals(secondPostCode)) {
        					allPostCode += "\r\n" + secondPostCode;
        				}
        			}
        		cell4.setCellValue(allPostCode);
        		
        		Cell cell5 = nextRow.createCell(5);
        		cell5.setCellValue(KW);
        		
        		Cell cell6 = nextRow.createCell(6);
        		cell6.setCellValue(KERG);
        		
        		rowCount++;
        		//nextRow.setHeight((short) -1);
        		
	        }
	        
	        String[] fieldsOption = fieldNumbers.toArray(new String[0]);
	        Cell cell0 = sheet.getRow(1).createCell(0);
    		cell0.setCellValue(fieldsOption[0]);
    		cell0.setCellStyle(styleBoldAndCenter);
	        DataValidationHelper helper = sheet.getDataValidationHelper();
	        CellRangeAddressList addressList = new CellRangeAddressList(1, 1, 0, 0);
	        DataValidationConstraint constraint = helper.createExplicitListConstraint(fieldsOption);
	        DataValidation validation = helper.createValidation(constraint, addressList);
	        sheet.addValidationData(validation);
	        
	        for(int j=1; j<rowCount; j++) {
	        	//sheet.getRow(j).setHeight((short)9);
	        	for(int k=1; k<7; k++) {
	        		Cell cell=sheet.getRow(j).getCell(k);
	        		if(cell != null) {
	        			cell.setCellStyle(styleRest);
	        			cell.setCellStyle(styleWrapText);
	        		}
	        	}
	        }
	        
	        /*
	        for(int j=0; j<selectedFieldsData.size(); j++) {
	        	Row firstRow = sheet.createRow(rowCount);
	        	Cell cell0 = firstRow.createCell(0);
	        	cell0.setCellValue(j+1);
	        	FieldData currentField = selectedFieldsData.get(j);
	        	int sizeOwners = currentField.getOwnersList().size();
	        	for(int k=0; k<sizeOwners; k++) {
	        		Owner currentOwner = currentField.getOwnersList().get(k);
	        		//checkWhiteSpaces(currentOwner.getName());
	        		Row nextRow=null;
	        		if(sheet.getRow(rowCount) == null) {
	        			nextRow = sheet.createRow(rowCount);
	        		} else nextRow=firstRow;
	        		rowCount++;
	        		Cell cell = nextRow.createCell(1);
	        		String cleanName = currentOwner.getName().trim().replaceAll("\\s+", " "); 
	        		cell.setCellValue(cleanName);
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
	        	
	        } */
	        
	        for(int j=0; j<7; j++) {
	        	sheet.autoSizeColumn(j);
	        }
	        sheet.setColumnWidth(1, 40 * 256);
	        sheet.setColumnWidth(2, 30 * 256);
	        sheet.setColumnWidth(5, 18 * 256);
			saver = new SavingFileProfile();
			saver.setNameLoadedFile(loadedFile.getFileName().toString());
			saver.setSavingFileProfile();
			savingFile = saver.getPath();
			File saveFile = savingFile.toFile();
			FileOutputStream fileOut = new FileOutputStream(saveFile);
	        workbook.write(fileOut);
	        fileOut.close();
	        workbook.close();
	        if(Desktop.isDesktopSupported()) {
	        	Desktop.getDesktop().open(saveFile);
	        }
	        
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
	
	Map<Owner, List<String>> getUniqueOwnersList(ArrayList<FieldData> selectedFieldsData){
		Map<Owner, List<String>> uniqueOwners = new HashMap<>();
		
		for(FieldData field:selectedFieldsData) {
			for(Owner owner:field.getOwnersList()) {
				String fieldString = field.getObreb()+";"+field.getFieldNumber()+";"+field.getKW();
				uniqueOwners.computeIfAbsent(owner, k -> new ArrayList<>())
                .add(fieldString);
				
			}
		}
		return uniqueOwners;
		
	}
	
	Map<Owner, List<String>> sortUniqueOwnersMap(Map<Owner, List<String>> uniqueOwners){
		Map<Owner, List<String>> sortedMap = uniqueOwners.entrySet()
	            .stream()
	            .sorted(Comparator.comparing(entry -> entry.getValue().get(0)))
	            .collect(Collectors.toMap(
	                Map.Entry::getKey, 
	                Map.Entry::getValue, 
	                (oldValue, newValue) -> oldValue, 
	                LinkedHashMap::new
	            ));
		
		return sortedMap;
	}
	
	String cleanWhiteSpaces(String input) {
		String output = null;
		if(input != null) {
			output = input.trim().replaceAll("\\s+", " ");
		}
		return output;
	}
	
	public static String toTitleCase(String text) {
	    if (text == null || text.isEmpty()) return text;

	    StringBuilder result = new StringBuilder();
	    // Dzielimy na słowa według spacji
	    for (String word : text.split("\\s+")) {
	        if (!word.isEmpty()) {
	            // Pierwsza litera -> Duża, reszta -> Mała
	            result.append(Character.toUpperCase(word.charAt(0)))
	                  .append(word.substring(1).toLowerCase())
	                  .append(" ");
	        }
	    }
	    return result.toString().trim();
	}
}
	
