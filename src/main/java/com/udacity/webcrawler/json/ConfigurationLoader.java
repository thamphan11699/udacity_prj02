package com.udacity.webcrawler.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Objects;
import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/** A static utility class that loads a JSON configuration file. */
public final class ConfigurationLoader {

  private final Path path;

  /** Create a {@link ConfigurationLoader} that loads configuration from the given {@link Path}. */
  public ConfigurationLoader(Path path) {
    this.path = Objects.requireNonNull(path);
  }

  /**
   * Loads configuration from this {@link ConfigurationLoader}'s path
   *
   * @return the loaded {@link CrawlerConfiguration}.
   */
  public CrawlerConfiguration load() {
    try (Reader reader = new FileReader(this.path.toFile())) {
      return read(reader);
    } catch (Exception e) {
      throw new RuntimeException(e.getCause());
    }
  }

  /**
   * Loads crawler configuration from the given reader.
   *
   * @param reader a Reader pointing to a JSON string that contains crawler configuration.
   * @return a crawler configuration
   */
  public static CrawlerConfiguration read(Reader reader) {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(reader);
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);
      mapper.disable(AUTO_CLOSE_SOURCE);
      mapper.setVisibility(
          VisibilityChecker.Std.defaultInstance()
              .withFieldVisibility(JsonAutoDetect.Visibility.ANY));
      return mapper.readValue(reader, CrawlerConfiguration.class);
    } catch (IOException e) {
      return new CrawlerConfiguration.Builder().build();
    }
  }
}
