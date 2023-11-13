package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

    private final Clock clock;
    private final ProfilingState log;
    private final Object obj;


    public ProfilingMethodInterceptor(Clock clock, ProfilingState log, Object obj) {
        this.clock = clock;
        this.log = log;
        this.obj = obj;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Instant start = null;
        boolean profiled = method.isAnnotationPresent(Profiled.class);

        if (profiled) start = clock.instant();
        try {
            return method.invoke(obj, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (profiled) {
                Duration duration = Duration.between(start, clock.instant());
                log.record(obj.getClass(), method, duration);
            }

        }


    }
}
