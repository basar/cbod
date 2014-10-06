package net.bsrc.cbod.core.model;

public enum EObjectType {

	WHEEL("wheel"), CAR_WINDOW("car_window"), HEAD_LIGHT("head_light"), TAIL_LIGHT(
			"tail_light"), CAR("car"), NONE_CAR_PART("none_car_part"), NONE_CAR(
			"none_car");

	private String name;

	private EObjectType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
