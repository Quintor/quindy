package nl.quintor.studybits;

import org.junit.Test;

public class ConnectIT extends BaseIT {
    @Test
    public void testConnectWithUniversity() {
        String UNIVERSITY_NAME = "rug";
        String CONNECTING_UNI_NAME = "gent";
        String STUDENT_NAME = "johan";

        setupStudentUniversityTriangle(STUDENT_NAME, UNIVERSITY_NAME, CONNECTING_UNI_NAME);
    }
}
