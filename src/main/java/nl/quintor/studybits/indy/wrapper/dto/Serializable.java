package nl.quintor.studybits.indy.wrapper.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.quintor.studybits.indy.wrapper.util.JSONUtil;

interface Serializable {
    default String toJSON() throws JsonProcessingException {
        return JSONUtil.mapper.writeValueAsString(this);
    }
}
