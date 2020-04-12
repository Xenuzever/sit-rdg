package io.sitoolkit.rdg.core.domain.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 当該オブジェクトが「等価である」と評価される条件は、 「互いにテーブル名、列名が等価であるColumnDefオブジェクトを持つこと」である。
 *
 * <p>この挙動は、 「フィールドをSortedSet columnsのみ持ち」、「@EqualsAndHashcodeを宣言する」 ことにより実現している。
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class ColumnPair {

  @EqualsAndHashCode.Include @Getter private List<ColumnDef> columns = new ArrayList<>();

  public ColumnPair(ColumnDef left, ColumnDef right) {
    List.of(left, right).stream().forEach(columns::add);
  }

  @JsonIgnore
  public ColumnDef getLeft() {
    return columns.get(0);
  }

  @JsonIgnore
  public ColumnDef getRight() {
    return columns.get(1);
  }

  @JsonCreator
  public ColumnPair(@JsonProperty("left") String left, @JsonProperty("right") String right) {

    this(
        ColumnDef.builder().fullyQualifiedName(left).build(),
        ColumnDef.builder().fullyQualifiedName(right).build());
  }

  @JsonValue
  public Map<String, String> toJson() {
    Map<String, String> json = new HashMap<>();
    json.put("left", getLeft().getFullyQualifiedName());
    json.put("right", getRight().getFullyQualifiedName());
    return json;
  }

  public void reset(ColumnDef left, ColumnDef right) {
    columns.clear();
    columns.add(left);
    columns.add(right);
  }

  @Override
  public String toString() {
    return "left:"
        + getLeft().getFullyQualifiedName()
        + ", right: "
        + getRight().getFullyQualifiedName();
  }
}
