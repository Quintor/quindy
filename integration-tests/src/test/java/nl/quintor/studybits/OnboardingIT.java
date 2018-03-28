package nl.quintor.studybits;

import org.junit.Test;

public class OnboardingIT extends BaseIT {
    @Test
    public void testOnboardStudent() {
        Integer universityId = registerUniversity("Rug");
        Integer studentId = registerStudent("student1", "Rug");

        onboardStudent(studentId, universityId);
    }
}
