package at.fhtw.tourplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class OpenRouteService {

    private static final Logger logger = LogManager.getLogger(OpenRouteService.class);

    @Value("${openroute.api.key:5b3ce3597851110001cf62485aa12d7a641a4d409727cc48fee2e836}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RouteInfo getRouteInfo(String fromLocation, String toLocation, String transportType) {
        try {
            logger.debug("=== OpenRouteService Debug ===");
            logger.debug("API Key: {}", (apiKey != null && !apiKey.equals("your-api-key-here") ?
                    apiKey.substring(0, Math.min(8, apiKey.length())) + "..." : "MISSING OR DEFAULT"));
            logger.debug("From: {}", fromLocation);
            logger.debug("To: {}", toLocation);
            logger.debug("Transport: {}", transportType);

            // Geocoding für Start- und Endpunkt
            double[] startCoords = geocode(fromLocation);
            double[] endCoords = geocode(toLocation);

            if (startCoords == null || endCoords == null) {
                logger.error("Geocoding failed for locations: {} -> {}", fromLocation, toLocation);
                throw new RuntimeException("Could not geocode locations: " + fromLocation + " -> " + toLocation);
            }

            logger.debug("Start coords: [{}, {}]", startCoords[0], startCoords[1]);
            logger.debug("End coords: [{}, {}]", endCoords[0], endCoords[1]);

            // Route berechnen
            String profile = mapTransportTypeToProfile(transportType);
            String url = "https://api.openrouteservice.org/v2/directions/" + profile + "/geojson";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("coordinates", Arrays.asList(
                    Arrays.asList(startCoords[0], startCoords[1]),
                    Arrays.asList(endCoords[0], endCoords[1])
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            JsonNode features = jsonNode.get("features");
            if (features.size() == 0) {
                logger.error("No route found in API response");
                throw new RuntimeException("No route found");
            }

            JsonNode feature = features.get(0);
            JsonNode properties = feature.get("properties");
            JsonNode summary = properties.get("summary");

            double distance = summary.get("distance").asDouble() / 1000.0; // Convert to km
            double duration = summary.get("duration").asDouble() / 3600.0; // Convert to hours

            JsonNode geometry = feature.get("geometry");
            String routeGeometry = geometry.toString();

            logger.info("Route calculation successful: Distance={}km, Duration={}h", distance, duration);
            logger.debug("==============================");

            return new RouteInfo(distance, duration, routeGeometry, startCoords, endCoords);

        } catch (Exception e) {
            logger.error("OpenRouteService error: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching route info: " + e.getMessage(), e);
        }
    }

    private double[] geocode(String location) {
        try {
            String url = "https://api.openrouteservice.org/geocode/search";
            String fullUrl = url + "?text=" + location + "&size=1";

            logger.debug("Geocoding URL: {}", fullUrl);

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
                double longitude = coordinates.get(0).asDouble();
                double latitude = coordinates.get(1).asDouble();
                logger.debug("Geocoded '{}' to [{}, {}]", location, longitude, latitude);
                return new double[]{longitude, latitude};
            } else {
                logger.warn("No geocoding results for location: {}", location);
                return null;
            }
        } catch (Exception e) {
            logger.error("Geocoding error for location '{}': {}", location, e.getMessage(), e);
            return null;
        }
    }

    private String mapTransportTypeToProfile(String transportType) {
        String profile = switch (transportType.toLowerCase()) {
            case "car" -> "driving-car";
            case "bicycle" -> "cycling-regular";
            case "hiking" -> "foot-hiking";
            default -> "driving-car";
        };
        logger.debug("Mapped transport type '{}' to profile '{}'", transportType, profile);
        return profile;
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