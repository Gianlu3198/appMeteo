// package com.meteo.meteoFetcher;

// import java.io.IOException;
// import java.net.URI;
// import java.net.URLEncoder;
// import java.net.http.HttpClient;
// import java.net.http.HttpRequest;
// import java.net.http.HttpResponse;
// import java.nio.charset.StandardCharsets;
// import java.time.Duration;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;

// import com.google.gson.JsonArray;
// import com.google.gson.JsonObject;
// import com.google.gson.JsonParser;
// import com.meteo.appMeteo.config.MeteoConfig;

// public class MeteoFetcher {

//     private final HttpClient client;

//     private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

//     // Costanti
//     private static final String GEO_URL = MeteoConfig.get("geo.url");
//     private static final String METEO_URL = MeteoConfig.get("meteo.url");

//     public MeteoFetcher(HttpClient client) {
//         this.client = client;
//     }

//     /**
//      * Recupera le informazioni meteorologiche correnti per una determinata città.
//      *
//      * <p>Il metodo esegue due chiamate API:
//      * <ul>
//      * <li>Geocoding API di Open-Meteo per ottenere latitudine e longitudine della
//      * città</li>
//      * <li>Weather API di Open-Meteo per ottenere i dati meteorologici correnti</li>
//      * </ul>
//      *
//      * <p>I dati restituiti includono:
//      * <ul>
//      * <li>Nome della città</li>
//      * <li>Temperatura corrente (°C)</li>
//      * <li>Descrizione delle condizioni meteo</li>
//      * </ul>
//      *
//      * @param citta Nome della città per cui recuperare i dati meteorologici.
//      * Non può essere null o vuoto.
//      *
//      * @return Un oggetto {@link com.google.gson.JsonObject} contenente:
//      * <ul>
//      * <li>"citta": nome della città richiesta</li>
//      * <li>"temperatura": temperatura corrente in gradi Celsius</li>
//      * <li>"descrizione": descrizione testuale delle condizioni meteo</li>
//      * <li>"errore": presente solo in caso di errore durante l'elaborazione</li>
//      * </ul>
//      *
//      * @throws RuntimeException se:
//      * <ul>
//      * <li>l'input città è vuoto o nullo</li>
//      * <li>la città non viene trovata tramite geocoding</li>
//      * <li>le API restituiscono un errore HTTP</li>
//      * </ul>
//      *
//      * @example
//      * <pre>{@code
//      * MeteoFetcher fetcher = new MeteoFetcher(HttpClient.newHttpClient());
//      * JsonObject result = fetcher.getWeather("Milano");
//      *
//      * System.out.println(result);
//      *
//      * Output esempio:
//      * {
//      * "citta": "Milano",
//      * "temperatura": 22.5,
//      * "descrizione": "Cielo sereno"
//      * }
//      * }</pre>
//      */
//     public JsonObject getWeather(String citta) {
//         try {
//             validateInput(citta);

//             String key = citta.trim().toLowerCase();

//             // 🔹 CACHE CHECK
//             CacheEntry entry = cache.get(key);

//             if (entry != null) {
//                 if (entry.isValid()) {
//                     JsonObject cached = entry.getData().deepCopy();
//                     cached.addProperty("cached", true);
//                     return cached;
//                 } else {
//                     cache.remove(key);
//                 }
//             }

//             // 🔹 FETCH COORDINATE
//             double[] coords = getCoordinates(citta);

//             // 🔹 FETCH METEO
//             JsonObject meteoJson = getMeteo(coords[0], coords[1]);

//             // 🔹 BUILD RESPONSE
//             JsonObject result = buildSuccessResponse(citta, meteoJson);
//             result.addProperty("cached", false);

//             // 🔹 STORE CACHE
//             cache.put(key, new CacheEntry(result.deepCopy()));

//             return result;

//         } catch (IOException e) {
//             return buildError("Errore di rete");
//         } catch (InterruptedException e) {
//             Thread.currentThread().interrupt();
//             return buildError("Richiesta interrotta");
//         } catch (RuntimeException e) {
//             return buildError("Richiesta non valida, controlla l'input");
//         }
//     }

//     // 🔹 VALIDAZIONE INPUT
//     private void validateInput(String citta) {
//         if (citta == null || citta.trim().isEmpty()) {
//             throw new RuntimeException("Input città non valido");
//         }

//         if (!citta.matches("[a-zA-ZàèéìòùÀÈÉÌÒÙ\\s-]+")) {
//             throw new RuntimeException("Formato città non valido");
//         }
//     }

//     // 🔹 GEOCODING
//     private double[] getCoordinates(String citta) throws IOException, InterruptedException {

//         String encodedCity = URLEncoder.encode(citta.trim(), StandardCharsets.UTF_8);
//         String url = GEO_URL + "?name=" + encodedCity + "&count=1";

//         HttpRequest request = HttpRequest.newBuilder()
//                 .uri(URI.create(url))
//                 .timeout(Duration.ofSeconds(5))
//                 .GET()
//                 .build();

//         HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

//         if (response.statusCode() != 200) {
//             throw new RuntimeException("Errore API geocoding");
//         }

//         JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
//         JsonArray results = json.getAsJsonArray("results");

//         if (results == null || results.isEmpty()) {
//             throw new RuntimeException("Città non trovata");
//         }

//         JsonObject city = results.get(0).getAsJsonObject();

//         return new double[] {
//                 city.get("latitude").getAsDouble(),
//                 city.get("longitude").getAsDouble()
//         };
//     }

//     // 🔹 METEO
//     private JsonObject getMeteo(double lat, double lon) throws IOException, InterruptedException {

//         String url = METEO_URL
//                 + "?latitude=" + lat
//                 + "&longitude=" + lon
//                 + "&current_weather=true";

//         HttpRequest request = HttpRequest.newBuilder()
//                 .uri(URI.create(url))
//                 .timeout(Duration.ofSeconds(5))
//                 .GET()
//                 .build();

//         HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

//         if (response.statusCode() != 200) {
//             throw new RuntimeException("Errore API meteo");
//         }

//         JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

//         if (!json.has("current_weather")) {
//             throw new RuntimeException("Dati meteo non disponibili");
//         }

//         return json.getAsJsonObject("current_weather");
//     }

//     // 🔹 RESPONSE OK
//     private JsonObject buildSuccessResponse(String citta, JsonObject current) {

//         double temperatura = current.get("temperature").getAsDouble();
//         int code = current.get("weathercode").getAsInt();

//         String descrizione = mapWeatherCode(code);

//         JsonObject result = new JsonObject();
//         result.addProperty("citta", citta);
//         result.addProperty("temperatura", temperatura);
//         result.addProperty("descrizione", descrizione);

//         return result;
//     }

//     // 🔹 MAPPING CODICI
//     private String mapWeatherCode(int code) {
//         return switch (code) {
//             case 0 -> "Cielo sereno";
//             case 1, 2, 3 -> "Parzialmente nuvoloso";
//             case 45, 48 -> "Nebbia";
//             case 61, 63, 65 -> "Pioggia";
//             case 71, 73, 75 -> "Neve";
//             case 95 -> "Temporale";
//             default -> "Condizione sconosciuta";
//         };
//     }

//     // 🔹 ERRORI
//     private JsonObject buildError(String message) {
//         JsonObject error = new JsonObject();
//         error.addProperty("errore", message);
//         return error;
//     }
// }
