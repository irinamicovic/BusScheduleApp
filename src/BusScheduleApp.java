import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BusScheduleApp {
    private static final String STOPS_FILE = "gtfs/stops.txt";
    private static final String STOP_TIMES_FILE = "gtfs/stop_times.txt";
    private static final String TRIPS_FILE = "gtfs/trips.txt";
    private static final String ROUTES_FILE = "gtfs/routes.txt";

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: BusScheduleApp <stop_id> <number_of_buses> <relative|absolute>");
            return;
        }

        String stopId = args[0];
        int numberOfBuses = Integer.parseInt(args[1]);
        boolean isRelative = args[2].equalsIgnoreCase("relative");

        Map<String, String> stops = loadStops();
        Map<String, String> routes = loadRoutes();
        Map<String, TripInfo> trips = loadTrips();
        List<StopTime> stopTimes = loadStopTimes();

        String stopName = stops.get(stopId);
        if (stopName == null) {
            System.out.println("Unknown stop ID: " + stopId);
            return;
        }

        System.out.println("Postaja: " + stopName);

        List<StopTime> upcomingBuses = new ArrayList<>();
        LocalTime now = LocalTime.now();

        System.out.println("Current time: " + now);

        for (StopTime stopTime : stopTimes) {
            if (stopTime.getStopId().equals(stopId)) {
                LocalTime arrivalTime = LocalTime.parse(stopTime.getArrivalTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
                //System.out.println("Found bus with arrival time: " + arrivalTime);
                if (arrivalTime.isAfter(now) && arrivalTime.isBefore(now.plusHours(2))) {
                    upcomingBuses.add(stopTime);
                }
            }
        }

        Collections.sort(upcomingBuses);

        if (upcomingBuses.isEmpty()) {
            System.out.println("No upcoming buses found.");
        } else {
            for (int i = 0; i < Math.min(numberOfBuses, upcomingBuses.size()); i++) {
                StopTime stopTime = upcomingBuses.get(i);
                String tripId = stopTime.getTripId();
                TripInfo tripInfo = trips.get(tripId);
                String routeId = tripInfo.getRouteId();
                String routeName = routes.get(routeId);

                if (routeName == null) {
                    routeName = "Nepoznato";
                }

                LocalTime arrivalTime = LocalTime.parse(stopTime.getArrivalTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
                if (isRelative) {
                    long minutesUntilArrival = java.time.Duration.between(now, arrivalTime).toMinutes();
                    System.out.println(routeName + ": " + minutesUntilArrival + "min");
                } else {
                    System.out.println(routeName + ": " + arrivalTime);
                }
            }
        }
    }

    private static Map<String, String> loadStops() {
        Map<String, String> stops = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(STOPS_FILE))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                String stopId = fields[0];
                String stopName = fields[2];
                stops.put(stopId, stopName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stops;
    }

    private static Map<String, String> loadRoutes() {
        Map<String, String> routes = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ROUTES_FILE))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                String routeId = fields[0];
                String routeName = fields[2];
                routes.put(routeId, routeName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return routes;
    }

    private static Map<String, TripInfo> loadTrips() {
        Map<String, TripInfo> trips = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(TRIPS_FILE))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                String tripId = fields[2];
                String routeId = fields[0];
                String serviceId = fields[1];
                trips.put(tripId, new TripInfo(routeId, serviceId));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trips;
    }

    private static List<StopTime> loadStopTimes() {
        List<StopTime> stopTimes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(STOP_TIMES_FILE))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                String tripId = fields[0];
                String arrivalTime = fields[1];
                String departureTime = fields[2];
                String stopId = fields[3];
                int stopSequence = Integer.parseInt(fields[4]);
                stopTimes.add(new StopTime(tripId, arrivalTime, departureTime, stopId, stopSequence));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopTimes;
    }
}

class StopTime implements Comparable<StopTime> {
    private String tripId;
    private String arrivalTime;
    private String departureTime;
    private String stopId;
    private int stopSequence;

    public StopTime(String tripId, String arrivalTime, String departureTime, String stopId, int stopSequence) {
        this.tripId = tripId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopId = stopId;
        this.stopSequence = stopSequence;
    }

    public String getTripId() {
        return tripId;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public String getStopId() {
        return stopId;
    }

    @Override
    public int compareTo(StopTime other) {
        return this.arrivalTime.compareTo(other.arrivalTime);
    }
}

class TripInfo {
    private String routeId;
    private String serviceId;

    public TripInfo(String routeId, String serviceId) {
        this.routeId = routeId;
        this.serviceId = serviceId;
    }

    public String getRouteId() {
        return routeId;
    }
}
