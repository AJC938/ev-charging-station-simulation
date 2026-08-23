package evcharging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/** Coordinates chargers, queues, reservations, scheduling, power allocation, and reporting. */
public class ChargingStation {
    private static final int WAIT_THRESHOLD = 20;
    private static final int MAX_WAIT_BOOST = 3;
    private static final double RESERVATION_PROBABILITY = 0.40;

    private final List<Charger> chargers = new ArrayList<>();
    private final Queue<Vehicle> waitingQueue = new LinkedList<>();
    private final List<Vehicle> servedVehicles = new ArrayList<>();
    private final ReservationSystem reservationSystem = new ReservationSystem();
    private final Random reservationRandom = new Random(364);
    private final double gridPowerBudget;
    private ChargingStationMode currentMode;
    private double totalActivePower;
    private double cumulativePowerKwMinutes;
    private int minutesSimulated;
    private int lostCustomers;
    private int criticalIncidents;
    private int totalArrived;
    private int currentTime;
    private int priorityBoostsApplied;

    public ChargingStation(double gridPowerBudget, ChargingStationMode mode) {
        this.gridPowerBudget = gridPowerBudget;
        this.currentMode = mode;
    }

    public static ChargingStation createDefaultStation(ChargingStationMode mode) {
        ChargingStation station = new ChargingStation(350.0, mode);
        for (int i = 1; i <= 3; i++) station.addCharger(new StandardCharger("STD-" + i));
        for (int i = 1; i <= 3; i++) station.addCharger(new FastCharger("FAST-" + i));
        for (int i = 1; i <= 2; i++) station.addCharger(new SuperCharger("SUPER-" + i));
        return station;
    }

    public void addCharger(Charger charger) { if (charger != null) chargers.add(charger); }

    public void addVehicle(Vehicle vehicle) {
        if (vehicle == null) throw new IllegalArgumentException("vehicle cannot be null");
        waitingQueue.add(vehicle);
        totalArrived++;
        if (currentMode == ChargingStationMode.PHASE2_PRIORITY_DYNAMIC
                && reservationRandom.nextDouble() < RESERVATION_PROBABILITY) {
            reservationSystem.reserveSlot(vehicle);
        }
    }

    public void advanceOneMinute(int minute) {
        currentTime = minute;
        removeImpatientVehicles();
        recordCriticalIncidents();
        if (currentMode == ChargingStationMode.PHASE2_PRIORITY_DYNAMIC) applyWaitingBoosts();
        dispatchVehicles();
        totalActivePower = currentMode == ChargingStationMode.PHASE1_FCFS_STATIC
                ? allocateStaticPower() : allocateDynamicPower();
        cumulativePowerKwMinutes += totalActivePower;
        minutesSimulated++;
        updateChargers();
    }

    private void dispatchVehicles() {
        if (currentMode == ChargingStationMode.PHASE1_FCFS_STATIC) {
            while (!waitingQueue.isEmpty()) {
                Vehicle v = waitingQueue.peek();
                Charger charger = findCompatibleAvailable(v);
                if (charger == null) break;
                waitingQueue.poll();
                charger.startCharging(v, currentTime);
            }
        } else {
            List<Vehicle> candidates = new ArrayList<>(waitingQueue);
            List<Charger> available = new ArrayList<>();
            for (Charger c : chargers) if (c.isAvailable()) available.add(c);
            available.sort(Comparator.comparingDouble(Charger::getMaxPowerOutput).reversed());
            for (Charger charger : available) {
                Vehicle best = null;
                for (Vehicle candidate : candidates) {
                    if (!charger.canCharge(candidate)) continue;
                    if (best == null || priorityScore(candidate) > priorityScore(best)
                            || (priorityScore(candidate) == priorityScore(best)
                            && candidate.getArrivalTime() < best.getArrivalTime())) best = candidate;
                }
                if (best != null) {
                    candidates.remove(best);
                    charger.startCharging(best, currentTime);
                }
            }
            waitingQueue.clear();
            candidates.sort(Comparator.comparingInt(Vehicle::getArrivalTime));
            waitingQueue.addAll(candidates);
        }
    }

