package evcharging;

/**
 * Super charger that can charge every vehicle type.
 */
public class SuperCharger extends Charger implements HighVoltageCapable {
    private static final double MAX_POWER_OUTPUT = 150.0;

    public SuperCharger(String chargerID) {
        super(chargerID, MAX_POWER_OUTPUT);
    }

    @Override
    public boolean canCharge(Vehicle vehicle) {
        return true;
    }

    @Override
    public String getChargerType() {
        return "Super Charger";
    }

    @Override
    public boolean canChargeFaster(Vehicle vehicle) {
        return true;
    }
}
