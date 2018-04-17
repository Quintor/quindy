package nl.quintor.studybits;

import org.junit.Test;

public class ProofRequestIT extends BaseIT {
    @Test
    public void testConnectWithUniversity() {
        String UNIVERSITY_NAME = "rug";
        String CONNECTING_UNI_NAME = "gent";
        String STUDENT_NAME = "lisa";

        registerUniversity(UNIVERSITY_NAME);
        registerUniversity(CONNECTING_UNI_NAME);
        registerStudent(STUDENT_NAME, UNIVERSITY_NAME);
        getNewClaims(STUDENT_NAME);
        connectStudent(STUDENT_NAME, CONNECTING_UNI_NAME);

        getNewProofRequests(STUDENT_NAME);
        assertNumberOfProofRequestsEquals(1, STUDENT_NAME);
    }
}
