package tv.lycam.server.api.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import tv.lycam.server.api.module.attachment.AttachmentModel;
import tv.lycam.server.api.module.attachment.AttachmentType;

import java.io.IOException;

/**
 * Created by lycamandroid on 2017/6/26.
 */
public class AttachmentDeserializer extends JsonDeserializer<AttachmentType> {

    @Override
    public AttachmentType deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        // You can add here the check whether the field is empty/null
        return AttachmentType.getValueByCode(jp.getIntValue());
    }
}
