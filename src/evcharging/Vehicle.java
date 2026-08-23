package evcharging;

/**
 * Base type for every vehicle that can enter the charging station simulation.
 * It owns common battery, waiting, reservation, priority-boost, and copy state.
 */
public abstract class Vehicle implements Prioritizable {
    private final String vehicleID;
    private final double batteryCapacity;
    private double currentBatteryLevel;
    private final double targetChargeLevel;
    private final int arrivalTime;
    private final int patience;
    private int chargingStartTime;
    private int finishTime;
    private boolean criticalIncidentLogged;
    private int priorityBoost = 0;
    private boolean hasReservation = false;

    protected Vehicle(String vehicleID, double batteryCapacity, double currentBatteryLevel,
            double targetChargeLevel, int arrivalTime, int patience) {
        this.vehicleID = vehicleID;
        this.batteryCapacity = batteryCapacity;
        this.currentBatteryLevel = clamp(currentBatteryLevel, 0.0, 100.0);
        this.targetChargeLevel = clamp(targetChargeLevel, 0.0, 100.0);
        this.arrivalTime = arrivalTime;
        this.patience = patience;
        this.chargingStartTime = -1;
        this.finishTime = -1;
        this.criticalIncidentLogged = false;
    }

    protected Vehicle(Vehicle other) {
        this.vehicleID = other.vehicleID;
        this.batteryCapacity = other.batteryCapacity;
        this.currentBatteryLevel = other.currentBatteryLevel;
        this.targetChargeLevel = other.targetChargeLevel;
        this.arrivalTime = other.arrivalTime;
        this.patience = other.patience;
        this.chargingStartTime = other.chargingStartTime;
        this.finishTime = other.finishTime;
        this.criticalIncidentLogged = other.criticalIncidentLogged;
        this.priorityBoost = other.priorityBoost;
        this.hasReservation = other.hasReservation;
    }

    public abstract String getType();
    @Override public abstract int getPriority();
    public abstract Vehicle deepCopy();
    protected abstract Vehicle createCopy(String vehicleID);

    public String getVehicleID() { return vehicleID; }
    public double getBatteryCapacity() { return batteryCapacity; }
    public double getCurrentBatteryLevel() { return currentBatteryLevel; }
    public double getTargetChargeLevel() { return targetChargeLevel; }
    public int getArrivalTime() { return arrivalTime; }
    public int getPatience() { return patience; }
    public int getChargingStartTime() { return chargingStartTime; }
    public int getFinishTime() { return finishTime; }
    public boolean isCriticalIncidentLogged() { return criticalIncidentLogged; }
    public int getPriorityBoost() { return priorityBoost; }
    public boolean hasReservation() { return hasReservation; }

    public void reserve() { hasReservation = true; }
    public void cancelReservation() { hasReservation = false; }

    public void increasePriority() { priorityBoost++; }
    public void increasePriority(int amount) { if (amount > 0) priorityBoost += amount; }
    protected int applyPriorityBoost(int basePriority) { return basePriority + priorityBoost; }

    public Vehicle shallowCopy() {
        Vehicle copy = createCopy(vehicleID);
        copySimulationStateTo(copy);
        return copy;
    }

    public void markCriticalIncidentLogged() { criticalIncidentLogged = true; }
    public void markChargingStarted(int currentTime) { if (chargingStartTime < 0) chargingStartTime = currentTime; }
    public void markFinished(int currentTime) { finishTime = currentTime; }

    public int getWaitBeforeCharging() {
        if (chargingStartTime < 0) return 0;
        return Math.max(0, chargingStartTime - arrivalTime);
    }

    public int getWaitingTime(int currentTime) {
        if (chargingStartTime >= 0) return getWaitBeforeCharging();
        return Math.max(0, currentTime - arrivalTime);
    }

    public boolean hasExceededPatience(int currentTime) {
        return getWaitingTime(currentTime) > patience;
    }

    public boolean isCriticalBattery() { return currentBatteryLevel <= 15.0; }

    public boolean isCommercial() {
        return this instanceof Taxi || this instanceof DeliveryVan || this instanceof ElectricBus;
    }

    public boolean isEmergencyVehicle() {
        return this instanceof Ambulance || this instanceof PoliceVehicle || this instanceof Highlander;
    }

    public double getEnergyNeededKWh() {
        double percentageNeeded = Math.max(0.0, targetChargeLevel - currentBatteryLevel);
        return batteryCapacity * percentageNeeded / 100.0;
    }

    public boolean isFullyCharged() { return currentBatteryLevel >= targetChargeLevel - 0.0001; }

    public void addEnergy(double energyKWh) {
        if (energyKWh <= 0.0 || isFullyCharged()) return;
        double percentageGain = energyKWh / batteryCapacity * 100.0;
        currentBatteryLevel = Math.min(targetChargeLevel, currentBatteryLevel + percentageGain);
    }

    @Override
    public String toString() {
        return getType() + "{" + "id='" + vehicleID + '\''
                + ", battery=" + String.format("%.1f", currentBatteryLevel) + "%"
                + ", target=" + String.format("%.1f", targetChargeLevel) + "%"
                + ", priority=" + getPriority() + ", boost=" + priorityBoost
                + ", reserved=" + hasReservation + '}';
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void copySimulationStateTo(Vehicle copy) {
        copy.chargingStartTime = chargingStartTime;
        copy.finishTime = finishTime;
        copy.criticalIncidentLogged = criticalIncidentLogged;
        copy.priorityBoost = priorityBoost;
        copy.hasReservation = hasReservation;
    }
}
