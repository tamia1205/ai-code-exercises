import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit test cases for WeatherStation class
 */
public class WeatherStationTest {
    private WeatherStation weatherStation;
    private final ByteArrayOutputStream outputCaptor = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        weatherStation = new WeatherStation();
        System.setOut(new PrintStream(outputCaptor));
    }

    @Test
    void testInitialValues() {
        assertEquals(0.0f, weatherStation.getTemperature());
        assertEquals(0.0f, weatherStation.getHumidity());
        assertEquals(0.0f, weatherStation.getPressure());
    }

    @Test
    void testSetMeasurements() {
        weatherStation.setMeasurements(75.0f, 60.0f, 30.0f);
        
        assertEquals(75.0f, weatherStation.getTemperature());
        assertEquals(60.0f, weatherStation.getHumidity());
        assertEquals(30.0f, weatherStation.getPressure());
    }

    @Test
    void testDisplayUpdates() {
        weatherStation.setMeasurements(75.0f, 60.0f, 30.0f);
        
        String output = outputCaptor.toString();
        
        // All displays should be updated
        assertTrue(output.contains("Current conditions: 75.0°F, 60.0% humidity"));
        assertTrue(output.contains("Weather statistics: Avg/Max/Min temperature = 73.0/77.0/70.0"));
        assertTrue(output.contains("Forecast: Improving weather on the way!"));
        assertTrue(output.contains("Heat index: 67.5"));
    }

    @Test
    void testForecastWithLowPressure() {
        weatherStation.setMeasurements(75.0f, 60.0f, 29.5f);
        
        String output = outputCaptor.toString();
        assertTrue(output.contains("Watch out for cooler, rainy weather"));
    }

    @Test
    void testForecastWithHighPressure() {
        weatherStation.setMeasurements(75.0f, 60.0f, 30.5f);
        
        String output = outputCaptor.toString();
        assertTrue(output.contains("Improving weather on the way!"));
    }

    @Test
    void testHeatIndexCalculation() {
        weatherStation.setMeasurements(85.0f, 75.0f, 30.0f);
        
        String output = outputCaptor.toString();
        assertTrue(output.contains("Heat index: 80.0"));
    }
    
    @Test
    void testMultipleUpdates() {
        weatherStation.setMeasurements(80.0f, 65.0f, 30.4f);
        outputCaptor.reset();
        
        weatherStation.setMeasurements(82.0f, 70.0f, 29.2f);
        String output = outputCaptor.toString();
        
        assertTrue(output.contains("Current conditions: 82.0°F, 70.0% humidity"));
        assertTrue(output.contains("Watch out for cooler, rainy weather"));
    }
    
    // Clean up after tests
    public void tearDown() {
        System.setOut(originalOut);
    }
}