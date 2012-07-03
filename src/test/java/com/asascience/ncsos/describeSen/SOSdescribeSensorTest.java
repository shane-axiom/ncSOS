/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asascience.ncsos.describeSen;

import com.asascience.ncsos.outputformatter.SOSOutputFormatter;
import com.asascience.ncsos.service.SOSParser;
import com.asascience.ncsos.util.XMLDomUtils;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author SCowan
 */
public class SOSdescribeSensorTest {
    private static String outputDir = null;
    private static String baseLocalDir = null;
    
    private static final String bsd_1_set = "resources/datasets/sura/watlev_NOAA_NAVD_PRE.nc";
    private static final String bsd_1_query = "procedure=urn:tds:station.sos:NOAA_8724698";
    private static final String bsd_2_set = "resources/datasets/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1/timeSeriesProfile-Multidimensional-MultipeStations-H.5.1.nc";
    private static final String bsd_2_query = "procedure=urn:tds:station.sos:Station1";
    
    private static String baseQuery = "request=DescribeSensor&service=sos&version=1.0.0&responseformat=";
    
    @BeforeClass
    public static void SetupEnviron() throws FileNotFoundException, UnsupportedEncodingException {
        // not really a test, just used to set up the various string values
        if (outputDir != null && baseLocalDir != null) {
            // exit early if the environ is already set
            return;
        }
        String container = "describeSensor";
        InputStream templateInputStream = null;
        try {
            File configFile = new File("resources/tests_config.xml");
            templateInputStream = new FileInputStream(configFile);
            Document configDoc = XMLDomUtils.getTemplateDom(templateInputStream);
            // read from the config file
            outputDir = XMLDomUtils.getNodeValue(configDoc, container, "outputDir");
            baseLocalDir = XMLDomUtils.getNodeValue(configDoc, container, "projectDir");
        } finally {
            if (templateInputStream != null) {
                try {
                    templateInputStream.close();
                } catch (IOException e) {
                    // ignore, closing..
                }
            }
        }

        File file = new File(outputDir);
        file.mkdirs();
        
        baseQuery += URLEncoder.encode("text/xml;subtype=\"sensorML/1.0.1\"", "UTF-8") + "&";
    }
     
    private void writeOutput(HashMap<String, Object> outMap, Writer write) {
        SOSOutputFormatter output = (SOSOutputFormatter)outMap.get("outputHandler");
        assertNotNull("got null output", output);
        output.writeOutput(write);
    }
    
    private static void fileWriter(String base, String fileName, Writer write) throws IOException {
        File file = new File(base + fileName);
        Writer output = new BufferedWriter(new FileWriter(file));
        output.write(write.toString());
        output.close();
        System.out.println("Your file has been written");
    }
    
    @Test
    public void testBasicDescribeSensorStation() throws IOException {
        NetcdfDataset cdfDataset = NetcdfDataset.openDataset(baseLocalDir + bsd_1_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhance(cdfDataset, baseQuery + bsd_1_query, bsd_1_set), writer);
        fileWriter(outputDir, "NOAA_8724698.xml", writer);
        // test for expected values below
        assertFalse("exception in output", writer.toString().contains("Exception"));
        assertTrue("missing component", writer.toString().contains("<component name=\"Sensor watlev\">"));
        assertTrue("station id not as expected", writer.toString().contains("<value>urn:tds:station.sos:NOAA_8724698</value>"));
    }
    
    @Test
    public void testBasicDescribeSensorStation2() throws IOException {
        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bsd_2_set);
        SOSParser parser = new SOSParser();
        Writer writer = new CharArrayWriter();
        writeOutput(parser.enhance(dataset, baseQuery + bsd_2_query, bsd_2_set), writer);
        fileWriter(outputDir, "timeSeriesProfile-Multidimensional-MultipleStations-H.5.1.xml", writer);
        assertFalse("exception in output", writer.toString().contains("Exception"));
        assertTrue("missing component", writer.toString().contains("<component name=\"Sensor temperature\">"));
        assertTrue("missing/invalid coords", writer.toString().contains("<gml:coordinates>37.5 -76.5</gml:coordinates>"));
    }
    
//    @Test
//    public void testBasicDescribeSensorStation3() throws IOException {
//        NetcdfDataset dataset = NetcdfDataset.openDataset(baseLocalDir + bsd_2_set);
//        SOSParser parser = new SOSParser();
//        Writer writer = new CharArrayWriter();
//        writeOutput(parser.enhance(dataset, baseQuery + bsd_2_query, bsd_2_set), writer);
//        fileWriter(outputDir, "timeSeriesProfile-Multidimensional-MultipleStations-H.5.1.xml", writer);
//        
//        assertFalse("exception in output", writer.toString().contains("Exception"));
//    }
}
