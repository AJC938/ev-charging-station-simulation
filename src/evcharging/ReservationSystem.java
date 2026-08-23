package evcharging;

import java.util.HashMap;
import java.util.Map;

/** Tracks Phase 2 reservations by vehicle ID. */
public class ReservationSystem {
    private final Map<String, Integer> reservationsByVehicleID = new HashMap<>();

    public boolean reserveSlot(Vehicle vehicle) {
        if (vehicle == null) return false;
        vehicle.reserve();
        reservationsByVehicleID.put(vehicle.getVehicleID(), vehicle.getArrivalTime());
        return true;
    }

    public boolean validateReservation(Vehicle vehicle) {
        return vehicle != null && vehicle.hasReservation()
                && reservationsByVehicleID.containsKey(vehicle.getVehicleID());
    }

    public int getReservationCount() { return reservationsByVehicleID.size(); }
}
