package net.bsrc.cbod.pascal;

public enum EPascalType {
	
	CAR("car"),
	PERSON("person");
	
	private String name;
	
	private EPascalType(String name){
		this.name=name;
	}

	public String getName() {
		return name;
	}
	
}
