package io.sitoolkit.rdg.core.domain.generator.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.sitoolkit.rdg.core.domain.schema.ColumnDef;

@JsonIgnoreProperties("type")
public interface ValueGenerator {

  // TODO column is unneccesary
  String generate(ColumnDef column);

  void initialize();
}
