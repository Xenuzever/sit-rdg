package io.sitoolkit.rdg.core.domain.generator.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sitoolkit.rdg.core.domain.schema.ColumnDef;
import io.sitoolkit.rdg.core.domain.schema.TableDef;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public class GeneratorConfig {

  @JsonProperty("scale")
  private String scaleStr;

  @JsonProperty("defaultRowCount")
  private Integer defaultRowCount;

  @JsonProperty("defaultRequiredValueCount")
  private Integer defaultRequiredValueCount;

  @JsonManagedReference
  @JsonProperty("schemaConfigs")
  private List<SchemaConfig> schemaConfigs = Collections.emptyList();

  public Integer getDefaultRowCount() {
    if (Objects.isNull(defaultRowCount)) {
      defaultRowCount = Integer.valueOf(5);
    }
    return defaultRowCount;
  }

  public Integer getDefaultRequiredValueCount() {
    if (Objects.isNull(defaultRequiredValueCount)) {
      defaultRequiredValueCount = Integer.valueOf(5);
    }
    return defaultRequiredValueCount;
  }

  @JsonIgnore
  @Getter(lazy = true)
  private final Scale scale = Scale.parse(scaleStr);

  @JsonIgnore
  @Getter(lazy = true)
  private final Map<String, Integer> rowCountMap =
      schemaConfigs.stream()
          .flatMap(s -> s.getTableConfigs().stream())
          .collect(Collectors.toMap(t -> t.getFullQualifiedName(), t -> t.getRowCount()));

  @JsonIgnore
  @Getter(lazy = true)
  private final Map<String, Integer> requiredValueCountMap =
      schemaConfigs.stream()
          .flatMap(s -> s.getTableConfigs().stream())
          .flatMap(t -> t.getColumnConfigs().stream())
          .collect(Collectors.toMap(c -> c.getFullQualifiedName(), c -> c.getRequiredValueCount()));

  @Getter(lazy = true)
  @JsonIgnore
  private final Map<String, ValueGenerator> valueGeneratorMap = initValueGenMap();

  @JsonIgnore
  public Integer getRowCount(TableDef tableDef) {

    Integer rowCount =
        getRowCountMap().getOrDefault(tableDef.getFullyQualifiedName(), getDefaultRowCount());

    return getScale().apply(rowCount);
  }

  @JsonIgnore
  public Integer getRequiredValueCount(ColumnDef col) {

    Integer requiredValueCount =
        getRequiredValueCountMap()
            .getOrDefault(col.getFullyQualifiedName(), getDefaultRequiredValueCount());

    return getScale().apply(requiredValueCount);
  }

  public ValueGenerator findValueGenerator(ColumnDef column) {
    return getValueGeneratorMap()
        .computeIfAbsent(column.getFullyQualifiedName(), k -> new RandomValueGenerator());
  }

  private Map<String, ValueGenerator> initValueGenMap() {
    Map<String, ValueGenerator> map =
        schemaConfigs.stream()
            .flatMap(s -> s.getTableConfigs().stream())
            .flatMap(t -> t.getColumnConfigs().stream())
            .collect(Collectors.toMap(ColumnConfig::getFullQualifiedName, ColumnConfig::getSpec));

    for (SchemaConfig sconfig : getSchemaConfigs()) {
      for (TableConfig tconfig : sconfig.getTableConfigs()) {
        for (ColumnConfig cconfig : tconfig.getColumnConfigs()) {

          if (cconfig.getSpec() instanceof MultiSequenceValueGenerator) {
            MultiSequenceValueGenerator msvg = (MultiSequenceValueGenerator) cconfig.getSpec();

            msvg.setTotalRequiredCount(tconfig.getRowCount());
            msvg.postDeserialize();
            String fully = tconfig.getFullQualifiedName() + "." + msvg.getSubColumn();
            map.put(fully, msvg.getSubColGen());
          }
        }
      }
    }

    return map;
  }
}
