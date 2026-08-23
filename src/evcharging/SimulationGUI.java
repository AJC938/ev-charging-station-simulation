package evcharging;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Lightweight Swing dashboard for observing the charging-station simulation. */
public class SimulationGUI extends JFrame {
    private static final int DAY_LENGTH = 1440;
    private final JLabel clockLabel = new JLabel("Minute 0 / 1440");
    private final JLabel modeLabel = new JLabel();
    private final JLabel queueLabel = new JLabel();
    private final JLabel servedLabel = new JLabel();
    private final JLabel powerLabel = new JLabel();
    private final JLabel utilizationLabel = new JLabel();
    private final JTextArea eventLog = new JTextArea(14, 58);
    private final JComboBox<ChargingStationMode> modeBox = new JComboBox<>(ChargingStationMode.values());
    private final JButton startButton = new JButton("Start");
    private final JButton resetButton = new JButton("Reset");
    private ChargingStation station;
    private Random random;
    private int minute;
    private int vehicleNumber;
    private Timer timer;

    public SimulationGUI() {
        super("EV Charging Station Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 620);
        setLocationRelativeTo(null);
        buildUi();
        resetSimulation();
    }

    private void buildUi() {
        JPanel top = new JPanel(new GridLayout(2, 4, 10, 8));
        top.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        top.add(new JLabel("Simulation")); top.add(clockLabel);
        top.add(new JLabel("Mode")); top.add(modeLabel);
        top.add(new JLabel("Queue")); top.add(queueLabel);
        top.add(new JLabel("Served")); top.add(servedLabel);
        top.add(new JLabel("Active Power")); top.add(powerLabel);
        top.add(new JLabel("Grid Utilization")); top.add(utilizationLabel);
        top.add(modeBox); top.add(startButton);
        top.add(resetButton); top.add(new JLabel(""));

        eventLog.setEditable(false);
        eventLog.setLineWrap(true);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(eventLog), BorderLayout.CENTER);

        modeBox.addActionListener(e -> {
            if (timer == null || !timer.isRunning()) resetSimulation();
        });
        startButton.addActionListener(e -> toggleSimulation());
        resetButton.addActionListener(e -> resetSimulation());
    }

    private void resetSimulation() {
        if (timer != null) timer.stop();
        station = ChargingStation.createDefaultStation((ChargingStationMode) modeBox.getSelectedItem());
        random = new Random(3642034L);
        minute = 0;
        vehicleNumber = 1;
        startButton.setText("Start");
        eventLog.setText("Ready. Press Start to run the selected strategy.\n");
        refresh();
    }

    private void toggleSimulation() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            startButton.setText("Resume");
            return;
        }
        if (minute >= DAY_LENGTH) resetSimulation();
        timer = new Timer(35, e -> tick());
        timer.start();
        startButton.setText("Pause");
    }

    private void tick() {
        int arrivals = samplePoisson(random, arrivalRate(minute));
        for (int i = 0; i < arrivals; i++) {
            String id = String.format("V%04d", vehicleNumber++);
            station.addVehicle(randomVehicle(id, minute));
        }
        station.advanceOneMinute(minute);
        if (arrivals > 0) eventLog.append(String.format("Minute %04d: %d vehicle(s) arrived.%n", minute, arrivals));
        minute++;
        refresh();
        if (minute >= DAY_LENGTH) {
            timer.stop();
            station.closeRemainingVehiclesAsLost(DAY_LENGTH);
            startButton.setText("Restart");
            eventLog.append("Simulation complete.\n");
        }
    }

    private void refresh() {
        clockLabel.setText(String.format("%d / %d", minute, DAY_LENGTH));
        modeLabel.setText(station.getCurrentMode().getDisplayName());
        queueLabel.setText(String.valueOf(station.getWaitingQueueSnapshot().size()));
        servedLabel.setText(String.valueOf(station.getServedCount()));
        powerLabel.setText(String.format("%.1f kW", station.getTotalActivePower()));
        utilizationLabel.setText(String.format("%.1f%%", station.getGridUtilization()));
    }

    private Vehicle randomVehicle(String id, int arrival) {
        double battery = random.nextDouble() < 0.18
                ? 5.0 + random.nextDouble() * 10.0
                : 16.0 + random.nextDouble() * 54.0;
        double roll = random.nextDouble();
        if (roll < 0.43) return new PrivateCar(id, battery, arrival);
        if (roll < 0.61) return new Taxi(id, battery, arrival);
        if (roll < 0.74) return new DeliveryVan(id, battery, arrival);
        if (roll < 0.79) return new PoliceVehicle(id, battery, arrival);
        if (roll < 0.83) return new Ambulance(id, battery, arrival);
        if (roll < 0.95) return new ElectricBus(id, battery, arrival);
        return new Highlander(id, battery, arrival);
    }

    private static int samplePoisson(Random random, double lambda) {
        double limit = Math.exp(-lambda);
        double product = 1.0;
        int count = 0;
        do { count++; product *= random.nextDouble(); } while (product > limit);
        return count - 1;
    }

    private static double arrivalRate(int minute) {
        int hour = minute / 60;
        if (hour >= 7 && hour < 10) return 0.50;
        if (hour >= 16 && hour < 19) return 0.47;
        if (hour >= 11 && hour < 14) return 0.32;
        if (hour < 6 || hour >= 22) return 0.06;
        return 0.14;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimulationGUI().setVisible(true));
    }
}
