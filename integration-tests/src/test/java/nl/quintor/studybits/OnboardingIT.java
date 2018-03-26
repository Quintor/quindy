package nl.quintor.studybits;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;

public class OnboardingIT {
    private static final String STUDENT = "http://localhost:8095";
    private static final String UNIVERSITY = "http://localhost:8090";

    protected static RequestSpecification givenCorrectHeaders( String endpoint ) {
        return given()
                .baseUri(endpoint)
                .header("Content-type", "application/json");
    }

    @Before
    public void setUp() {

    }

    @Test
    public void testOnboardStudent() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        givenCorrectHeaders(STUDENT)
                .delete("/test/nuke")
                .then()
                .assertThat().statusCode(200);

        Integer universityId = givenCorrectHeaders(STUDENT)
                .queryParam("name", "Rug")
                .queryParam("endpoint", UNIVERSITY)
                .post("/university/register")
                .then()
                .assertThat().statusCode(200)
                .extract()
                .path("id");

        Integer studentId = givenCorrectHeaders(STUDENT)
                .queryParam("username", "student1")
                .queryParam("university", "Rug")
                .post("/student/register")
                .then()
                .assertThat().statusCode(200)
                .extract()
                .path("id");

        givenCorrectHeaders(STUDENT)
                .queryParam("student", studentId)
                .queryParam("university", universityId)
                .post("/student/onboard")
                .then()
                .assertThat().statusCode(200);
    }
}
