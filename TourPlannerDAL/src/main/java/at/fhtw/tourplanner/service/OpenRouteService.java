package at.fhtw.tourplanner.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OpenRouteService {

    @Value("${openroute.api.key:your-api-key-here}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RouteInfo getRouteInfo(String fromLocation, String toLocation, String transportType) {
        try {
            // Geocoding für Start- und Endpunkt
            double[] startCoords = geocode(fromLocation);
            double[] endCoords = geocode(toLocation);

            if (startCoords == null || endCoords == null) {
                throw new RuntimeException("Could not geocode locations");
            }

            // Route berechnen
            String profile = mapTransportTypeToProfile(transportType);
            String url = String.format(
                    "https://api.openrouteservice.org/v2/directions/%s?api_key=%s&start=%f,%f&end=%f,%f",
                    profile, apiKey, startCoords[0], startCoords[1], endCoords[0], endCoords[1]
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            // Distanz und Zeit extrahieren
            JsonNode feature = jsonNode.get("features").get(0);
            JsonNode properties = feature.get("properties");
            JsonNode summary = properties.get("summary");

            double distance = summary.get("distance").asDouble() / 1000.0; // Convert to km
            double duration = summary.get("duration").asDouble() / 3600.0; // Convert to hours

            // Geometry für Karte
            JsonNode geometry = feature.get("geometry");
            String routeGeometry = geometry.toString();

            return new RouteInfo(distance, duration, routeGeometry, startCoords, endCoords);

        } catch (Exception e) {
            throw new RuntimeException("Error fetching route info: " + e.getMessage(), e);
        }
    }

    private double[] geocode(String location) {
        try {
            String url = String.format(
                    "https://api.openrouteservice.org/geocoding/v1/search?api_key=%s&text=%s&limit=1",
                    apiKey, location
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            JsonNode features = jsonNode.get("features");
            if (features.size() > 0) {
                JsonNode coordinates = features.get(0).get("geometry").get("coordinates");
                return new double[]{coordinates.get(0).asDouble(), coordinates.get(1).asDouble()};
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException("Geocoding failed for: " + location, e);
        }
    }

    private String mapTransportTypeToProfile(String transportType) {
        switch (transportType.toLowerCase()) {
            case "bicycle":
                return "cycling-regular";
            case "walking":
            case "hiking":
                return "foot-walking";
            case "car":
            default:
                return "driving-car";
        }
    }

    public static class RouteInfo {
        public final double distance;
        public final double duration;
        public final String routeGeometry;
        public final double[] startCoords;
        public final double[] endCoords;

        public RouteInfo(double distance, double duration, String routeGeometry,
                         double[] startCoords, double[] endCoords) {
            this.distance = distance;
            this.duration = duration;
            this.routeGeometry = routeGeometry;
            this.startCoords = startCoords;
            this.endCoords = endCoords;
        }
    }
}