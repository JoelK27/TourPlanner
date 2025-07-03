package at.fhtw.tourplanner.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class OpenRouteService {

    @Value("${openroute.api.key:5b3ce3597851110001cf62485aa12d7a641a4d409727cc48fee2e836}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RouteInfo getRouteInfo(String fromLocation, String toLocation, String transportType) {
        try {
            System.out.println("=== OpenRouteService Debug ===");
            System.out.println("API Key: " + (apiKey != null && !apiKey.equals("your-api-key-here") ?
                    apiKey.substring(0, Math.min(8, apiKey.length())) + "..." : "MISSING OR DEFAULT"));
            System.out.println("From: " + fromLocation);
            System.out.println("To: " + toLocation);
            System.out.println("Transport: " + transportType);

            // Geocoding für Start- und Endpunkt
            double[] startCoords = geocode(fromLocation);
            double[] endCoords = geocode(toLocation);

            if (startCoords == null || endCoords == null) {
                System.err.println("ERROR: Geocoding failed!");
                throw new RuntimeException("Could not geocode locations: " + fromLocation + " -> " + toLocation);
            }

            System.out.println("Start coords: [" + startCoords[0] + ", " + startCoords[1] + "]");
            System.out.println("End coords: [" + endCoords[0] + ", " + endCoords[1] + "]");

            // Route berechnen - KORREKTE URL und Format laut Dokumentation
            String profile = mapTransportTypeToProfile(transportType);
            String url = "https://api.openrouteservice.org/v2/directions/" + profile + "/geojson";

            // Request Body erstellen - laut Dokumentation
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("coordinates", Arrays.asList(
                    Arrays.asList(startCoords[0], startCoords[1]),
                    Arrays.asList(endCoords[0], endCoords[1])
            ));

            // HTTP Headers setzen - Authorization header statt api_key parameter
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/geo+json");  // GeoJSON Accept Header

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("API URL: " + url);
            System.out.println("Request Body: " + objectMapper.writeValueAsString(requestBody));

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String responseBody = response.getBody();

            System.out.println("API Response received: " + (responseBody != null ? "SUCCESS" : "NULL"));

            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // GeoJSON Response-Format parsen
            JsonNode features = jsonNode.get("features");
            if (features.size() == 0) {
                throw new RuntimeException("No route found in response");
            }

            JsonNode feature = features.get(0);
            JsonNode properties = feature.get("properties");
            JsonNode summary = properties.get("summary");

            double distance = summary.get("distance").asDouble() / 1000.0; // Convert to km
            double duration = summary.get("duration").asDouble() / 3600.0; // Convert to hours

            // Geometry für Karte - bereits GeoJSON Format
            JsonNode geometry = feature.get("geometry");
            String routeGeometry = geometry.toString();

            System.out.println("SUCCESS: Distance=" + distance + "km, Duration=" + duration + "h");
            System.out.println("==============================");

            return new RouteInfo(distance, duration, routeGeometry, startCoords, endCoords);

        } catch (Exception e) {
            System.err.println("=== OpenRouteService ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.err.println("==============================");
            throw new RuntimeException("Error fetching route info: " + e.getMessage(), e);
        }
    }

    private double[] geocode(String location) {
        try {
            // Verwende Authorization Header statt Query Parameter
            String url = "https://api.openrouteservice.org/geocode/search";

            // Query Parameters
            String fullUrl = url + "?text=" + location + "&size=1";

            System.out.println("Geocoding URL: " + fullUrl);

            // HTTP Headers mit Authorization
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.set("Accept", "application/json");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            JsonNode features = jsonNode.get("features");
            if (features.size() > 0) {
                JsonNode coordinates = features.get(0).get("geometry").get("coordinates");
                double[] coords = new double[]{coordinates.get(0).asDouble(), coordinates.get(1).asDouble()};
                System.out.println("Geocoded " + location + " to: [" + coords[0] + ", " + coords[1] + "]");
                return coords;
            }

            return null;
        } catch (Exception e) {
            System.err.println("Geocoding failed for: " + location + " - " + e.getMessage());
            return null;
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