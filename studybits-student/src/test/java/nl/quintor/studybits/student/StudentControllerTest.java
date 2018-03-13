package nl.quintor.studybits.student;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.quintor.studybits.student.controller.StudentController;
import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.services.StudentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static nl.quintor.studybits.student.RandomDataGenerator.randLong;
import static nl.quintor.studybits.student.RandomDataGenerator.randString;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
public class StudentControllerTest {
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @InjectMocks
    StudentController studentController;

    @MockBean
    private StudentService studentService;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void Given_StudentNotRegistered_When_StudentIsRegistered_Then_RegisteringWillSucceed() throws Exception {
        // Arrange
        Student testObject = new Student(randLong(), randString(), null, null);
        when(studentService.createAndSave(anyString(), anyString())).thenReturn(testObject);
        // Act
        MvcResult response = mockMvc.perform(
                post("/student/register")
                        .param("username", randString())
                        .param("university", randString()))
                .andExpect(status().isOk())
                .andReturn();

        ObjectMapper mapper = new ObjectMapper();
        Student result = mapper.readValue(response.getResponse().getContentAsString(), Student.class);
        assertEquals(testObject, result);
    }

}
