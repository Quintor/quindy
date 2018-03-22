package nl.quintor.studybits.student.integration;

import nl.quintor.studybits.student.model.University;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static nl.quintor.studybits.student.RandomDataGenerator.randString;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UniversityControllerIntegrationTest extends BaseIntegrationTest {

    private String baseURL = "/university/";

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void University_FromFrontendToBackend_IntegrationTest() throws Exception {
        University testUni = new University(null, randString(), randString());

        // Create a new University
        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("name", testUni.getName());
        payload.add("endpoint", testUni.getEndpoint());
        University resultCreate = restTemplate.postForEntity(baseURL + "register", payload, University.class)
                                              .getBody();

        testUni.setId(resultCreate.getId());
        assertEquals(testUni, resultCreate);

        // Find the University by Id
        University resultFindById = restTemplate.getForEntity(baseURL + testUni.getId(), University.class)
                                                .getBody();
        assertEquals(resultFindById, testUni);

        // Find all Universities and check that created University is returned
        List<University> resultFindAll = restTemplate.exchange(baseURL, HttpMethod.GET, null, new ParameterizedTypeReference<List<University>>() {})
                                                     .getBody();
        assertFalse(resultFindAll.isEmpty());
        assertEquals(1, resultFindAll.size());
        assertEquals(testUni, resultFindAll.get(0));

        // Update University and check that update was persisted
        testUni.setName(randString());
        testUni.setEndpoint(randString());
        restTemplate.put(baseURL, testUni);

        University resultUpdateById = restTemplate.getForEntity(baseURL + testUni.getId(), University.class)
                                                  .getBody();
        assertEquals(testUni, resultUpdateById);

        // Delete the University and check that change was persisted
        mockMvc.perform(delete(baseURL + testUni.getId()))
               .andExpect(status().isOk());


    }
}
