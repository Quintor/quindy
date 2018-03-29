package nl.quintor.studybits;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class BaseIT {
    static final String STUDENT = "http://localhost:8095";
    static final String UNIVERSITY = "http://localhost:8090";

    static RequestSpecification givenCorrectHeaders(String endpoint) {
        return given()
                .baseUri(endpoint)
                .header("Content-type", "application/json");
    }

    @Before
    public void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        givenCorrectHeaders(STUDENT)
                .delete("/test/nuke")
                .then()
                .assertThat().statusCode(200);
        givenCorrectHeaders(UNIVERSITY)
                .delete("/test/nuke")
                .then()
                .assertThat().statusCode(200);
    }

    Integer registerUniversity(String name) {
        return givenCorrectHeaders(STUDENT)
                .queryParam("name", name)
                .queryParam("endpoint", UNIVERSITY)
                .post("/university/register")
                .then()
                .assertThat().statusCode(200)
                .extract()
                .path("id");
    }

    Integer registerStudent(String username, String uniName) {
        return givenCorrectHeaders(STUDENT)
                .queryParam("username", username)
                .queryParam("university", uniName)
                .post("/student/register")
                .then()
                .assertThat().statusCode(200)
                .extract()
                .path("id");
    }

    void onboardStudent(Integer studentId, Integer universityId) {
        givenCorrectHeaders(STUDENT)
                .queryParam("student", studentId)
                .queryParam("university", universityId)
                .post("/student/onboard")
                .then()
                .assertThat().statusCode(200);
    }

    void getNewClaims(Integer studentId) {
        givenCorrectHeaders(STUDENT)
                .get("/student/{studentId}/claims/new", studentId)
                .then()
                .assertThat().statusCode(200);
    }

    void assertNumberOfClaimsEquals(Integer expectedNumber, Integer studentId) {
        givenCorrectHeaders(STUDENT)
                .get("/student/{studentId}/claims", studentId)
                .then()
                .assertThat().statusCode(200)
                .body("size()", is(expectedNumber));
    }

    void assertNoClaims(Integer studentId) {
        assertNumberOfClaimsEquals(0, studentId);
    }
}
