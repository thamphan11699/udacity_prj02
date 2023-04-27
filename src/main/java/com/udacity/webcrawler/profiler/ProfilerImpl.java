package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.FileWriter;import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/** Concrete implementation of the {@link Profiler}. */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);
    List<Method> methods = List.of(klass.getDeclaredMethods());
    if (methods.isEmpty() || !isProfileClass(methods)) {
      throw new IllegalArgumentException(klass.getName() + " not contain a @Profiled method.");
    }

    ProfilingMethodInterceptor interceptor = new ProfilingMethodInterceptor(clock, delegate, state);

    return (T)
        Proxy.newProxyInstance(
            ProfilerImpl.class.getClassLoader(), new Class[] {klass}, interceptor);
  }

  private boolean isProfileClass(List<Method> methods) {
    return methods.stream()
        .anyMatch(method -> Objects.nonNull(method.getAnnotation(Profiled.class)));
  }

  @Override
  public void writeData(Path path) {
    try{
      Writer writer = new FileWriter(path.toFile().getName());
      writeData(writer);
      writer.flush();
    } catch (Exception e) {
      throw new RuntimeException(e.getCause());
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
