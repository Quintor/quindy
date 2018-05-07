package nl.quintor.studybits.university.controllers.student;

import nl.quintor.studybits.indy.wrapper.dto.AuthcryptedMessage;
import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.dto.ProofRequestInfoDto;
import nl.quintor.studybits.university.helpers.LinkHelper;
import nl.quintor.studybits.university.models.AuthEncryptedMessageModel;
import nl.quintor.studybits.university.models.ProofRequestInfo;
import nl.quintor.studybits.university.services.ProofHandler;
import org.apache.commons.lang3.Validate;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/{universityName}/student/{userName}/proofrequests")
public class ProofRequestController {

    private final UserContext userContext;
    private final LinkHelper linkHelper;
    private final Map<String, ProofHandler> proofHandlerMap;
    private final Mapper mapper;

    private AuthEncryptedMessageModel toModel(AuthcryptedMessage authcryptedMessage) {
        return mapper.map(authcryptedMessage, AuthEncryptedMessageModel.class);
    }

    private AuthcryptedMessage toDto(AuthEncryptedMessageModel authEncryptedMessageModel) {
        return mapper.map(authEncryptedMessageModel, AuthcryptedMessage.class);
    }

    public ProofHandler getHandler(String proofName) {
        Validate.notNull(proofName, "Proof name cannot be null.");
        return Validate.notNull(proofHandlerMap.get(proofName.toLowerCase()), String.format("Unknown proof: %s", proofName));
    }

    @Autowired
    public ProofRequestController(UserContext userContext, LinkHelper linkHelper, ProofHandler[] proofHandlers, Mapper mapper) {
        this.userContext = userContext;
        this.linkHelper = linkHelper;
        this.proofHandlerMap = Arrays.stream(proofHandlers)
                .collect(Collectors.toMap(x -> x.getProofName().toLowerCase(), x -> x));
        this.mapper = mapper;
    }

    private ProofRequestInfo toProofRequestInfo(ProofRequestInfoDto dto) {
        ProofRequestInfo proofRequestInfo = mapper.map(dto, ProofRequestInfo.class);
        return linkHelper.withLink(proofRequestInfo, ProofRequestController.class,
                c -> c.getProofRequest(proofRequestInfo.getName(), proofRequestInfo.getProofId()));
    }

    @SuppressWarnings("unchecked")
    private List<ProofRequestInfoDto> findProofRequests(ProofHandler proofHandler) {
        return proofHandler.findProofRequests(userContext.currentUserId());
    }

    @GetMapping
    List<ProofRequestInfo> findAllProofRequests() {
        return proofHandlerMap
                .values()
                .stream()
                .flatMap(proofHandler -> findProofRequests(proofHandler).stream())
                .map(this::toProofRequestInfo)
                .collect(Collectors.toList());
    }

    @GetMapping("/{proofName}/{proofId}")
    AuthEncryptedMessageModel getProofRequest(@PathVariable String proofName, @PathVariable Long proofId) {
        ProofHandler handler = getHandler(proofName);
        AuthcryptedMessage result = handler.getProofRequestMessage(userContext.currentUserId(), proofId);
        return linkHelper.withLink(toModel(result), ProofRequestController.class, x -> x.handleProof(proofName, proofId, null));
    }

    @PostMapping("/{proofName}/{proofId}")
    Object handleProof(@PathVariable String proofName, @PathVariable Long proofId, @RequestBody AuthEncryptedMessageModel authEncryptedMessageModel) {
        ProofHandler handler = getHandler(proofName);
        return handler.handleProof(userContext.currentUserId(), proofId, toDto(authEncryptedMessageModel));
    }
}