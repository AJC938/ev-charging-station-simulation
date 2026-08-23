package evcharging;

/** Taxi vehicle with low base priority and short patience. */
public class Taxi extends Vehicle {
    private static final double BATTERY_CAPACITY = 55.0;
    private static final double TARGET_CHARGE_LEVEL = 85.0;
    private static final int PATIENCE = 25;
    private static final int PRIORITY = 1;

    public Taxi(String vehicleID, double currentBatteryLevel, int arrivalTime) {
        super(vehicleID, BATTERY_CAPACITY, currentBatteryLevel, TARGET_CHARGE_LEVEL, arrivalTime, PATIENCE);
    }

    public Taxi(Taxi other) { super(other); }

    @Override
    public int getPriority() { return applyPriorityBoost(PRIORITY); }

    @Override
    public String getType() { return "Taxi"; }

    @Override
    protected Vehicle createCopy(String vehicleID) {
        return new Taxi(vehicleID, getCurrentBatteryLevel(), getArrivalTime());
    }

    @Override
    public Vehicle deepCopy() { return new Taxi(this); }
}
