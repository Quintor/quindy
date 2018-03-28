package nl.quintor.studybits;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class ClaimIT extends BaseIT {
    @Test
    public void testGetNewClaims() {
        Integer universityId = registerUniversity("Rug");
        Integer studentId = registerStudent("student3", "Rug");
        onboardStudent(studentId, universityId);

        givenCorrectHeaders(STUDENT)
                .get("/student/{studentId}/claims", studentId)
                .then()
                .assertThat().statusCode(200)
                .body("size()", is(0));

        givenCorrectHeaders(STUDENT)
                .get("/student/{studentId}/claims/new", studentId)
                .then()
                .assertThat().statusCode(200);

        givenCorrectHeaders(STUDENT)
                .get("/student/{studentId}/claims", studentId)
                .then()
                .assertThat().statusCode(200)
                .body("size()", greaterThan(0));
    }
}
