package evcharging;

/**
 * Ambulance emergency vehicle with high priority and very low patience.
 */
public class Ambulance extends Vehicle {
    private static final double BATTERY_CAPACITY = 75.0;
    private static final double TARGET_CHARGE_LEVEL = 90.0;
    private static final int PATIENCE = 10;
    private static final int PRIORITY = 5;

    public Ambulance(String vehicleID, double currentBatteryLevel, int arrivalTime) {
        super(vehicleID, BATTERY_CAPACITY, currentBatteryLevel, TARGET_CHARGE_LEVEL, arrivalTime, PATIENCE);
    }

    public Ambulance(Ambulance other) { super(other); }

    @Override
    public int getPriority() { return applyPriorityBoost(PRIORITY); }

    @Override
    public String getType() { return "Ambulance"; }

    @Override
    protected Vehicle createCopy(String vehicleID) {
        return new Ambulance(vehicleID, getCurrentBatteryLevel(), getArrivalTime());
    }

    @Override
    public Vehicle deepCopy() { return new Ambulance(this); }
}
