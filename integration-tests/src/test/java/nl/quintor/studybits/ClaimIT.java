package nl.quintor.studybits;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class ClaimIT extends BaseIT {
    @Test
    public void testGetNewClaims() {
        String UNIVERSITY_NAME = "rug";
        String STUDENT_NAME = "student3";

        registerUniversity(UNIVERSITY_NAME);
        registerStudent(STUDENT_NAME, UNIVERSITY_NAME);
        onboardStudent(STUDENT_NAME, UNIVERSITY_NAME);

        assertNoClaims(STUDENT_NAME);
        getNewClaims(STUDENT_NAME);

        givenCorrectHeaders(STUDENT_URL)
                .get("/student/{studentUserName}/claims", "student3")
                .then()
                .assertThat().statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    public void createNewClaim() {
        String UNIVERSITY_NAME = "rug";
        String STUDENT_NAME = "student1";

        registerUniversity(UNIVERSITY_NAME);
        registerStudent(STUDENT_NAME, UNIVERSITY_NAME);
        onboardStudent(STUDENT_NAME, UNIVERSITY_NAME);

        TranscriptModel transcriptModel = new TranscriptModel("Master of Disaster", "Awesome", "2017/18", "9.5");

        givenCorrectHeaders(UNIVERSITY_URL)
                .pathParam("universityName", UNIVERSITY_NAME)
                .pathParam("userName", "admin1")
                .pathParam("studentUserName", STUDENT_NAME)
                .body(transcriptModel)
                .post("/{universityName}/admin/{userName}/transcripts/{studentUserName}")
                .then()
                .assertThat().statusCode(200);

        getNewClaims(STUDENT_NAME);
        givenCorrectHeaders(STUDENT_URL)
                .pathParam("studentUserName", STUDENT_NAME)
                .pathParam("schemaName", "Transcript")
                .get("/student/{studentUserName}/claims/schema/{schemaName}")
                .then()
                .body("size()", is(1));
    }
}
