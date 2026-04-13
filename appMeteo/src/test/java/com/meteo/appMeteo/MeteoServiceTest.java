package com.meteo.appMeteo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.meteo.appMeteo.service.MeteoService;

class MeteoServiceTest {

    private HttpClient client;
    private MeteoService service;

    @BeforeEach
    void setup() {
        client = mock(HttpClient.class);
        service = new MeteoService(client); 
    }

    @Test
    void testCittaValida() throws Exception {
        HttpResponse<String> geoResponse = mock(HttpResponse.class);
        HttpResponse<String> meteoResponse = mock(HttpResponse.class);

        String geoJson = """
        { "results": [ { "latitude": 45.46, "longitude": 9.19 } ] }
        """;

        String meteoJson = """
        { "current_weather": { "temperature": 22.5, "windspeed": 5.2, "weathercode": 0 } }
        """;

        when(geoResponse.statusCode()).thenReturn(200);
        when(geoResponse.body()).thenReturn(geoJson);

        when(meteoResponse.statusCode()).thenReturn(200);
        when(meteoResponse.body()).thenReturn(meteoJson);

        when(client.send(any(), any(HttpResponse.BodyHandler.class)))
                .thenReturn(geoResponse)
                .thenReturn(meteoResponse);

        var result = service.getMeteoByCity("Milano");

        assertNotNull(result);
        assertEquals(22.5, result.getTemperatura());
        assertEquals("Cielo sereno", result.getDescrizione());
        assertFalse(result.isCached());
    }

    @Test
    void testCittaInesistente() throws Exception {
        HttpResponse<String> geoResponse = mock(HttpResponse.class);

        when(geoResponse.statusCode()).thenReturn(200);
        when(geoResponse.body()).thenReturn("{ \"results\": [] }");

        when(client.send(any(), any(HttpResponse.BodyHandler.class)))
                .thenReturn(geoResponse);

        assertThrows(RuntimeException.class,
                () -> service.getMeteoByCity("fakecity"));
    }

    @Test
    void testInputVuoto() {
        assertThrows(RuntimeException.class,
                () -> service.getMeteoByCity(""));
    }

    @Test
    void testErroreApi() throws Exception {
        HttpResponse<String> geoResponse = mock(HttpResponse.class);

        when(geoResponse.statusCode()).thenReturn(500);

        when(client.send(any(), any(HttpResponse.BodyHandler.class)))
                .thenReturn(geoResponse);

        assertThrows(RuntimeException.class,
                () -> service.getMeteoByCity("Torino"));
    }
}