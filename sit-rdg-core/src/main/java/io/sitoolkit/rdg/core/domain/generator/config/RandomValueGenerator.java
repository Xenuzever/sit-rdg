package io.sitoolkit.rdg.core.domain.generator.config;

import io.sitoolkit.rdg.core.domain.generator.RandomValueUtils;
import io.sitoolkit.rdg.core.domain.schema.ColumnDef;

public class RandomValueGenerator implements ValueGenerator {

  @Override
  public String generate(ColumnDef column) {
    return RandomValueUtils.generate(column.getDataType());
  }

  @Override
  public void initialize() {
    // NOP
  }
}
