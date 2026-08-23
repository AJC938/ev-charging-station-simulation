package evcharging;

/**
 * Special demo vehicle with maximum priority.
 */
public class Highlander extends Vehicle {
    public static final int MAX_PRIORITY = 100;
    private static final double BATTERY_CAPACITY = 65.0;
    private static final double TARGET_CHARGE_LEVEL = 95.0;
    private static final int PATIENCE = 8;

    public Highlander(String vehicleID, double currentBatteryLevel, int arrivalTime) {
        super(vehicleID, BATTERY_CAPACITY, currentBatteryLevel, TARGET_CHARGE_LEVEL, arrivalTime, PATIENCE);
    }

    public Highlander(Highlander other) { super(other); }

    @Override
    public int getPriority() { return applyPriorityBoost(MAX_PRIORITY); }

    @Override
    public String getType() { return "Highlander"; }

    @Override
    protected Vehicle createCopy(String vehicleID) {
        return new Highlander(vehicleID, getCurrentBatteryLevel(), getArrivalTime());
    }

    @Override
    public Vehicle deepCopy() { return new Highlander(this); }
}
