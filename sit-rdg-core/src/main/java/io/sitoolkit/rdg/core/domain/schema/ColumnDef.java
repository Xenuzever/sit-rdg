package io.sitoolkit.rdg.core.domain.schema;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ColumnDef implements Comparable<ColumnDef> {

  @JsonBackReference private TableDef table;

  @JsonProperty("columnName")
  private String name;

  @EqualsAndHashCode.Include private String fullyQualifiedName;

  private DataType dataType;

  // @JsonProperty("argumentsStringList")
  // private List<String> args;

  @JsonProperty("constraints")
  @Builder.Default
  private List<ConstraintAttribute> constraints = new ArrayList<>();

  @Builder.Default @JsonIgnore private List<RelationDef> relations = new ArrayList<>();

  public static ColumnDef shallowCopyExcludeRelations(ColumnDef columnDef) {
    ColumnDef newColumn = new ColumnDef();
    newColumn.setTable(columnDef.getTable());
    newColumn.setName(columnDef.getName());
    // newColumn.setArgs(columnDef.getArgs());
    newColumn.setDataType(columnDef.getDataType());
    newColumn.setConstraints(columnDef.getConstraints());
    return newColumn;
  }

  // this MUST NOT be replaced to @Getter(lazy = true)
  @JsonIgnore
  public String getFullyQualifiedName() {
    if (fullyQualifiedName == null) {
      fullyQualifiedName = table.getFullyQualifiedName() + "." + name;
    }
    return fullyQualifiedName;
  }

  @JsonIgnore
  public boolean isPrimaryKey() {
    return Objects.nonNull(constraints) && constraints.contains(ConstraintAttribute.PRIMARY_KEY);
  }

  @Override
  public int compareTo(ColumnDef o) {
    return getFullyQualifiedName().compareTo(o.getFullyQualifiedName());
  }
}
