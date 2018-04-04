package nl.quintor.studybits.university.controllers.admin;

import lombok.AllArgsConstructor;
import nl.quintor.studybits.university.UserContext;
import nl.quintor.studybits.university.dto.Transcript;
import nl.quintor.studybits.university.entities.TranscriptRecord;
import nl.quintor.studybits.university.models.TranscriptModel;
import nl.quintor.studybits.university.services.TranscriptService;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{universityName}/admin/{userName}/transcripts")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class TranscriptController {

    private final UserContext userContext;
    private final TranscriptService transcriptService;
    private final Mapper mapper;

    private TranscriptModel toModel(Transcript transcript) {
        return mapper.map(transcript, TranscriptModel.class);
    }

    @PostMapping("/{studentUserName}")
    public void addTranscript(@PathVariable String studentUserName, @RequestBody TranscriptModel transcriptModel) {
        transcriptService.addTranscript(userContext.currentUniversityName(), studentUserName, transcriptModel);
    }

    @GetMapping
    List<TranscriptRecord> findAllByUniversity() {
        return transcriptService.findAllByUniversity(userContext.currentUniversityName());
    }
}