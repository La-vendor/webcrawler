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

    // TODO: You will need to add more instance fields and constructor arguments to this class.


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Instant start = null;


        if(method.isAnnotationPresent(Profiled.class)){

            try{
                start = clock.instant();
                 return method.invoke(obj, args);
            }catch (InvocationTargetException e) {
                throw e.getTargetException();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            finally{

                Duration duration = Duration.between(start, clock.instant());
                log.record(obj.getClass(), method, duration);
            }

        }else{

            return method.invoke(obj, args);
        }
        // TODO: This method interceptor should inspect the called method to see if it is a profiled
        //       method. For profiled methods, the interceptor should record the start time, then
        //       invoke the method using the object that is being profiled. Finally, for profiled
        //       methods, the interceptor should record how long the method call took, using the
        //       ProfilingState methods.

    }
}
