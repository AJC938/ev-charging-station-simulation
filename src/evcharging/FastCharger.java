package evcharging;

/**
 * Fast charger that supports all vehicles except electric buses.
 */
public class FastCharger extends Charger implements HighVoltageCapable {
    private static final double MAX_POWER_OUTPUT = 70.0;

    public FastCharger(String chargerID) {
        super(chargerID, MAX_POWER_OUTPUT);
    }

    /**
     * Fast chargers can charge every vehicle except ElectricBus.
     */
    @Override
    public boolean canCharge(Vehicle vehicle) {
        return !(vehicle instanceof ElectricBus);
    }

    @Override
    public String getChargerType() {
        return "Fast Charger";
    }

    /**
     * Taxi is not listed directly; it qualifies only when critical battery is true.
     */
    @Override
    public boolean canChargeFaster(Vehicle vehicle) {
        return canCharge(vehicle)
                && (vehicle instanceof DeliveryVan
                        || vehicle instanceof Ambulance
                        || vehicle instanceof PoliceVehicle
                        || vehicle instanceof Highlander
                        || vehicle.isCriticalBattery());
    }
}
