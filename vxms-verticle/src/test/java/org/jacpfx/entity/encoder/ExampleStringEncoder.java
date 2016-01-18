package org.jacpfx.entity.encoder;

import com.google.gson.Gson;
import org.jacpfx.entity.Payload;
import org.jacpfx.vertx.websocket.encoder.Encoder;

/**
 * Created by Andy Moncsek on 25.11.15.
 */
public class ExampleStringEncoder implements Encoder.StringEncoder<Payload<String>> {
    // TODO create a "more reactive" interface
    @Override
    public String encode(Payload<String> input) {
        Gson gg = new Gson();
        return gg.toJson(input);
    }
}
