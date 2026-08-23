package evcharging;

/** Base class for all station chargers. */
public abstract class Charger {
    private final String chargerID;
    private final double maxPowerOutput;
    private double currentPowerOutput;
    private boolean occupied;
    private Vehicle connectedVehicle;
    private int timeRemaining;

    protected Charger(String chargerID, double maxPowerOutput) {
        this.chargerID = chargerID;
        this.maxPowerOutput = maxPowerOutput;
        this.currentPowerOutput = 0.0;
        this.occupied = false;
        this.connectedVehicle = null;
        this.timeRemaining = 0;
    }

    public abstract boolean canCharge(Vehicle vehicle);
    public abstract String getChargerType();

    public String getChargerID() { return chargerID; }
    public double getMaxPowerOutput() { return maxPowerOutput; }
    public double getCurrentPowerOutput() { return currentPowerOutput; }
    public boolean isOccupied() { return occupied; }
    public boolean isAvailable() { return !occupied; }
    public Vehicle getConnectedVehicle() { return connectedVehicle; }
    public int getTimeRemaining() { return timeRemaining; }

    public void setCurrentPowerOutput(double currentPowerOutput) {
        this.currentPowerOutput = Math.max(0.0, Math.min(maxPowerOutput, currentPowerOutput));
        updateTimeRemaining();
    }

    public void startCharging(Vehicle vehicle, int currentTime) {
        if (vehicle == null || !canCharge(vehicle)) {
            throw new IllegalArgumentException("Vehicle cannot be charged by " + getChargerType());
        }
        connectedVehicle = vehicle;
        occupied = true;
        vehicle.markChargingStarted(currentTime);
        updateTimeRemaining();
    }

    public void chargeOneMinute(int currentTime) {
        if (!occupied || connectedVehicle == null || currentPowerOutput <= 0.0) {
            updateTimeRemaining();
            return;
        }
        connectedVehicle.addEnergy(currentPowerOutput / 60.0);
        updateTimeRemaining();
    }

    public Vehicle releaseVehicle(int currentTime) {
        if (!occupied || connectedVehicle == null) return null;
        Vehicle releasedVehicle = connectedVehicle;
        releasedVehicle.markFinished(currentTime);
        connectedVehicle = null;
        occupied = false;
        currentPowerOutput = 0.0;
        timeRemaining = 0;
        return releasedVehicle;
    }

    @Override
    public String toString() {
        String status = occupied ? "charging " + connectedVehicle.getVehicleID() : "available";
        return String.format("%s[%s, max=%.1f kW, current=%.1f kW, %s]",
                getChargerType(), chargerID, maxPowerOutput, currentPowerOutput, status);
    }

    private void updateTimeRemaining() {
        if (!occupied || connectedVehicle == null || connectedVehicle.isFullyCharged()) {
            timeRemaining = 0;
            return;
        }
        if (currentPowerOutput <= 0.0) {
            timeRemaining = Integer.MAX_VALUE;
            return;
        }
        timeRemaining = (int) Math.ceil(connectedVehicle.getEnergyNeededKWh() / currentPowerOutput * 60.0);
    }
}
