package nl.quintor.studybits.university.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.quintor.studybits.indy.wrapper.dto.SchemaKey;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimIssuerSchema {

    private String claimIssuerName;

    private String claimIssuerDid;

    private String schemaId;
}
