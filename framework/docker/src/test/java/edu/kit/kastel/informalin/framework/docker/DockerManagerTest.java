/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.docker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DockerManagerTest {
    private DockerManager dm;

    @Test
    void testCreation() throws Exception {
        dm = new DockerManager("tests");
        Assertions.assertTrue(dm.getContainerIds().isEmpty());
        var containerInformation = dm.createContainerByImage("httpd:2.4", true, true);
        Assertions.assertNotNull(containerInformation);
        Assertions.assertNotNull(containerInformation.containerId());
        Assertions.assertFalse(containerInformation.containerId().isBlank());
        Assertions.assertEquals(1, dm.getContainerIds().size());
        Assertions.assertEquals(dm.getContainerIds().get(0), containerInformation.containerId());

        // Verify that the service runs ..
        URL url = new URL("http://127.0.0.1:" + containerInformation.apiPort());
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        InputStream is = con.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
        String line;
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\n');
        }
        rd.close();
        var data = response.toString();
        Assertions.assertTrue(data.contains("It works!"));
    }

    @AfterEach
    void tearDown() {
        dm.shutdownAll();
        Assertions.assertTrue(dm.getContainerIds().isEmpty());
    }
}
