package evcharging;

/**
 * Standard charger for smaller and medium vehicles.
 */
public class StandardCharger extends Charger {
    private static final double MAX_POWER_OUTPUT = 22.0;

    public StandardCharger(String chargerID) {
        super(chargerID, MAX_POWER_OUTPUT);
    }

    /**
     * Standard chargers support cars, taxis, vans, police vehicles, and Highlander.
     */
    @Override
    public boolean canCharge(Vehicle vehicle) {
        return vehicle instanceof PrivateCar
                || vehicle instanceof Taxi
                || vehicle instanceof DeliveryVan
                || vehicle instanceof PoliceVehicle
                || vehicle instanceof Highlander;
    }

    @Override
    public String getChargerType() {
        return "Standard Charger";
    }
}
