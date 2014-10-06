package net.bsrc.cbod.pascal;

public enum EPascalType {

	CAR("car"), PERSON("person"), BICYCLE("bicycle"), BIRD("bird"), BOAT("boat"), BOTTLE(
			"bottle"), BUS("bus"), CAT("cat"), CHAIR("chair"), COW("cow"), DINING_TABLE(
			"diningtable"), DOG("dog"), HORSE("horse"), MOTOR_BIKE("motorbike"), POTTED_PLANT(
			"pottedplant"), SHEEP("sheep"), SOFA("sofa"), TRAIN("train"), TV_MONITOR(
			"tvmonitor"), AEROPLANE("aeroplane");

	private String name;

	private EPascalType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
