package at.fhtw.tourplanner.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ImageService {
    @Value("${openroute.api.key}")
    private String orsApiKey;

    public byte[] fetchTourMapImage(String encodedRouteGeometry, double[] startCoords, double[] endCoords) {
        try {
            String baseUrl = "https://api.openrouteservice.org/staticmap";
            String size = "1200x800";
            String center = startCoords[1] + "," + startCoords[0];
            String zoom = "10";
            
            // Marker für Start und End
            String markers = "color:green|" + startCoords[1] + "," + startCoords[0] + "|" +
                            "color:red|" + endCoords[1] + "," + endCoords[0];

            String url = baseUrl +
                    "?api_key=" + URLEncoder.encode(orsApiKey, StandardCharsets.UTF_8) +
                    "&size=" + URLEncoder.encode(size, StandardCharsets.UTF_8) +
                    "&center=" + URLEncoder.encode(center, StandardCharsets.UTF_8) +
                    "&zoom=" + URLEncoder.encode(zoom, StandardCharsets.UTF_8) +
                    "&markers=" + URLEncoder.encode(markers, StandardCharsets.UTF_8);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.err.println("ORS static map error: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error fetching static map image: " + e.getMessage());
            return null;
        }
    }
}