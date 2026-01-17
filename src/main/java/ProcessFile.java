import java.io.*;
import java.nio.file.*;
import java.util.regex.*;
import java.nio.charset.Charset;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ProcessFile {
	Path loadedFile, tempFile, savingFile;
	SavingFileProfile saver;
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
		//BufferedReader reader;
		//BufferedWriter writer;
		try {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("S¹siedzi");
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
            
			String[] columnsNames = {"Zdefinowani", "Imiê i Nazwisko S¹siadów", "Adres", "Kod pocztowy i poczta", "Obrêb", "Nr dzia³ki", "Ark mapy", "KW", "KERG", "NR Roboty" };
            for (int i = 0; i < columnsNames.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnsNames[i]);
                cell.setCellStyle(styleHeader);
                sheet.autoSizeColumn(i);
            }
            
            Row secondRow = sheet.createRow(1);
            for (int i = 0; i < columnsNames.length; i++) {
            	Cell cell = secondRow.createCell(i);
                cell.setCellValue("HGW "+i);
                cell.setCellStyle(styleRest);
                sheet.autoSizeColumn(i);
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
				"Wyst¹pi³ b³ad: \r\n"+errorMessege,
		        "Wyst¹pi³ b³¹d",
		        JOptionPane.ERROR_MESSAGE);
	}
	
}
