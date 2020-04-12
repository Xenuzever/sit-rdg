package io.sitoolkit.rdg.core.domain.generator.config;

import io.sitoolkit.rdg.core.infrastructure.JsonUtils;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneratorConfigReader {

  public static final String CONFIG_FILE_NAME = "generator-config.json";

  public GeneratorConfig read(Path inDir) {
    return readFile(inDir.resolve(CONFIG_FILE_NAME));
  }

  public GeneratorConfig readFile(Path file) {
    if (file.toFile().exists()) {
      return JsonUtils.json2object(file, GeneratorConfig.class);
    }
    return new GeneratorConfig();
  }
}
