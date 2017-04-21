package net.segner.maven.plugins.communal.weblogic;

import net.segner.maven.plugins.communal.module.EarModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

@RunWith(MockitoJUnitRunner.class)
public class WeblogicApplicationXmlTest {

    WeblogicApplicationXml weblogicApplicationXml;

    @Mock
    EarModule earModule;

    @Before
    public void before() {
        weblogicApplicationXml = new WeblogicApplicationXml(earModule);
    }

    @Test
    public void findClassloaderStructure_NotExisting() throws Exception {
        // test
        WeblogicClassloaderStructure classloaderStructure = weblogicApplicationXml.findClassloaderStructure();

        // validate
        assertThat("Classloader structure is null if none exists in the document",
                classloaderStructure, nullValue());
    }

    @Test
    public void createClassloaderStructure() throws Exception {
        // test
        WeblogicClassloaderStructure classloaderStructure = weblogicApplicationXml.createClassloaderStructure();

        // validate
        assertThat("Create classloader created something",
                classloaderStructure, notNullValue(WeblogicClassloaderStructure.class));
        assertThat("Classloader structure has a relationship with provided weblogicApplicationXml",
                Whitebox.getInternalState(classloaderStructure, "applicationXml"), is(weblogicApplicationXml));
    }

}