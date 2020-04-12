package io.sitoolkit.rdg.core.application;

import java.nio.file.Path;
import java.util.List;

public interface DataGenerator {

  public List<Path> generate(Path input, List<Path> output);
}
