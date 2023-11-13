package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

    private final Clock clock;
    private final ProfilingState state = new ProfilingState();
    private final ZonedDateTime startTime;

    @Inject
    ProfilerImpl(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
        this.startTime = ZonedDateTime.now(clock);
    }

    private Boolean profiledClass(Class<?> klass) {
        List<Method> methods = new ArrayList<>(Arrays.asList(klass.getDeclaredMethods()));
        if (methods.isEmpty()) {
            return false;
        }
        return methods.stream().anyMatch(m -> m.getAnnotation(Profiled.class) != null);
    }

    @Override
    public <T> T wrap(Class<T> klass, T delegate) {
        Objects.requireNonNull(klass);
        Objects.requireNonNull(delegate);

        if (!profiledClass(klass)) {
            throw new IllegalArgumentException(klass.getName());
        }

        Object proxy = Proxy.newProxyInstance(
                klass.getClassLoader(),
                new Class<?>[]{klass},
                new ProfilingMethodInterceptor(clock, state, delegate));
        return (T) proxy;

    }

    @Override
    public void writeData(Path path) throws IOException {

        try (Writer bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writeData(bufferedWriter);
        }
    }

    @Override
    public void writeData(Writer writer) throws IOException {
        writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
        writer.write(System.lineSeparator());
        state.write(writer);
        writer.write(System.lineSeparator());
    }
}
