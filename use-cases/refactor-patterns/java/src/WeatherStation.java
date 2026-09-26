import java.util.ArrayList;
import java.util.List;

// Weather monitoring system where multiple displays need to be updated
public class WeatherStation {
    private float temperature;
    private float humidity;
    private float pressure;
    private List<String> displayUpdates;

    public WeatherStation() {
        this.temperature = 0.0f;
        this.humidity = 0.0f;
        this.pressure = 0.0f;
        this.displayUpdates = new ArrayList<>();
    }

    // Update weather measurements
    public void setMeasurements(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;

        // Update all displays when measurements change
        updateCurrentConditionsDisplay();
        updateStatisticsDisplay();
        updateForecastDisplay();
        updateHeatIndexDisplay();

        // In a real application, we would notify actual display objects
        for (String update : displayUpdates) {
            System.out.println(update);
        }
        displayUpdates.clear();
    }

    // Methods to update different displays
    private void updateCurrentConditionsDisplay() {
        String update = String.format("Current conditions: %.1f°F, %.1f%% humidity",
                temperature, humidity);
        displayUpdates.add(update);
    }

    private void updateStatisticsDisplay() {
        // In a real app, we would calculate actual statistics
        String update = String.format("Weather statistics: Avg/Max/Min temperature = %.1f/%.1f/%.1f",
                temperature - 2, temperature + 2, temperature - 5);
        displayUpdates.add(update);
    }

    private void updateForecastDisplay() {
        // In a real app, we would use pressure trends to predict weather
        String prediction = pressure < 29.92f ? "Watch out for cooler, rainy weather" :
                "Improving weather on the way!";
        displayUpdates.add("Forecast: " + prediction);
    }

    private void updateHeatIndexDisplay() {
        // Simple heat index calculation
        float heatIndex = (temperature + humidity) / 2;
        String update = String.format("Heat index: %.1f", heatIndex);
        displayUpdates.add(update);
    }

    // Getters for current measurements
    public float getTemperature() {
        return temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getPressure() {
        return pressure;
    }

    // Example usage method
    public static void main(String[] args) {
        WeatherStation weatherStation = new WeatherStation();

        // Weather changes - each change should update all displays
        System.out.println("--- Weather Update 1 ---");
        weatherStation.setMeasurements(80.0f, 65.0f, 30.4f);

        System.out.println("\n--- Weather Update 2 ---");
        weatherStation.setMeasurements(82.0f, 70.0f, 29.2f);

        System.out.println("\n--- Weather Update 3 ---");
        weatherStation.setMeasurements(78.0f, 90.0f, 29.2f);
    }
}