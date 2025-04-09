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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PolyTest {
  @BeforeAll
  public void setUp() throws Exception {
    cleanUp();
  }

  @AfterAll
  public void tearDown() throws Exception {
    cleanUp();
  }

  public void cleanUp() throws Exception {
    Path caseStudiesDirectory = Paths.get(System.getProperty("user.dir"), "unit-tests", "poly", "caseStudies");
    ProcessBuilder command =
      new ProcessBuilder(
        "/bin/bash", "-c", "find . -name 'modelRR.*' -delete && find . -name 'actualResults' -delete"
      );
    
    command.directory(caseStudiesDirectory.toFile());
    Process process = command.start();
    process.waitFor();
  }

  @Nested
  class RRFilesAnalysis {
    @ParameterizedTest
    @MethodSource("rrFilesAnalysisArguments")
    public void
    itBuildsTheCorrectRRFiles(String directory, String prism, String props, String flags) throws Exception {
      Path caseStudyDirectory = Paths.get(System.getProperty("user.dir"), "unit-tests", "poly", "caseStudies", directory);

      ProcessBuilder command =
        new ProcessBuilder("/bin/bash", "-c", "../../bin/polygames " + prism + " " + props + " --exportresults actualResults " + flags);
      
      command.directory(caseStudyDirectory.toFile());
      Process process = command.start();
      process.waitFor();

      String actual   = new String(Files.readAllBytes(caseStudyDirectory.resolve("modelRR.txt")), StandardCharsets.UTF_8);
      String expected = new String(Files.readAllBytes(caseStudyDirectory.resolve("expectedModelRR.txt")), StandardCharsets.UTF_8);
      assertEquals(expected, actual, "The RR files differ.");
    }

    private static Stream<Arguments> rrFilesAnalysisArguments() {
      return Stream.of(
        // directory, prism, props, flags
        Arguments.of("mdsm", "mdsm.prism", "mdsm.props", ""),
        Arguments.of("taskGraph", "taskGraph.prism", "taskGraph.props", ""),
        Arguments.of("jamming", "jamming.prism", "jamming.props", "--const slots=2"),
        Arguments.of("repudiationHonestTptg", "repudiationHonestTptg.prism", "repudiationHonestTptg.props", "--const p=0.01 --const K=500"),
        Arguments.of("powerControl", "powerControl.prism", "powerControl.props", "--const k=300 --const powmax=3 --const fail=0.1 --const emax=10"),
        Arguments.of("publicGoodGame", "publicGoodGame.prism", "publicGoodGame.props", "--const emax=100 --const einit=4 --const kmax=2 --const f=1.78 --const K=100"),
        Arguments.of("intrusionDetectionPolicies", "intrusionDetectionPolicies.prism", "intrusionDetectionPolicies.props", "--const rounds=4 --const scenario=2 --const K=1")
      );
    }
  }

  @Nested
  class NumericalAnalysis {
    @ParameterizedTest
    @MethodSource("numericalAnalysisArguments")
    public void
    numericalResultsShouldBeEqual(String directory, String prism, String props, String flags) throws Exception {
      Path caseStudyDirectory = Paths.get(System.getProperty("user.dir"), "unit-tests", "poly", "caseStudies", directory);

      ProcessBuilder command =
        new ProcessBuilder("/bin/bash", "-c", "../../bin/polygames " + prism + " " + props + " --exportresults actualResults " + flags);
      
      command.directory(caseStudyDirectory.toFile());
      Process process = command.start();
      process.waitFor();

      String actual   = new String(Files.readAllBytes(caseStudyDirectory.resolve("actualResults")), StandardCharsets.UTF_8);
      String expected = new String(Files.readAllBytes(caseStudyDirectory.resolve("expectedResults")), StandardCharsets.UTF_8);
      assertEquals(expected, actual, "The result files differ.");
    }

    private static Stream<Arguments> numericalAnalysisArguments() {
      return Stream.of(
        // directory, prism, props, flags
        Arguments.of("mdsm", "mdsm.prism", "mdsm.props", ""),
        Arguments.of("taskGraph", "taskGraph.prism", "taskGraph.props", ""),
        Arguments.of("jamming", "jamming.prism", "jamming.props", "--const slots=2"),
        Arguments.of("repudiationHonestTptg", "repudiationHonestTptg.prism", "repudiationHonestTptg.props", "--const p=0.01 --const K=500"),
        Arguments.of("powerControl", "powerControl.prism", "powerControl.props", "--const k=300 --const powmax=3 --const fail=0.1 --const emax=10"),
        Arguments.of("publicGoodGame", "publicGoodGame.prism", "publicGoodGame.props", "--const emax=100 --const einit=4 --const kmax=2 --const f=1.78 --const K=100"),
        Arguments.of("intrusionDetectionPolicies", "intrusionDetectionPolicies.prism", "intrusionDetectionPolicies.props", "--const rounds=4 --const scenario=2 --const K=1"),
        Arguments.of("mdsm", "mdsmWithArrays.prism", "mdsm.props", ""),
        Arguments.of("taskGraph", "taskGraphWithArrays.prism", "taskGraph.props", ""),
        Arguments.of("jamming", "jammingWithArrays.prism", "jamming.props", "--const slots=2")
      );
    }
  }
}
