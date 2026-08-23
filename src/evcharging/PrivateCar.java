package evcharging;

/** Private passenger car with standard battery and low base priority. */
public class PrivateCar extends Vehicle {
    private static final double BATTERY_CAPACITY = 60.0;
    private static final double TARGET_CHARGE_LEVEL = 90.0;
    private static final int PATIENCE = 60;
    private static final int PRIORITY = 1;

    public PrivateCar(String vehicleID, double currentBatteryLevel, int arrivalTime) {
        super(vehicleID, BATTERY_CAPACITY, currentBatteryLevel, TARGET_CHARGE_LEVEL, arrivalTime, PATIENCE);
    }

    public PrivateCar(PrivateCar other) { super(other); }

    @Override
    public int getPriority() { return applyPriorityBoost(PRIORITY); }

    @Override
    public String getType() { return "Private Car"; }

    @Override
    protected Vehicle createCopy(String vehicleID) {
        return new PrivateCar(vehicleID, getCurrentBatteryLevel(), getArrivalTime());
    }

    @Override
    public Vehicle deepCopy() { return new PrivateCar(this); }
}
