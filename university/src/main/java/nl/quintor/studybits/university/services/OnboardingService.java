package nl.quintor.studybits.university.services;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import nl.quintor.studybits.indy.wrapper.dto.AnoncryptedMessage;
import nl.quintor.studybits.indy.wrapper.dto.ConnectionRequest;
import nl.quintor.studybits.university.entities.IndyConnection;
import nl.quintor.studybits.university.entities.User;
import nl.quintor.studybits.university.repositories.UserRepository;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class OnboardingService {

    private final UserRepository userRepository;
    private final UniversityService universityService;
    private final Mapper mapper;

    @SneakyThrows
    public ConnectionRequest onboardBegin(String universityName, String userName) {
        return universityService.createConnectionRequest(universityName, userName, null);
    }

    @SneakyThrows
    public Boolean onboardFinalize(String universityName, Long userId, AnoncryptedMessage anoncryptedMessage) {
        User user = userRepository.getOne(userId);
        Validate.isTrue(user.getConnection() == null, "Onboarding already completed!");
        String newcomerDid = universityService
                .acceptConnectionResponse(universityName, anoncryptedMessage);
        user.setConnection(new IndyConnection(null, newcomerDid));
        userRepository.saveAndFlush(user);
        return true;
    }
}