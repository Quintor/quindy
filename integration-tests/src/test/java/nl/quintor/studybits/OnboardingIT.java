package nl.quintor.studybits;

import org.junit.Test;

public class OnboardingIT extends BaseIT {
    @Test
    public void testOnboardStudent() {
        Integer universityId = registerUniversity("Rug");
        Integer studentId = registerStudent("student1", "Rug");

        givenCorrectHeaders(STUDENT)
                .queryParam("student", studentId)
                .queryParam("university", universityId)
                .post("/student/onboard")
                .then()
                .assertThat().statusCode(200);
    }
}
