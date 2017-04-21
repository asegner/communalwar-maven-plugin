package net.segner.maven.plugins.communal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class LibraryPrefixFilterTest {

    private static final String PREFIX_TEST = "testprefix";
    private static final String PREFIX_TEST2 = "testprefix2";
    private static final String LIBRARY_NAME_TEST = "testprefix-4.0.jar";

    private LibraryPrefixFilter underTest;

    @Test
    public void testPrefixGetter() throws Exception {
        // test
        underTest = new LibraryPrefixFilter(PREFIX_TEST);
        String result = underTest.getPrefix();

        // validate
        assertThat("Property 'prefix' using value: " + PREFIX_TEST, result, is(PREFIX_TEST));
    }

    @Test
    public void testPrefixMatchTrue() throws Exception {
        // test
        underTest = new LibraryPrefixFilter(PREFIX_TEST);
        Boolean result = underTest.isMatch(LIBRARY_NAME_TEST);

        // validate
        assertThat("Prefix successfully matches", result, is(true));
    }

    @Test
    public void testPrefixMatchFalse() throws Exception {
        // test
        underTest = new LibraryPrefixFilter(PREFIX_TEST2);
        Boolean result = underTest.isMatch(LIBRARY_NAME_TEST);

        // validate
        assertThat("Prefix does not match", result, is(false));
    }
}
