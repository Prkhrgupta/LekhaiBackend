package in.lekhai.gsp.ewb.domain.enums;

import in.lekhai.contract.model.VehicleType;

public enum EwbVehicleType {
    R("Regular", VehicleType.REGULAR),
    O("ODC(Over Dimentional Cargo)", VehicleType.ODC)
    ;

    private final String type;
    private final VehicleType vehicleType;

    EwbVehicleType(String type,
                   VehicleType vehicleType) {
        this.type = type;
        this.vehicleType = vehicleType;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }
}
