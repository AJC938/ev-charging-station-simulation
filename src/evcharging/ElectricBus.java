package evcharging;

/**
 * Electric bus with the largest battery and public-transport priority.
 */
public class ElectricBus extends Vehicle {
    private static final double BATTERY_CAPACITY = 250.0;
    private static final double TARGET_CHARGE_LEVEL = 80.0;
    private static final int PATIENCE = 35;
    private static final int PRIORITY = 2;

    public ElectricBus(String vehicleID, double currentBatteryLevel, int arrivalTime) {
        super(vehicleID, BATTERY_CAPACITY, currentBatteryLevel, TARGET_CHARGE_LEVEL, arrivalTime, PATIENCE);
    }

    public ElectricBus(ElectricBus other) { super(other); }

    @Override
    public int getPriority() { return applyPriorityBoost(PRIORITY); }

    @Override
    public String getType() { return "Electric Bus"; }

    @Override
    protected Vehicle createCopy(String vehicleID) {
        return new ElectricBus(vehicleID, getCurrentBatteryLevel(), getArrivalTime());
    }

    @Override
    public Vehicle deepCopy() { return new ElectricBus(this); }
}
