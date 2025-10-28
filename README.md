# CO₂ Calculator (Java 24, Maven)

A command-line tool that computes CO₂-equivalent emissions for a trip between two cities, using OpenRouteService for geocoding and driving distance.

## Requirements
- Java 24 (JDK 24)
- Maven 3.9+
- OpenRouteService API key in env var **ORS_TOKEN**

## Build
```bash
mvn -q -DskipTests=false test package
```
This will produce a shaded (fat) JAR in `target/co2-calculator-1.0.0-shaded.jar`.

## Run
Examples (order-agnostic flags, `--k v` or `--k=v`):
```bash
export ORS_TOKEN=YOUR_TOKEN_HERE

java -jar target/co2-calculator-1.0.0-shaded.jar   --start Hamburg --end Berlin --transportation-method diesel-car-medium

java -jar target/co2-calculator-1.0.0-shaded.jar   --start "Los Angeles" --end "New York" --transportation-method=electric-car-large
```

## Transport methods
Keys and factors (g CO₂e per passenger-km) are embedded in the app. See `TransportMethod` enum for the full list.

## Notes
- Rounds to **1 decimal** and prints: `Your trip caused {X.Y}kg of CO2-equivalent.`
- Uses ORS Geocode Search and Matrix API (profile: `driving-car`) to obtain a realistic road distance.
- If multiple geocoding results exist, the first (highest-confidence) one is used.
# sap-coding-challenge
