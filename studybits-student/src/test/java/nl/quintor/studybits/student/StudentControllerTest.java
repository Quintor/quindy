package nl.quintor.studybits.student;

import nl.quintor.studybits.student.model.Student;
import nl.quintor.studybits.student.model.University;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
public class StudentControllerTest {
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    private final Integer randomStringLength = 10;

    @Before
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

    }

    @Test
    public void Given_StudentNotRegistered_When_StudentIsRegistered_Then_RegisteringWillSucceed() throws Exception {
        // Arrange
        University randomUniversity = new University(RandomStringUtils.randomAlphabetic(randomStringLength), RandomStringUtils.randomAlphabetic(randomStringLength));
        Student randomStudent = new Student(RandomStringUtils.randomAlphabetic(randomStringLength), randomUniversity);

        // Act
        mockMvc.perform(
                post("/student/register")
                        .requestAttr("username", randomStudent.getUsername())
                        .requestAttr("university", randomUniversity.getName()))

                // Assert
                .andExpect(status().isOk());

    }

}
