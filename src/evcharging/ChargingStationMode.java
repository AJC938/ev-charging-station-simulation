package evcharging;

public enum ChargingStationMode {
    PHASE1_FCFS_STATIC("FCFS + Static Power"),
    PHASE2_PRIORITY_DYNAMIC("Priority + Dynamic Power");

    private final String displayName;

    ChargingStationMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
