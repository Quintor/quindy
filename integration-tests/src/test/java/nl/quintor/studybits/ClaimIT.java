package nl.quintor.studybits;

import org.junit.Test;

public class ClaimIT extends BaseIT {
    @Test
    public void testFetchClaim() {
        Integer universityId = registerUniversity("Rug");
        Integer studentId = registerStudent("student3", "Rug");

        givenCorrectHeaders(STUDENT)
                .get("/student/{studentId}/claims/fetch", studentId)
                .then()
                .assertThat().statusCode(200);
    }
}
