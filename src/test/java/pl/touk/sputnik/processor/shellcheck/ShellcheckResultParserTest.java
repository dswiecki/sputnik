package pl.touk.sputnik.processor.shellcheck;

import com.google.common.io.Resources;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import pl.touk.sputnik.review.Violation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.StringStartsWith.startsWith;

@RunWith(JUnitParamsRunner.class)
public class ShellcheckResultParserTest {

    private ShellcheckResultParser shellcheckResultParser;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        shellcheckResultParser = new ShellcheckResultParser();
    }

    @Test
    @Parameters({
            "shellcheck/empty-file.txt",
            "shellcheck/no-violations.txt"
    })
    public void shouldParseNoViolations(String filePath) throws IOException, URISyntaxException {
        // given
        String response = IOUtils.toString(Resources.getResource(filePath).toURI());

        // when
        List<Violation> violations = shellcheckResultParser.parse(response);

        // then
        assertThat(violations).hasSize(0);
    }

    @Test
    @Parameters({
            "shellcheck/sample-output.txt"
    })
    public void shouldParseSampleViolations(String filePath) throws IOException, URISyntaxException {
        // given
        String response = IOUtils.toString(Resources.getResource(filePath).toURI());

        // when
        List<Violation> violations = shellcheckResultParser.parse(response);

        // then
        assertThat(violations).hasSize(3);
        assertThat(violations).extracting("filenameOrJavaClassName").contains("myotherscript", "myscript");
        assertThat(violations)
                .extracting("message")
                .contains("[Code:1035] Message: You need a space after the [ and before the ].")
                .contains("[Code:2039] Message: In POSIX sh, echo flags are undefined.")
                .contains("[Code:2086] Message: Double quote to prevent globbing and word splitting.");
    }

    @Test
    public void shouldThrowExceptionWhenViolationWithUnknownMessageType() throws IOException, URISyntaxException {
        String response = IOUtils.toString(Resources.getResource("shellcheck/unknown-message-output.txt").toURI());

        thrown.expect(ShellcheckException.class);
        thrown.expectMessage(startsWith("Unknown message type returned by shellcheck (type = fatal)"));
        shellcheckResultParser.parse(response);
    }
}
