package org.jacpfx.entity.encoder;

import com.google.gson.Gson;
import org.jacpfx.entity.Payload;
import org.jacpfx.common.encoder.Encoder;

/**
 * Created by Andy Moncsek on 25.11.15.
 */
public class ExampleStringEncoder implements Encoder.StringEncoder<Payload<String>> {
    @Override
    public String encode(Payload<String> input) {
        Gson gg = new Gson();
        return gg.toJson(input);
    }
}
