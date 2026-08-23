package evcharging;

/**
 * Delivery van with medium battery capacity and business-service priority.
 */
public class DeliveryVan extends Vehicle {
    private static final double BATTERY_CAPACITY = 90.0;
    private static final double TARGET_CHARGE_LEVEL = 85.0;
    private static final int PATIENCE = 30;
    private static final int PRIORITY = 2;

    public DeliveryVan(String vehicleID, double currentBatteryLevel, int arrivalTime) {
        super(vehicleID, BATTERY_CAPACITY, currentBatteryLevel, TARGET_CHARGE_LEVEL, arrivalTime, PATIENCE);
    }

    public DeliveryVan(DeliveryVan other) { super(other); }

    @Override
    public int getPriority() { return applyPriorityBoost(PRIORITY); }

    @Override
    public String getType() { return "Delivery Van"; }

    @Override
    protected Vehicle createCopy(String vehicleID) {
        return new DeliveryVan(vehicleID, getCurrentBatteryLevel(), getArrivalTime());
    }

    @Override
    public Vehicle deepCopy() { return new DeliveryVan(this); }
}
