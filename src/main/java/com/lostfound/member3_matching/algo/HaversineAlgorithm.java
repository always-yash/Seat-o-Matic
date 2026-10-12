package com.lostfound.member3_matching.algo;

/**
 * Custom manual implementation of Haversine Formula for distance calculation
 * and geographical similarity scoring between two GPS coordinates.
 */
public class HaversineAlgorithm {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculates distance between two coordinates in kilometers using manual Haversine formula.
     */
    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(rLat1) * Math.cos(rLat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Converts distance into similarity score according to configurable thresholds:
     * 0 – 0.1 km = 100%
     * 0.1 – 0.5 km = 90%
     * 0.5 – 1.0 km = 75%
     * 1.0 – 2.0 km = 60%
     * 2.0 – 5.0 km = 40%
     * > 5.0 km = 20%
     */
    public static double convertDistanceToScore(double distanceKm) {
        if (distanceKm <= 0.10) {
            return 100.0;
        } else if (distanceKm <= 0.50) {
            return 90.0;
        } else if (distanceKm <= 1.00) {
            return 75.0;
        } else if (distanceKm <= 2.00) {
            return 60.0;
        } else if (distanceKm <= 5.00) {
            return 40.0;
        } else {
            return 20.0;
        }
    }

    /**
     * Calculates geographical similarity score between two coordinates.
     * If GPS coordinates are not provided (e.g. 0.0), evaluates location text similarity.
     */
    public static double calculateLocationScore(Double lat1, Double lon1, String locName1,
                                                Double lat2, Double lon2, String locName2) {
        boolean hasGps1 = lat1 != null && lon1 != null && (lat1 != 0.0 || lon1 != 0.0);
        boolean hasGps2 = lat2 != null && lon2 != null && (lat2 != 0.0 || lon2 != 0.0);

        if (hasGps1 && hasGps2) {
            double distance = calculateDistanceKm(lat1, lon1, lat2, lon2);
            return convertDistanceToScore(distance);
        }

        // Fallback: Use string text matching for location names
        if (locName1 != null && locName2 != null) {
            return LevenshteinAlgorithm.calculateTextScore(locName1, locName2);
        }

        return 50.0; // Default neutral score when no coordinates or names exist
    }
}
