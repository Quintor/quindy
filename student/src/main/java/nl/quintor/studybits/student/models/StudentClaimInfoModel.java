package nl.quintor.studybits.student.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.ResourceSupport;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StudentClaimInfoModel extends ResourceSupport {
    private Long claimId;
    private String name;
    private String version;
    private String label;
}
