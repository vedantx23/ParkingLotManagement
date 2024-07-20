import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class ParkingSystem {
    private Map<String, Integer> parkingSlots;
    private int totalSlots;
    Connection connection;

    public ParkingSystem(int totalSlots) {
        this.totalSlots = totalSlots;
        this.parkingSlots = new HashMap<>();
        initializeParkingSlots();
        connectToDatabase();
    }

    private void initializeParkingSlots() {
        for (int i = 1; i <= totalSlots; i++) {
            parkingSlots.put("Slot" + i, 0); // 0 represents an empty slot
        }
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javaproject", "root", "Manu230204%");

            String createTableSQL = "CREATE TABLE IF NOT EXISTS parking (slot TEXT PRIMARY KEY, car_details TEXT)";
            connection.createStatement().executeUpdate(createTableSQL);

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void parkCar(String carDetails) {
        for (Map.Entry<String, Integer> entry : parkingSlots.entrySet()) {
            if (entry.getValue() == 0) {
                String slot = entry.getKey();
                parkingSlots.put(slot, 1); // 1 represents an occupied slot
                System.out.println("Car parked at " + slot + ": " + carDetails);

                // Save the car details to the database
                saveCarToDatabase(slot, carDetails);

                return;
            }
        }
        System.out.println("Sorry, parking is full.");
    }

    private void saveCarToDatabase(String slot, String carDetails) {
        try {
            String insertSQL = "INSERT INTO parking (slot, car_details) VALUES (?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(1, slot);
            preparedStatement.setString(2, carDetails);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void displayParkingStatus() {
        System.out.println("Parking Status:");
        for (Map.Entry<String, Integer> entry : parkingSlots.entrySet()) {
            System.out.println(entry.getKey() + ": " + (entry.getValue() == 0 ? "Empty" : "Occupied"));
        }
    }
}

public class ParkingApp {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter the total number of parking slots: ");
            int totalSlots = scanner.nextInt();
            scanner.nextLine(); // consume the newline character

            ParkingSystem parkingSystem = new ParkingSystem(totalSlots);

            while (true) {
                System.out.println("\n1. Park a car");
                System.out.println("2. Display parking status");
                System.out.println("3. Exit");
                System.out.print("Choose an option (1/2/3): ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // consume the newline character

                switch (choice) {
                    case 1:
                        System.out.print("Enter car details: ");
                        String carDetails = scanner.nextLine();
                        parkingSystem.parkCar(carDetails);
                        break;
                    case 2:
                        parkingSystem.displayParkingStatus();
                        break;
                    case 3:
                        System.out.println("Exiting the application. Goodbye!");
                        // Close the database connection before exiting
                        try {
                            parkingSystem.connection.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid choice. Please choose again.");
                }
            }
        }
    }
}