# CO₂ Calculator (Java 21, Maven)

A CLI tool that estimates CO₂-equivalent emissions for a trip between two cities. Distances come from OpenRouteService geocoding and driving matrices to keep results realistic.

## Prerequisites
- Java 21 (JDK 21)
- `ORS_TOKEN` environment variable containing an OpenRouteService API key
- (Optional) Maven installation — the included wrapper (`./mvnw`) pins Maven 3.9.6

## Architecture
- **CLI entrypoint**: `Co2CalculatorCommand` wires Picocli parsing with dependency construction and logs user-facing output.
- **Application service**: `OpenRouteServiceTripCalculator` orchestrates the workflow—geocoding the cities, requesting a matrix distance, and applying the selected transport emission factor.
- **Infrastructure client**: `OpenRouteServiceClient` wraps OkHttp + Jackson calls to the ORS APIs, performing response validation and JSON extraction.
- **Domain types**: `TransportMethod` encapsulates available emission factors; `Coordinates` provides a minimal value object for longitude/latitude pairs.

## Why Picocli?
The project uses [Picocli](https://picocli.info) to avoid custom argument parsing:
- Handles both `--key=value` and `--key value` styles
- Generates `--help` / `--version` output automatically
- Provides validation and clear error messages
- Adds minimal overhead while keeping the CLI implementation concise

## Build
```bash
./mvnw test package
```
This creates a shaded JAR at `target/co2-calculator-1.0.0.jar`.

```powershell
.\mvnw.cmd test package
```
Runs natively on Windows—no WSL needed. If you just installed Java, open a fresh PowerShell or Command Prompt so the PATH updates.

## Run
Flags accept both `--key value` and `--key=value` forms and can be provided in any order:
macOS/Linux:
```bash
export ORS_TOKEN=YOUR_TOKEN_HERE
```

```bash
./co2-calculator \
  --start Hamburg \
  --end Berlin \
  --transportation-method diesel-car-medium

./co2-calculator \
  --start "Barcelona" \
  --end "Granada" \
  --transportation-method=electric-car-large
```

Windows (PowerShell or Command Prompt):
```powershell
setx ORS_TOKEN "YOUR_TOKEN_HERE"
```

```powershell
.\co2-calculator.bat `
  --start Hamburg `
  --end Berlin `
  --transportation-method diesel-car-medium

.\co2-calculator.bat `
  --start "Barcelona" `
  --end "Granada" `
  --transportation-method electric-car-large
```

## Transport Methods
Emission factors (g CO₂e per passenger-km) are shipped with the application. Check the `TransportMethod` enum for the full list of supported keys.

## Behaviour Notes
- Output is rounded to one decimal place: `Your trip caused {X.Y}kg of CO2-equivalent.`
- Uses ORS Geocode Search and Matrix APIs with the `driving-car` profile for distance calculations.
- Selects the highest-confidence geocoding match when multiple locations are returned.

## AI Assistance
I used AI tools for code suggestions and recommendations, but the implementation, style, and tests are my own.
