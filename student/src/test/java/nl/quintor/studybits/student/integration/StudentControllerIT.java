package nl.quintor.studybits.student.integration;

import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.model.University;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static nl.quintor.studybits.student.RandomDataGenerator.randString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StudentControllerIT extends BaseIT {

    private String baseURL = "/student/";

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void Student_FromFrontendToBackend_IT() throws Exception {
        // Create a new University
        MultiValueMap<String, String> payloadUni = new LinkedMultiValueMap<>();
        payloadUni.add("name", randString());
        payloadUni.add("endpoint", randString());
        University testUni = restTemplate.postForEntity("/university/register", payloadUni, University.class)
                                         .getBody();

        Student testStudent = new Student(null, randString(), testUni, null);

        // Create a new Student
        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("username", testStudent.getUsername());
        payload.add("university", testStudent.getOriginUniversity()
                                             .getName());
        Student resultCreate = restTemplate.postForEntity(baseURL + "register", payload, Student.class)
                                           .getBody();

        testStudent.setId(resultCreate.getId());
        testStudent.setMetaWallet(resultCreate.getMetaWallet());
        assertEquals(testStudent, resultCreate);

        // Find the Student by Id
        Student resultFindById = restTemplate.getForEntity(baseURL + testStudent.getId(), Student.class)
                                             .getBody();
        assertEquals(resultFindById, testStudent);

        // Find all Students and check that created Student is returned
        List<Student> resultFindAll = restTemplate.exchange(baseURL, HttpMethod.GET, null, new ParameterizedTypeReference<List<Student>>() {})
                                                  .getBody();
        assertFalse(resultFindAll.isEmpty());
        assertEquals(1, resultFindAll.size());
        assertEquals(testStudent, resultFindAll.get(0));

        // Update Student and check that update was persisted
        testStudent.setUsername(randString());
        restTemplate.put(baseURL, testStudent);

        Student resultUpdateById = restTemplate.getForEntity(baseURL + testStudent.getId(), Student.class)
                                               .getBody();
        assertEquals(testStudent, resultUpdateById);

        // Delete the Student again
        mockMvc.perform(delete(baseURL + testStudent.getId()))
               .andExpect(status().isOk());
    }
}
