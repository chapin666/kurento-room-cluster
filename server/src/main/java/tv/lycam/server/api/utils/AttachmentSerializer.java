package tv.lycam.server.api.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import tv.lycam.server.api.module.attachment.AttachmentType;

import java.io.IOException;

/**
 * Created by lycamandroid on 2017/6/26.
 */
public class AttachmentSerializer extends JsonSerializer<AttachmentType> {
    @Override
    public void serialize(AttachmentType value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        jgen.writeString(value.getValue());
    }

    public Class<AttachmentType> handledType() { return AttachmentType.class; }
}

