package evcharging;

/**
 * Interface for objects that expose a scheduling priority.
 */
public interface Prioritizable {
    /**
     * Returns the current priority after any boosts.
     *
     * @return priority value
     */
    int getPriority();
}
