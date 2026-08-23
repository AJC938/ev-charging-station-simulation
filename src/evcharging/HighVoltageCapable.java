package evcharging;

/**
 * Interface for chargers that can provide faster charging to eligible vehicles.
 */
public interface HighVoltageCapable {
    /**
     * Checks whether a vehicle can benefit from faster charging.
     *
     * @param vehicle vehicle being evaluated
     * @return true when faster charging should apply
     */
    boolean canChargeFaster(Vehicle vehicle);
}
