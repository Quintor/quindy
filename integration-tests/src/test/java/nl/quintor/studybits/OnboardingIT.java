package nl.quintor.studybits;

import org.junit.Test;

public class OnboardingIT extends BaseIT {
    @Test
    public void testOnboardStudent() {
        String UNIVERSITY_NAME = "Rug";
        String STUDENT_NAME = "student1";

        registerUniversity(UNIVERSITY_NAME);
        registerStudent(STUDENT_NAME, UNIVERSITY_NAME);

        onboardStudent(STUDENT_NAME, UNIVERSITY_NAME);
    }
}
