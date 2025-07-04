package at.fhtw.tourplanner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OpenRouteServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OpenRouteService openRouteService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(openRouteService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(openRouteService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(openRouteService, "objectMapper", objectMapper);
    }

    @Test
    void testGetRouteInfoSuccess() {
        // Mock geocoding responses
        String geocodeResponse = """
            {
                "features": [
                    {
                        "geometry": {
                            "coordinates": [16.3738, 48.2082]
                        }
                    }
                ]
            }
            """;

        String routeResponse = """
            {
                "features": [
                    {
                        "geometry": {
                            "type": "LineString",
                            "coordinates": [[16.3738, 48.2082], [13.0550, 47.8095]]
                        },
                        "properties": {
                            "summary": {
                                "distance": 300000,
                                "duration": 12600
                            }
                        }
                    }
                ]
            }
            """;

        when(restTemplate.exchange(contains("geocode"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(geocodeResponse));

        when(restTemplate.postForEntity(contains("directions"), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(routeResponse));

        OpenRouteService.RouteInfo result = openRouteService.getRouteInfo("Vienna", "Salzburg", "Car");

        assertNotNull(result);
        assertEquals(300.0, result.distance, 0.1);
        assertEquals(3.5, result.duration, 0.1);
        assertNotNull(result.routeGeometry);
        assertEquals(2, result.startCoords.length);
        assertEquals(2, result.endCoords.length);
    }

    @Test
    void testGeocodeFailure() {
        String emptyResponse = """
            {
                "features": []
            }
            """;

        when(restTemplate.exchange(contains("geocode"), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(emptyResponse));

        assertThrows(RuntimeException.class, () -> {
            openRouteService.getRouteInfo("InvalidLocation", "Salzburg", "Car");
        });
    }

    @Test
    void testMapTransportTypeToProfile() {
        // Use reflection to test private method
        try {
            java.lang.reflect.Method method = OpenRouteService.class.getDeclaredMethod("mapTransportTypeToProfile", String.class);
            method.setAccessible(true);

            assertEquals("driving-car", method.invoke(openRouteService, "Car"));
            assertEquals("cycling-regular", method.invoke(openRouteService, "Bicycle"));
            assertEquals("foot-hiking", method.invoke(openRouteService, "Hiking"));
            assertEquals("driving-car", method.invoke(openRouteService, "Unknown"));
        } catch (Exception e) {
            fail("Reflection test failed: " + e.getMessage());
        }
    }

    @Test
    void testRouteInfoConstructor() {
        double[] startCoords = {16.3738, 48.2082};
        double[] endCoords = {13.0550, 47.8095};
        String geometry = "{\"type\":\"LineString\"}";

        OpenRouteService.RouteInfo routeInfo = new OpenRouteService.RouteInfo(
                300.0, 3.5, geometry, startCoords, endCoords
        );

        assertEquals(300.0, routeInfo.distance);
        assertEquals(3.5, routeInfo.duration);
        assertEquals(geometry, routeInfo.routeGeometry);
        assertArrayEquals(startCoords, routeInfo.startCoords);
        assertArrayEquals(endCoords, routeInfo.endCoords);
    }
}