import java.util.ArrayList;

public class FieldData {
	private static int count;
	int id;
	String FieldId;
	String FieldNumber;
	ArrayList<Owner> OwnersList = new ArrayList<Owner>();
	String KW;	
	

public FieldData(){
	count++;
	this.id = count;
}

public int getId(){
	return id;
}

public String getFieldId() {
	return FieldId;
}

public void setFieldId(String fieldId) {
	FieldId = fieldId;
}

public String getFieldNumber() {
	return FieldNumber;
}

public void setFieldNumber(String fieldNumber) {
	this.FieldNumber = fieldNumber;
}

public ArrayList<Owner> getOwnersList() {
	return OwnersList;
}

public void setOwnersList(ArrayList<Owner> ownersList) {
	this.OwnersList = ownersList;
}

public void addOwner(Owner owner) {
	this.OwnersList.add(owner);
}

public String getKW() {
	return KW;
}

public void setKW(String kW) {
	this.KW = kW;
}

@Override
public String toString() {
	String output = "";
	output = FieldNumber +" - " + FieldId + "\r\n";
	boolean isFirst=true;
	boolean shortSpace = false;
	for(Owner owner:OwnersList) {
		shortSpace = false;
		if(isFirst) {
			if(owner.getOwnershipType().equals("Własność"))
				output += "WŁ. ";
			isFirst = false;
			shortSpace = true;
		} else if(! owner.getOwnershipType().equals("Własność")) {
			output += "ZA. ";
			shortSpace = true;
		}
		
		if(shortSpace) {
			output += owner.toString();
		} else
			output += "   "+owner.toString();
		
		if(OwnersList.size()>1){
			if(! shortSpace || owner.getOwnershipType().equals("Własność"))
				output += ",";
		}
		
		output += "\r\n";
	}
	output += "KW "+ KW;
	
	return output;
}



}



