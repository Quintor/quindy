package nl.quintor.studybits;

import org.junit.Test;

public class ClaimIT extends BaseIT {
    @Test
    public void testGetClaim() {
        Integer universityId = registerUniversity("Rug");
        Integer studentId = registerStudent("student3", "Rug");
        onboardStudent(studentId, universityId);

//        givenCorrectHeaders(STUDENT)
//                .get("/student/{studentId}/claims/new", studentId)
//                .then()
//                .assertThat().statusCode(200);
//
//        String out = givenCorrectHeaders(STUDENT)
//                .get("/student/{studentId}/claims")
//                .then()
//                .assertThat().statusCode(200)
//                .extract()
//                .body().as(String.class);
//
//        System.out.println(out);
    }
}
