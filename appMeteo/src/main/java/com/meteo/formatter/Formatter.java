package com.meteo.formatter;

public final class Formatter {
    
    private Formatter() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Converte il codice meteo Open-Meteo in una descrizione leggibile.
     *
     * @param code codice meteo restituito dall'API
     * @return descrizione testuale delle condizioni meteo
     */
    public static String weatherCodeToString(int code) {
        return switch (code) {
            case 0 -> "Cielo sereno";
            case 1, 2, 3 -> "Parzialmente nuvoloso";
            case 45, 48 -> "Nebbia";
            case 51, 53, 55 -> "Pioviggine";
            case 61, 63, 65 -> "Pioggia";
            case 71, 73, 75 -> "Neve";
            case 80, 81, 82 -> "Rovesci";
            case 95 -> "Temporale";
            case 96, 99 -> "Temporale con grandine";
            default -> "Condizione sconosciuta";
        };
    }
}
