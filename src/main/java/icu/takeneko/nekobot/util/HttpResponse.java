package icu.takeneko.nekobot.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public record HttpResponse(
    int statusCode,
    byte[] body
) {
    public Reader reader(){
        return new InputStreamReader(new ByteArrayInputStream(body));
    }

    public InputStream inputStream(){
        return new ByteArrayInputStream(body);
    }
}