    private double allocateStaticPower() {
        if (chargers.isEmpty()) return 0.0;
        double perCharger = gridPowerBudget / chargers.size();
        double active = 0.0;
        for (Charger c : chargers) {
            double power = c.isOccupied() ? Math.min(c.getMaxPowerOutput(), perCharger) : 0.0;
            c.setCurrentPowerOutput(power);
            active += power;
        }
        return active;
    }

    private double allocateDynamicPower() {
        List<Charger> active = new ArrayList<>();
        for (Charger c : chargers) {
            c.setCurrentPowerOutput(0.0);
            if (c.isOccupied()) active.add(c);
        }
        double remaining = gridPowerBudget;
        List<Charger> open = new ArrayList<>(active);
        while (!open.isEmpty() && remaining > 0.0001) {
            double weightSum = 0.0;
            for (Charger c : open) weightSum += powerWeight(c);
            if (weightSum <= 0) break;
            boolean saturated = false;
            List<Charger> next = new ArrayList<>();
            double used = 0.0;
            for (Charger c : open) {
                double proposed = remaining * powerWeight(c) / weightSum;
                double capacity = c.getMaxPowerOutput() - c.getCurrentPowerOutput();
                if (proposed >= capacity - 0.0001) {
                    c.setCurrentPowerOutput(c.getMaxPowerOutput());
                    used += capacity;
                    saturated = true;
                } else next.add(c);
            }
            if (!saturated) {
                for (Charger c : open) c.setCurrentPowerOutput(c.getCurrentPowerOutput()
                        + remaining * powerWeight(c) / weightSum);
                remaining = 0.0;
            } else {
                remaining -= used;
                open = next;
            }
        }
        double activePower = 0.0;
        for (Charger c : chargers) if (c.isOccupied()) activePower += c.getCurrentPowerOutput();
        return activePower;
    }

    private double priorityScore(Vehicle v) {
        double score = v.getPriority();
        if (v.isCriticalBattery()) score += 2;
        if (v.hasReservation()) score += 3;
        return score;
    }

    private double powerWeight(Charger c) {
        Vehicle v = c.getConnectedVehicle();
        if (v == null) return 0.0;
        double weight = Math.max(1, priorityScore(v));
        if (v.isCriticalBattery()) weight += 2;
        double urgency = v.getEnergyNeededKWh() / v.getBatteryCapacity();
        weight += Math.min(4, urgency * 8);
        if (c instanceof HighVoltageCapable && ((HighVoltageCapable) c).canChargeFaster(v)) weight += 2;
        return weight;
    }

    private void updateChargers() {
        for (Charger c : chargers) {
            if (!c.isOccupied()) continue;
            c.chargeOneMinute(currentTime);
            if (c.getConnectedVehicle() != null && c.getConnectedVehicle().isFullyCharged()) {
                Vehicle finished = c.releaseVehicle(currentTime + 1);
                if (finished != null) servedVehicles.add(finished);
            }
        }
    }

    public void closeRemainingVehiclesAsLost(int endTime) {
        lostCustomers += waitingQueue.size();
        waitingQueue.clear();
    }

    private void removeImpatientVehicles() {
        Iterator<Vehicle> it = waitingQueue.iterator();
        while (it.hasNext()) {
            if (it.next().hasExceededPatience(currentTime)) {
                it.remove();
                lostCustomers++;
            }
        }
    }

    private void recordCriticalIncidents() {
        for (Vehicle v : waitingQueue) {
            int threshold = Math.max(5, v.getPatience() / 2);
            if (v.isCriticalBattery() && !v.isCriticalIncidentLogged()
                    && v.getWaitingTime(currentTime) >= threshold) {
                v.markCriticalIncidentLogged();
                criticalIncidents++;
            }
        }
    }

