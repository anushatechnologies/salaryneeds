package com.salaryneeds.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GeoDistanceUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoDistanceUtils() {
        // Private constructor for utility class
    }

    /**
     * Calculates the great-circle distance between two points on the Earth's surface
     * using the Haversine formula.
     *
     * @param lat1 Latitude of point 1 (degrees)
     * @param lon1 Longitude of point 1 (degrees)
     * @param lat2 Latitude of point 2 (degrees)
     * @param lon2 Longitude of point 2 (degrees)
     * @return Distance in kilometers, rounded to 2 decimal places. Returns null if any coordinate is null.
     */
    public static Double calculateDistanceKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        double distance = EARTH_RADIUS_KM * c;

        return BigDecimal.valueOf(distance)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Generates a secure Google Maps turn-by-turn navigation URL for driving to the destination.
     * Compatible with Android Google Maps app, iOS Maps, and web browsers.
     *
     * @param destinationLat Destination latitude
     * @param destinationLng Destination longitude
     * @return Google Maps navigation URL, or null if coordinates are invalid
     */
    public static String buildGoogleMapsNavigationUrl(Double destinationLat, Double destinationLng) {
        if (destinationLat == null || destinationLng == null) {
            return null;
        }
        return String.format("https://www.google.com/maps/dir/?api=1&destination=%.6f,%.6f&travelmode=driving",
                destinationLat, destinationLng);
    }

    /**
     * Generates a Google Maps directions URL with origin and destination coordinates.
     *
     * @param originLat Origin latitude
     * @param originLng Origin longitude
     * @param destinationLat Destination latitude
     * @param destinationLng Destination longitude
     * @return Google Maps directions URL
     */
    public static String buildGoogleMapsNavigationUrl(Double originLat, Double originLng, Double destinationLat, Double destinationLng) {
        if (destinationLat == null || destinationLng == null) {
            return null;
        }
        if (originLat != null && originLng != null) {
            return String.format("https://www.google.com/maps/dir/?api=1&origin=%.6f,%.6f&destination=%.6f,%.6f&travelmode=driving",
                    originLat, originLng, destinationLat, destinationLng);
        }
        return buildGoogleMapsNavigationUrl(destinationLat, destinationLng);
    }

    /**
     * Generates a Google Maps Static API preview image URL.
     *
     * @param lat Latitude
     * @param lng Longitude
     * @param apiKey Google Maps API Key
     * @return Static Map image URL
     */
    public static String buildStaticMapPreviewUrl(Double lat, Double lng, String apiKey) {
        if (lat == null || lng == null || apiKey == null || apiKey.isBlank()) {
            return null;
        }
        return String.format("https://maps.googleapis.com/maps/api/staticmap?center=%.6f,%.6f&zoom=15&size=600x300&markers=color:red%%7Clabel:S%%7C%.6f,%.6f&key=%s",
                lat, lng, lat, lng, apiKey);
    }

    /**
     * Generates an Embed API URL for frontend iframe display.
     *
     * @param lat Latitude
     * @param lng Longitude
     * @param apiKey Google Maps API Key
     * @return Google Maps Embed URL
     */
    public static String buildEmbedMapUrl(Double lat, Double lng, String apiKey) {
        if (lat == null || lng == null || apiKey == null || apiKey.isBlank()) {
            return null;
        }
        return String.format("https://www.google.com/maps/embed/v1/place?key=%s&q=%.6f,%.6f",
                apiKey, lat, lng);
    }
}
