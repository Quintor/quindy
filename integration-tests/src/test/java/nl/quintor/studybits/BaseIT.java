package nl.quintor.studybits;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class BaseIT {
    static final String STUDENT_URL = "http://localhost:8095";
    static final String UNIVERSITY_URL = "http://localhost:8090";

    static RequestSpecification givenCorrectHeaders(String endpoint) {
        return given()
                .baseUri(endpoint)
                .header("Content-type", "application/json");
    }

    @Before
    public void setUp() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        givenCorrectHeaders(STUDENT_URL)
                .delete("/test/nuke")
                .then()
                .assertThat().statusCode(200);
        givenCorrectHeaders(UNIVERSITY_URL)
                .delete("/test/nuke")
                .then()
                .assertThat().statusCode(200);
    }

    Integer registerUniversity(String name) {
        return givenCorrectHeaders(STUDENT_URL)
                .queryParam("name", name)
                .queryParam("endpoint", UNIVERSITY_URL)
                .post("/university/register")
                .then()
                .assertThat().statusCode(200)
                .extract()
                .path("id");
    }

    Integer registerStudent(String username, String uniName) {
        return givenCorrectHeaders(STUDENT_URL)
                .queryParam("username", username)
                .queryParam("universityName", uniName)
                .post("/student/register")
                .then()
                .assertThat().statusCode(200)
                .extract()
                .path("id");
    }

    void onboardStudent(String studentUserName, String universityName) {
        givenCorrectHeaders(STUDENT_URL)
                .queryParam("student", studentUserName)
                .queryParam("university", universityName)
                .post("/student/onboard")
                .then()
                .assertThat().statusCode(200);
    }

    void getNewClaims(String studentUserName) {
        givenCorrectHeaders(STUDENT_URL)
                .pathParam("studentUserName", studentUserName)
                .get("/student/{studentUserName}/claims/new")
                .then()
                .assertThat().statusCode(200);
    }

    void assertNumberOfClaimsEquals(Integer expectedNumber, String studentUserName) {
        givenCorrectHeaders(STUDENT_URL)
                .pathParam("studentUserName", studentUserName)
                .get("/student/{studentUserName}/claims")
                .then()
                .assertThat().statusCode(200)
                .body("size()", is(expectedNumber));
    }

    void assertNoClaims(String studentUserName) {
        assertNumberOfClaimsEquals(0, studentUserName);
    }
}
