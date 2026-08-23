package tests;

import evcharging.*;

/** Lightweight regression suite using only the Java standard library. */
public final class DSATest {
    private static int passed;

    public static void main(String[] args) {
        testVehiclePriorityAndCopies();
        testReservationSystem();
        testChargerCompatibility();
        testChargingStationModes();
        System.out.println("All tests passed: " + passed);
    }

    private static void testVehiclePriorityAndCopies() {
        Ambulance ambulance = new Ambulance("AMB-1", 10, 0);
        assertEquals("Ambulance priority", 5, ambulance.getPriority());
        ambulance.increasePriority(2);
        assertEquals("Priority boost", 7, ambulance.getPriority());
        Vehicle shallow = ambulance.shallowCopy();
        Vehicle deep = ambulance.deepCopy();
        assertTrue("Shallow copy is distinct", shallow != ambulance);
        assertTrue("Deep copy is distinct", deep != ambulance);
        assertEquals("Deep copy type", "Ambulance", deep.getType());
    }

    private static void testReservationSystem() {
        ReservationSystem reservations = new ReservationSystem();
        Taxi taxi = new Taxi("T-1", 30, 12);
        assertTrue("Reservation created", reservations.reserveSlot(taxi));
        assertTrue("Reservation validates", reservations.validateReservation(taxi));
        assertEquals("Reservation count", 1, reservations.getReservationCount());
    }

    private static void testChargerCompatibility() {
        Charger standard = new StandardCharger("C1");
        Charger fast = new FastCharger("C2");
        Charger superCharger = new SuperCharger("C3");
        PrivateCar car = new PrivateCar("P-1", 40, 0);
        ElectricBus bus = new ElectricBus("B-1", 40, 0);
        assertTrue("Standard accepts car", standard.canCharge(car));
        assertTrue("Fast accepts car", fast.canCharge(car));
        assertTrue("Fast rejects bus", !fast.canCharge(bus));
        assertTrue("Super accepts bus", superCharger.canCharge(bus));
    }

    private static void testChargingStationModes() {
        ChargingStation phase1 = ChargingStation.createDefaultStation(ChargingStationMode.PHASE1_FCFS_STATIC);
        ChargingStation phase2 = ChargingStation.createDefaultStation(ChargingStationMode.PHASE2_PRIORITY_DYNAMIC);
        assertEquals("Phase 1 mode", ChargingStationMode.PHASE1_FCFS_STATIC, phase1.getCurrentMode());
        assertEquals("Phase 2 mode", ChargingStationMode.PHASE2_PRIORITY_DYNAMIC, phase2.getCurrentMode());
        phase2.addVehicle(new PrivateCar("P-2", 25, 0));
        phase2.advanceOneMinute(0);
        assertTrue("Vehicle is tracked", phase2.getTotalArrived() >= 1);
        assertTrue("Grid budget positive", phase2.getGridPowerBudget() > 0);
        assertTrue("Station has chargers", !phase2.getChargers().isEmpty());
    }

    private static void assertTrue(String name, boolean value) {
        if (!value) throw new AssertionError(name);
        passed++;
    }

    private static void assertEquals(String name, Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError(name + ": expected=" + expected + ", actual=" + actual);
        passed++;
    }
}
