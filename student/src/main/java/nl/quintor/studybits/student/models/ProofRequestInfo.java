package nl.quintor.studybits.student.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.ResourceSupport;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ProofRequestInfo extends ResourceSupport implements Comparable<ProofRequestInfo> {

    private Long proofId;

    private String name;

    private String version;

    @Override
    public int compareTo(ProofRequestInfo proofRequestInfo) {
        int result = this.name.compareTo(proofRequestInfo.name);

        if (result == 0) {
            result = this.version.compareTo(proofRequestInfo.version);
        }

        return result;
    }
}
