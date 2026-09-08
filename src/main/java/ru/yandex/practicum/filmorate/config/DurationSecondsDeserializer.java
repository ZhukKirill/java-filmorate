package ru.yandex.practicum.filmorate.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Duration;

public class DurationSecondsDeserializer extends JsonDeserializer<Duration> {
    @Override
    public Duration deserialize(JsonParser p, DeserializationContext ct)
            throws IOException {
        return Duration.ofSeconds(p.getLongValue());
    }
}