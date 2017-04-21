package net.segner.maven.plugins.communal.weblogic;

import lombok.extern.slf4j.Slf4j;
import net.java.truevfs.access.TFile;
import net.java.truevfs.access.TFileInputStream;
import net.java.truevfs.access.TFileOutputStream;
import net.segner.maven.plugins.communal.module.ApplicationModule;
import net.segner.maven.plugins.communal.module.EarModule;
import net.segner.maven.plugins.communal.module.EjbModule;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class WeblogicApplicationXml {

    public static final String TAG_WEBLOGIC_APPLICATION = "weblogic-application";
    public static final String PATH_RELATIVE_WL_APP_FOLDER = "META-INF";

    private final EarModule earModule;
    private TFile weblogicappXml;
    private Document document;
    private WeblogicClassloaderStructure wcs;

    public WeblogicApplicationXml(EarModule ear) {
        Validate.notNull(ear);
        this.earModule = ear;
        weblogicappXml = new TFile(ear.getModuleRoot(), PATH_RELATIVE_WL_APP_FOLDER + File.separator + "weblogic-application.xml");
    }

    public void persistToEarModule() throws TransformerException, IOException {
        try (TFileOutputStream outputStream = new TFileOutputStream(weblogicappXml)) {

            final DOMImplementationLS ls = (DOMImplementationLS) document.getImplementation().getFeature("LS", "3.0");
            final LSOutput out = ls.createLSOutput();
            out.setByteStream(outputStream);

            final LSSerializer serializer = ls.createLSSerializer();
            serializer.getDomConfig().setParameter("format-pretty-print", true);
            serializer.write(document, out);
        }

        // copy to unpacked location
        earModule.copy(weblogicappXml, PATH_RELATIVE_WL_APP_FOLDER);
    }

    @Nullable
    public WeblogicClassloaderStructure findClassloaderStructure() throws ParserConfigurationException, SAXException, IOException {
        WeblogicClassloaderStructure weblogicClassloaderStructure = wcs;

        // locate an existing classloader structure
        if (weblogicClassloaderStructure == null) {
            Element root = getXmlRoot();
            NodeList matches = root.getElementsByTagName(WeblogicClassloaderStructure.TAG_CLASSLOADER_STRUCTURE);
            if (matches != null && matches.getLength() > 0) {
                for (int i = 0; i < matches.getLength(); i++) {
                    if (matches.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        Element el = (Element) matches.item(i);
                        if (root.equals(el.getParentNode())) {
                            weblogicClassloaderStructure = new WeblogicClassloaderStructure(this);
                            break;
                        }
                    }
                }
            }
        }
        return weblogicClassloaderStructure;
    }

    @Nonnull
    public WeblogicClassloaderStructure createClassloaderStructure() throws IOException, SAXException, ParserConfigurationException {
        // create a new classloader structure
        Element newroot = getDocument().createElement(WeblogicClassloaderStructure.TAG_CLASSLOADER_STRUCTURE);
        getXmlRoot().appendChild(newroot);
        return new WeblogicClassloaderStructure(this);
    }

    @Nonnull
    Element getXmlRoot() throws IOException, SAXException, ParserConfigurationException {
        NodeList wlappList = getDocument().getElementsByTagName(TAG_WEBLOGIC_APPLICATION);
        Validate.isTrue(wlappList.getLength() == 1, "Validate one weblogic-application element");
        return (Element) wlappList.item(0);
    }


    @Nonnull
    public Document getDocument() throws ParserConfigurationException, IOException, SAXException {
        if (document == null) {
            DocumentBuilder documentFactory = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            if (weblogicappXml.exists()) {
                log.info("Found existing weblogic-application.xml, modifying in-place");
                document = documentFactory.parse(new TFileInputStream(weblogicappXml));
            } else {
                log.info("No existing weblogic-application.xml -- Generating");
                document = documentFactory.newDocument();
                Element wlapp = document.createElement(TAG_WEBLOGIC_APPLICATION);
                wlapp.setAttribute("xmlns", "http://xmlns.oracle.com/weblogic/weblogic-application");
                wlapp.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                wlapp.setAttribute("xsi:schemaLocation", "http://xmlns.oracle.com/weblogic/weblogic-application http://xmlns.oracle.com/weblogic/weblogic-application/1.4/weblogic-application.xsd");
                document.appendChild(wlapp);
            }
            document.normalizeDocument();
        }
        return document;
    }

    public void setupCommunalWeblogicApplicationXml(String sharedModule) throws IOException, SAXException, ParserConfigurationException {
        wcs = findClassloaderStructure();
        wcs = (wcs == null) ? createClassloaderStructure() : wcs;

        // remove the communal war from the classloader structure and make it a parent of the classloader structure
        wcs.removeModule(sharedModule);
        Element parent = wcs.makeParentClassloaderStructure(sharedModule);

        // get a list of the non-ejb modules without the shared module
        List<ApplicationModule> standardModuleList = earModule.getModules().entrySet().stream()
                .filter(moduleEntry -> !EjbModule.class.isAssignableFrom(moduleEntry.getValue().getClass()))
                .map(moduleEntry -> moduleEntry.getValue())
                .collect(Collectors.toList());
        standardModuleList.removeIf(applicationModule -> StringUtils.equalsIgnoreCase(sharedModule, applicationModule.getName()));


        // get a list of the ejb modules without the shared module
        List<ApplicationModule> ejbModuleList = earModule.getModules().entrySet().stream()
                .filter(moduleEntry -> EjbModule.class.isAssignableFrom(moduleEntry.getValue().getClass()))
                .map(moduleEntry -> moduleEntry.getValue())
                .collect(Collectors.toList());
        standardModuleList.removeIf(applicationModule -> StringUtils.equalsIgnoreCase(sharedModule, applicationModule.getName()));

        // pass the ejb module list to the classloaderstructure to append ones that are not present in the explicitly defined classloader structure
        // ejbs should be above webmodules in the classloader structure
        parent = wcs.appendEjbModulesNotPresent(parent, ejbModuleList);

        // pass the web module list to the classloaderstructure to append ones that are not present in the explicitly defined classloader structure
        wcs.appendModulesNotPresent(parent, standardModuleList);
    }
}
