package nl.quintor.studybits.student.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaWalletModel {
    private Long id;
    private String name;
    private String mainDid;
    private String mainKey;
}
