public class Owner {
	String name;
	String ownershipType;
	String participation;
	String AddressStreet;
	String AddressPostCode;
	String Address2St;
	String Address2Code;
		
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOwnershipType() {
		return ownershipType;
	}
	public void setOwnershipType(String ownershipType) {
		this.ownershipType = ownershipType;
	}
	public String getParticipation() {
		return participation;
	}
	public void setParticipation(String participation) {
		this.participation = participation;
	}
	
	public String getAddressStreet() {
		return AddressStreet;
	}

	public void setAddressStreet(String addressStreet) {
		AddressStreet = addressStreet;
	}

	public String getAddressPostCode() {
		return AddressPostCode;
	}

	public void setAddressPostCode(String addressPostCode) {
		AddressPostCode = addressPostCode;
	}
	
	public String getAddress2Code() {
		return Address2Code;
	}
	public void setAddress2Code(String address2PostCode) {
		Address2Code = address2PostCode;
	}
	public String getAddress2St() {
		return Address2St;
	}
	public void setAddress2St(String address2St) {
		Address2St = address2St;
	}
	@Override
	public String toString() {
		String output = "";
		
		if(participation.equals("1/1")) {
			output += name;
		} else
			output += name + "- " + participation;
		
		output += "\n adres:"+AddressStreet+","+AddressPostCode;
		if(getAddress2St()!=null  && ! getAddress2St().equals(getAddressStreet()))
			output += "\n adres2:"+Address2St+","+Address2Code;
		
		return output;
	}
	
	
	
	
	
	

}
