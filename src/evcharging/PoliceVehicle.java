package evcharging;

/** Emergency police vehicle with high priority. */
public class PoliceVehicle extends Vehicle {
    private static final double BATTERY_CAPACITY = 80.0;
    private static final double TARGET_CHARGE_LEVEL = 90.0;
    private static final int PATIENCE = 12;
    private static final int PRIORITY = 4;

    public PoliceVehicle(String vehicleID, double currentBatteryLevel, int arrivalTime) {
        super(vehicleID, BATTERY_CAPACITY, currentBatteryLevel, TARGET_CHARGE_LEVEL, arrivalTime, PATIENCE);
    }

    public PoliceVehicle(PoliceVehicle other) { super(other); }

    @Override
    public int getPriority() { return applyPriorityBoost(PRIORITY); }

    @Override
    public String getType() { return "PoliceVehicle"; }

    @Override
    protected Vehicle createCopy(String vehicleID) {
        return new PoliceVehicle(vehicleID, getCurrentBatteryLevel(), getArrivalTime());
    }

    @Override
    public Vehicle deepCopy() { return new PoliceVehicle(this); }
}
