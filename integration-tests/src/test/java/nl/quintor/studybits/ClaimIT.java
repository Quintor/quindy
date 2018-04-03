package nl.quintor.studybits;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class ClaimIT extends BaseIT {
    @Test
    public void testGetNewClaims() {
        Integer universityId = registerUniversity("rug");
        Integer studentId = registerStudent("student3", "rug");
        onboardStudent(studentId, universityId);

        assertNoClaims(studentId);
        getNewClaims(studentId);

        givenCorrectHeaders(STUDENT)
                .get("/student/{studentId}/claims", studentId)
                .then()
                .assertThat().statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    public void createNewClaim() {
        Integer universityId = registerUniversity("rug");
        Integer studentId = registerStudent("student1", "rug");
        onboardStudent(studentId, universityId);

        TranscriptModel transcriptModel = new TranscriptModel("Master of Disaster", "Awesome", "2017/18", "9.5");

        givenCorrectHeaders(UNIVERSITY)
                .pathParam("universityName", "rug")
                .pathParam("userName", "admin1")
                .pathParam("studentUserName", "student1")
                .body(transcriptModel)
                .post("/{universityName}/admin/{userName}/transcripts/{studentUserName}")
                .then()
                .assertThat().statusCode(200);

        getNewClaims(studentId);
        givenCorrectHeaders(STUDENT)
                .pathParam("studentId", studentId)
                .pathParam("schemaName", "Transcript")
                .get("/student/{studentId}/claims/schema/{schemaName}")
                .then()
                .body("size()", is(1));
    }
}
