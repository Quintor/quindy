package nl.quintor.studybits.student.model;

import lombok.Data;

@Data
public class ClaimRecordModel {
    private Long id;
    private String ownerUserName;
    private String claimHashId;
}
