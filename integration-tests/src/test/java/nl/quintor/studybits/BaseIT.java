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

    void registerUniversity(String name) {
        givenCorrectHeaders(STUDENT_URL)
                .queryParam("name", name)
                .queryParam("endpoint", UNIVERSITY_URL)
                .post("/university/register")
                .then()
                .assertThat().statusCode(200)
                .extract()
                .path("id");
    }

    void registerStudent(String username, String uniName) {
        givenCorrectHeaders(STUDENT_URL)
                .queryParam("studentUserName", username)
                .queryParam("universityName", uniName)
                .post("/student/register")
                .then()
                .assertThat().statusCode(200)
                .extract()
                .path("id");
    }

    void connectStudent(String studentUserName, String universityName) {
        givenCorrectHeaders(STUDENT_URL)
                .queryParam("studentUserName", studentUserName)
                .queryParam("universityName", universityName)
                .post("/student/connect")
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

    void getNewProofRequests(String studentUserName) {
        givenCorrectHeaders(STUDENT_URL)
                .pathParam("studentUserName", studentUserName)
                .get("/student/{studentUserName}/proof-requests/new")
                .then()
                .assertThat().statusCode(200);
    }

    void assertNumberOfProofRequestsEquals(Integer expectedNumber, String studentUserName) {
        givenCorrectHeaders(STUDENT_URL)
                .pathParam("studentUserName", studentUserName)
                .get("/student/{studentUserName}/proof-requests")
                .then()
                .assertThat().statusCode(200)
                .body("size()", is(expectedNumber));
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
