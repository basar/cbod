package net.bsrc.cbod.core.model;

public enum EObjectType {

    WHEEL("wheel"),
    HEAD_LIGHT("head_light"),
    TAIL_LIGHT("tail_light"),
    LICENSE_PLATE("license_plate"),
    CAR("car"),
    NONE_CAR_PART("none_car_part"),
    NONE_CAR("none_car");

    private String name;

    private EObjectType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
