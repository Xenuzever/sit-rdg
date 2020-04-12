package io.sitoolkit.rdg.core.domain.schema;

import static org.junit.Assert.assertThat;

import io.sitoolkit.rdg.core.application.SchemaAnalyzer;
import io.sitoolkit.rdg.core.infrastructure.TestResourceUtils;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Test;

@Slf4j
public class TableDependencyComparatorTest {

  TableDependencyComparator comparator = new TableDependencyComparator();

  @Test
  public void test() {
    SchemaAnalyzer analyzer = new SchemaAnalyzer();
    Path input = TestResourceUtils.res2path(this, "dependency-tables.sql");
    SchemaInfo schemaInfo = analyzer.read(input);

    List<TableDef> tables = schemaInfo.getAllTables();

    for (int i = 0; i < 10; i++) {
      Collections.shuffle(tables);

      log.debug(
          "before: {}", tables.stream().map(TableDef::getName).collect(Collectors.joining(",")));

      Collections.sort(tables, comparator);

      log.debug(
          "after: {}", tables.stream().map(TableDef::getName).collect(Collectors.joining(",")));

      assertThat(tables.get(0).getName(), Matchers.is("tab_1"));
      assertThat(tables.get(1).getName(), Matchers.is("tab_2"));
      assertThat(tables.get(2).getName(), Matchers.is("tab_3"));
    }
  }
}
