package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;

import java.io.IOException;

@Getter
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ListPairwiseResult implements Serializable {
    @JsonProperty("my_did")
    private String myDid;
    @JsonProperty("their_did")
    private String theirDid;
    private String metadata;

    public PairwiseMetadata getParsedMetadata() throws IOException {
        return JSONUtil.mapper.readValue(metadata, PairwiseMetadata.class);
    }
}
