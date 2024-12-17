package poly;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PolyTest {
  @BeforeAll
  public void setUp() throws Exception {
    clean();
  }

  @AfterAll
  public void tearDown() throws Exception {
    clean();
  }

  public void clean() throws Exception {
    Path base = Paths.get(System.getProperty("user.dir"), "unit-tests", "poly", "caseStudies");
    ProcessBuilder processBuilder =
      new ProcessBuilder(
        "/bin/bash", "-c", "find . -name 'modelRR.*' -delete && find . -name 'actualResults' -delete && find . -name 'actualStatistics' -delete"
      );
    processBuilder.directory(base.toFile());
    Process process = processBuilder.start();
    process.waitFor();
  }

  @Nested
  class RRFilesAnalysis {
    @ParameterizedTest
    @MethodSource("rrFilesAnalysisArguments")
    public void
    it_builds_the_correct_RR_files(String directory, String prismFileName, String propsFileName, String flags) throws Exception {
      Path base = Paths.get(System.getProperty("user.dir"), "unit-tests", "poly", "caseStudies", directory);

      ProcessBuilder processBuilder =
        new ProcessBuilder(
          "/bin/bash", "-c", "../../../../bin/polygames " + prismFileName + ".prism " + propsFileName + ".props --exportresults actualResults " + flags
        );
      processBuilder.directory(base.toFile());
      Process process = processBuilder.start();
      process.waitFor();

      String actual   = new String(Files.readAllBytes(base.resolve("modelRR.txt")),         StandardCharsets.UTF_8);
      String expected = new String(Files.readAllBytes(base.resolve("expectedModelRR.txt")), StandardCharsets.UTF_8);
      assertEquals(expected, actual, "The RR files differ");
    }

    private static Stream<Arguments> rrFilesAnalysisArguments() {
      return Stream.of(
        // directory, prism, props, flags
        Arguments.of("mdsm", "mdsm", "mdsm", ""),
        Arguments.of("taskGraph", "taskGraph", "taskGraph", ""),
        Arguments.of("jamming", "jamming", "jamming", "--const slots=2"),
        Arguments.of("repudiationHonestTptg", "repudiationHonestTptg", "repudiationHonestTptg", "--const p=0.01 --const K=500"),
        Arguments.of("powerControl", "powerControl", "powerControl", "--const k=300 --const powmax=3 --const fail=0.1 --const emax=10"),
        Arguments.of("publicGoodGame", "publicGoodGame", "publicGoodGame", "--const emax=100 --const einit=4 --const kmax=2 --const f=1.78 --const K=100"),
        Arguments.of("intrusionDetectionPolicies", "intrusionDetectionPolicies", "intrusionDetectionPolicies", "--const rounds=4 --const scenario=2 --const K=1")
      );
    }
  }

  @Nested
  class NumericalAnalysis {
    @ParameterizedTest
    @MethodSource("numericalAnalysisArguments")
    public void
    the_numerical_results_are_equal(String directory, String prismFileName, String propsFileName, String flags) throws Exception {
      Path base = Paths.get(System.getProperty("user.dir"), "unit-tests", "poly", "caseStudies", directory);

      ProcessBuilder processBuilder =
        new ProcessBuilder(
          "/bin/bash", "-c", "../../../../bin/polygames " + prismFileName + ".prism " + propsFileName + ".props --exportresults actualResults " + flags
        );
      processBuilder.directory(base.toFile());
      Process process = processBuilder.start();
      process.waitFor();

      String actual   = new String(Files.readAllBytes(base.resolve("actualResults")),   StandardCharsets.UTF_8);
      String expected = new String(Files.readAllBytes(base.resolve("expectedResults")), StandardCharsets.UTF_8);
      assertEquals(expected, actual, "The result files differ");
    }

    private static Stream<Arguments> numericalAnalysisArguments() {
      return Stream.of(
        // directory, prism, props, flags
        Arguments.of("mdsm", "mdsm", "mdsm", ""),
        Arguments.of("taskGraph", "taskGraph", "taskGraph", ""),
        Arguments.of("jamming", "jamming", "jamming", "--const slots=2"),
        Arguments.of("repudiationHonestTptg", "repudiationHonestTptg", "repudiationHonestTptg", "--const p=0.01 --const K=500"),
        Arguments.of("powerControl", "powerControl", "powerControl", "--const k=300 --const powmax=3 --const fail=0.1 --const emax=10"),
        Arguments.of("publicGoodGame", "publicGoodGame", "publicGoodGame", "--const emax=100 --const einit=4 --const kmax=2 --const f=1.78 --const K=100"),
        Arguments.of("intrusionDetectionPolicies", "intrusionDetectionPolicies", "intrusionDetectionPolicies", "--const rounds=4 --const scenario=2 --const K=1"),
        Arguments.of("mdsm", "mdsmWithArrays", "mdsm", ""),
        Arguments.of("taskGraph", "taskGraphWithArrays", "taskGraph", ""),
        Arguments.of("jamming", "jammingWithArrays", "jamming", "--const slots=2")
      );
    }
  }

  @Nested
  class MeasurementsAnalysis {
    @ParameterizedTest
    @MethodSource("measurementsAnalysisArguments")
    public void
    the_measurements_are_equal(String directory, String prismFileName, String propsFileName, String flags) throws Exception {
      Path base = Paths.get(System.getProperty("user.dir"), "unit-tests", "poly", "caseStudies", directory);
      ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", "../../../../bin/polygames " + prismFileName + ".prism " + propsFileName + ".props --exportresults actualResults " + flags + " | grep -e 'States:' -e 'Transitions:' -e 'Choices:' -e 'Max/avg:' | awk '/States:/ {print \"States=\" $2} /Transitions:/ {print \"Transitions=\" $2} /Choices:/ {print \"Choices=\" $2} /Max\\/avg:/ {print \"Max/avg=\" $2}' > actualMeasurements");
        
      processBuilder.directory(base.toFile());
      Process process = processBuilder.start();
      process.waitFor();
      
      String[] measurements = { "States", "Transitions", "Choices", "Max/avg" };

      for (String measurement : measurements) {
        assertEquals(
          getMeasurementByName(base, "expectedMeasurements", measurement), getMeasurementByName(base, "actualMeasurements", measurement)
        );
      }
    }

    private String getMeasurementByName(Path base, String propertiesFile, String key) throws Exception {
      InputStream input     = new FileInputStream(base.resolve(propertiesFile).toString());
      Properties properties = new Properties();
      properties.load(input);
      return properties.getProperty(key);
    }

    private static Stream<Arguments> measurementsAnalysisArguments() {
      return Stream.of(
        // directory, prism, props, flags
        Arguments.of("mdsm", "mdsm", "mdsm", "")
      );
    }
  }
}
