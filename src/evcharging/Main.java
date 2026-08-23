package evcharging;

import java.util.Random;

/** Console entry point that compares the two station strategies on one deterministic day. */
public class Main {
    private static final int MINUTES_IN_DAY = 1440;
    private static final long RANDOM_SEED = 3642034L;

    public static void main(String[] args) {
        long seed = args.length > 0 ? Long.parseLong(args[0]) : RANDOM_SEED;
        ChargingStation phase1 = runOneDay(ChargingStationMode.PHASE1_FCFS_STATIC, seed);
        ChargingStation phase2 = runOneDay(ChargingStationMode.PHASE2_PRIORITY_DYNAMIC, seed);
        System.out.println(phase1.generateDailyReport("EV Charging Station - Phase 1 Daily Report"));
        System.out.println();
        System.out.println(phase2.generateDailyReport("EV Charging Station - Phase 2 Daily Report"));
        System.out.println();
        System.out.println(phase2.generateImprovementReport(phase1));
    }

    private static ChargingStation runOneDay(ChargingStationMode mode, long seed) {
        ChargingStation station = ChargingStation.createDefaultStation(mode);
        Random random = new Random(seed);
        int nextVehicleNumber = 1;
        for (int minute = 0; minute < MINUTES_IN_DAY; minute++) {
            int arrivals = samplePoisson(random, arrivalRateForMinute(minute));
            for (int i = 0; i < arrivals; i++) {
                String id = String.format("V%04d", nextVehicleNumber++);
                station.addVehicle(createRandomVehicle(id, minute, random));
            }
            station.advanceOneMinute(minute);
        }
        station.closeRemainingVehiclesAsLost(MINUTES_IN_DAY);
        return station;
    }

    private static int samplePoisson(Random random, double lambda) {
        double limit = Math.exp(-lambda);
        int count = 0;
        double product = 1.0;
        do {
            count++;
            product *= random.nextDouble();
        } while (product > limit);
        return count - 1;
    }

    private static double arrivalRateForMinute(int minute) {
        int hour = minute / 60;
        if (hour >= 7 && hour < 10) return 0.50;
        if (hour >= 16 && hour < 19) return 0.47;
        if (hour >= 11 && hour < 14) return 0.32;
        if (hour < 6 || hour >= 22) return 0.06;
        return 0.14;
    }

    private static Vehicle createRandomVehicle(String id, int arrivalTime, Random random) {
        double battery = random.nextDouble() < 0.18
                ? 5.0 + random.nextDouble() * 10.0
                : 16.0 + random.nextDouble() * 54.0;
        double roll = random.nextDouble();
        if (roll < 0.43) return new PrivateCar(id, battery, arrivalTime);
        if (roll < 0.61) return new Taxi(id, battery, arrivalTime);
        if (roll < 0.74) return new DeliveryVan(id, battery, arrivalTime);
        if (roll < 0.79) return new PoliceVehicle(id, battery, arrivalTime);
        if (roll < 0.83) return new Ambulance(id, battery, arrivalTime);
        if (roll < 0.95) return new ElectricBus(id, battery, arrivalTime);
        return new Highlander(id, battery, arrivalTime);
    }
}
