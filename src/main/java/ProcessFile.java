import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.*;
import java.nio.charset.Charset;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;


public class ProcessFile {
	Path loadedFile, tempFile, savingFile;
	SavingFileProfile saver;
	ArrayList<FieldData> listFieldsData = new ArrayList<FieldData>();
	//Path tempFilePath = Paths.get(System.getProperty("user.home")+"\\Desktop");
	
	String processLineOfFile(String originalLine) {
		
		// here implement own algorithm for processing one line of file
        String[] splitLine = originalLine.split("\\s+");
        String pointNumber = splitLine[1];
        
        Pattern pointsPrefixPattern = Pattern.compile("\\d{3}\\.\\d{3}-"); //erase prefix (example 223.112-)
        Matcher matcher = pointsPrefixPattern.matcher(pointNumber);
        String noPrefixNumber = matcher.replaceAll("").toString();
        
        Pattern minusOnePattern = Pattern.compile("-1$");
        matcher = minusOnePattern.matcher(noPrefixNumber);
        String withoutMinusOne = matcher.replaceAll("");
        
        
        Pattern slashPattern = Pattern.compile("-1\\/");	//replace -1/ by -
        matcher = slashPattern.matcher(withoutMinusOne);
        String finalNumber = matcher.replaceAll("-");
        
        String newLine = "";
        for(int splitIndex=1; splitIndex<splitLine.length; splitIndex++)
        	newLine = newLine+splitLine[splitIndex]+" ";
        
        newLine += finalNumber+"\r\n";
        
		return newLine;
	}
	
	public ProcessFile(Path file) {
		this.loadedFile = file;
	}
	
	boolean run() {
		FieldData fieldData = null;
		String KERG ="";
		//BufferedReader reader;
		//BufferedWriter writer;
		
		
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
				fieldData.setObreb(getObreb(tbodysObreb, fieldData));
				if(fieldData != null && fieldData.getKW()!= null)
					listFieldsData.add(fieldData);
			}
			
			
			
			
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Sąsiedzi");
			Row headerRow = sheet.createRow(0);
			CellStyle styleHeader = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 11);
            styleHeader.setFont(font);
            styleHeader.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
            styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            CellStyle styleRest = workbook.createCellStyle();
            styleRest.setFont(font);
            
			String[] columnsNames = {"Lp", "Imię i Nazwisko Sąsiadów", "Adres", "Kod pocztowy i poczta", "Obręb", "Nr działki", "Ark mapy", "KW", "KERG", "NR Roboty" };
            for (int i = 0; i < columnsNames.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnsNames[i]);
                cell.setCellStyle(styleHeader);
                sheet.autoSizeColumn(i);
            }
            int rowCount=1;
            for(int j=0; j<listFieldsData.size(); j++) {
            	Row firstRow = sheet.createRow(rowCount);
            	Cell cell0 = firstRow.createCell(0);
            	cell0.setCellValue(j+1);
            	FieldData currentField = listFieldsData.get(j);
            	int sizeOwners = currentField.getOwnersList().size();
            	for(int k=0; k<sizeOwners; k++) {
            		Owner currentOwner = currentField.getOwnersList().get(k);
            		Row nextRow=null;
            		if(sheet.getRow(rowCount) == null) {
            			nextRow = sheet.createRow(rowCount);
            		} else nextRow=firstRow;
            		rowCount++;
            		Cell cell = nextRow.createCell(1);
            		cell.setCellValue(currentOwner.getName());
            		Cell cell2 = nextRow.createCell(2);
            		cell2.setCellValue(currentOwner.getAddressStreet());
            		Cell cell3 = nextRow.createCell(3);
            		cell3.setCellValue(currentOwner.getAddressPostCode());
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
            for(int j=0; j<9; j++) {
            	sheet.autoSizeColumn(j);
            }
            
            
			
            /*
			reader = Files.newBufferedReader(loadedFile, Charset.defaultCharset());
			tempFile = Files.createTempFile("tempProcessFile", ".txt");
			writer = Files.newBufferedWriter(tempFile, Charset.defaultCharset());
			String currentLine=null;
			while((currentLine = reader.readLine()) != null) {
				String newLine = processLineOfFile(currentLine);
				writer.write(newLine);
				writer.flush();
				
			}
			*/
			saver = new SavingFileProfile();
			saver.setNameLoadedFile(loadedFile.getFileName().toString());
			saver.setSavingFileProfile();
			savingFile = saver.getPath();
			//Files.copy(tempFile.toAbsolutePath(), savingFile, REPLACE_EXISTING);
			//Files.delete(tempFile);
			
			
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
							ownerName += "i ";
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
	
	String getObreb(org.jsoup.select.Elements tbodysObreb, FieldData fieldData) {
		String obrebName="";
		for(int i=0; i<tbodysObreb.size(); i++ ) {
			for(org.jsoup.nodes.Element obrebRow : tbodysObreb.get(i).select("tr") ) {
				if(obrebRow.text().contains("Nazwa obrębu")) {
					System.out.println(obrebRow.text());
					obrebName=obrebRow.text().split(":")[1];
				}
				if(obrebRow.text().contains("Numer obrębu")) {
					String[] splittedNumber = obrebRow.text().split(":");
					String fieldObreb = fieldData.getFieldId().split("\\.")[1];
					if(fieldObreb==splittedNumber[1]);{
						System.out.println("I'm in"+splittedNumber[1]+" obreb:"+fieldObreb + obrebName);
						return obrebName;
					}
					
				}
			}
		}
		return "";
	}
	
}