    private void applyWaitingBoosts() {
        for (Vehicle v : waitingQueue) {
            int expected = Math.min(v.getWaitingTime(currentTime) / WAIT_THRESHOLD, MAX_WAIT_BOOST);
            while (v.getPriorityBoost() < expected) {
                v.increasePriority();
                priorityBoostsApplied++;
            }
        }
    }

    private Charger findCompatibleAvailable(Vehicle v) {
        for (Charger c : chargers) if (c.isAvailable() && c.canCharge(v)) return c;
        return null;
    }

    public String generateDailyReport(String title) {
        StringBuilder report = new StringBuilder();
        report.append(title).append('\n').append("=".repeat(title.length())).append('\n');
        report.append(String.format("Mode                    : %s%n", currentMode.getDisplayName()));
        report.append(String.format("Vehicles arrived        : %d%n", totalArrived));
        report.append(String.format("Vehicles served         : %d%n", getServedCount()));
        report.append(String.format("Lost customers          : %d%n", lostCustomers));
        report.append(String.format("Critical incidents      : %d%n", criticalIncidents));
        report.append(String.format("Average wait            : %.1f min%n", getAverageWaitTime()));
        report.append(String.format("Grid utilization        : %.1f%%%n", getGridUtilization()));
        report.append(String.format("Reservations            : %d%n", getReservationCount()));
        report.append(String.format("Priority boosts         : %d%n", priorityBoostsApplied));
        return report.toString();
    }

    public String generateImprovementReport(ChargingStation baseline) {
        return String.format("Comparison: served %+d, lost %+d, average wait %+.1f min, grid utilization %+.1f points",
                getServedCount() - baseline.getServedCount(), lostCustomers - baseline.getLostCustomers(),
                getAverageWaitTime() - baseline.getAverageWaitTime(),
                getGridUtilization() - baseline.getGridUtilization());
    }

    public double getAverageWaitTime() {
        if (servedVehicles.isEmpty()) return 0.0;
        return servedVehicles.stream().mapToInt(Vehicle::getWaitBeforeCharging).average().orElse(0.0);
    }

    public double getGridUtilization() {
        if (gridPowerBudget <= 0 || minutesSimulated == 0) return 0.0;
        return cumulativePowerKwMinutes / (gridPowerBudget * minutesSimulated) * 100.0;
    }

    public double getServedRate() { return totalArrived == 0 ? 0 : getServedCount() * 100.0 / totalArrived; }
    public double getLostRate() { return totalArrived == 0 ? 0 : lostCustomers * 100.0 / totalArrived; }
    public double getGridPowerBudget() { return gridPowerBudget; }
    public double getTotalActivePower() { return totalActivePower; }
    public List<Charger> getChargers() { return Collections.unmodifiableList(chargers); }
    public List<Vehicle> getWaitingQueueSnapshot() { return new ArrayList<>(waitingQueue); }
    public List<Vehicle> getServedVehicles() { return Collections.unmodifiableList(servedVehicles); }
    public List<Vehicle> getWaitingQueueShallowCopy() { return new ArrayList<>(waitingQueue); }
    public List<Vehicle> getServedVehiclesShallowCopy() { return new ArrayList<>(servedVehicles); }
    public List<Vehicle> getWaitingQueueDeepCopy() { return copyVehicles(waitingQueue); }
    public List<Vehicle> getServedVehiclesDeepCopy() { return copyVehicles(servedVehicles); }
    private List<Vehicle> copyVehicles(Iterable<Vehicle> source) {
        List<Vehicle> copy = new ArrayList<>();
        for (Vehicle v : source) copy.add(v.deepCopy());
        return copy;
    }
    public int getLostCustomers() { return lostCustomers; }
    public int getCriticalIncidents() { return criticalIncidents; }
    public int getTotalArrived() { return totalArrived; }
    public int getServedCount() { return servedVehicles.size(); }
    public int getPriorityBoostsApplied() { return priorityBoostsApplied; }
    public int getReservationCount() { return reservationSystem.getReservationCount(); }
    public ChargingStationMode getCurrentMode() { return currentMode; }
    public void setCurrentMode(ChargingStationMode mode) { currentMode = mode; }
}
