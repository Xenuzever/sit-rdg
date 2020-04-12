package io.sitoolkit.rdg.core.application;

import static com.google.common.truth.Truth.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import io.sitoolkit.rdg.core.domain.schema.ColumnPair;
import io.sitoolkit.rdg.core.domain.schema.RelationDef;
import io.sitoolkit.rdg.core.domain.schema.SchemaDef;
import io.sitoolkit.rdg.core.domain.schema.SchemaInfo;
import io.sitoolkit.rdg.core.domain.schema.TableDef;
import io.sitoolkit.rdg.core.infrastructure.TestResourceUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class SchemaAnalyzerTest {

  SchemaAnalyzer analyzer = new SchemaAnalyzer();
  Path workingDir;

  @Before
  public void setup() throws URISyntaxException {
    workingDir = Paths.get(SchemaAnalyzer.class.getClassLoader().getResource(".").toURI());
  }

  // @Test
  // public void testReadDynamicRelations() throws IOException, URISyntaxException {
  //   Path input = workingDir.resolve("create-and-select.sql");
  //   SchemaInfo answer = analyzer.read(input);

  //   List<SchemaDef> schemas = new ArrayList<>(answer.getSchemas());
  //   List<TableDef> tables =
  //       schemas.get(0).getTables().stream()
  //           .sorted(Comparator.comparing(TableDef::getName))
  //           .collect(Collectors.toList());

  //   Function<TableDef, List<ColumnDef>> sortColumnsFunc =
  //       table -> {
  //         return table.getColumns().stream()
  //             .sorted(Comparator.comparing(ColumnDef::getName))
  //             .collect(Collectors.toList());
  //       };
  //   List<ColumnDef> table1columns = new ArrayList<>(sortColumnsFunc.apply(tables.get(0)));
  //   List<ColumnDef> table2columns = new ArrayList<>(sortColumnsFunc.apply(tables.get(1)));
  //   List<ColumnDef> table3columns = new ArrayList<>(sortColumnsFunc.apply(tables.get(2)));

  //   List<RelationDef> table1relations4group1 = table1columns.get(0).getRelations();
  //   List<RelationDef> table1relations4group2 = table1columns.get(1).getRelations();
  //   List<RelationDef> table2relations4group1 = table2columns.get(1).getRelations();
  //   List<RelationDef> table3relations4group2 = table3columns.get(1).getRelations();

  //   assertThat(table1relations4group1).containsAnyIn(table2relations4group1);
  //   assertThat(table1relations4group2).containsAnyIn(table3relations4group2);

  //   assertThat(table1relations4group1).containsExactlyElementsIn(table1relations4group2);
  // }

  @Test
  public void testReadStaticRelations() throws Exception {
    Path input = TestResourceUtils.res2path(this, "create-table-with-foreign-key.sql");
    SchemaInfo schemaInfo = analyzer.read(input);
    assertReadStaticRelations(schemaInfo);
  }

  @Test
  public void testAnalyzeStaticRelations() {
    Path input = TestResourceUtils.res2path(this, "create-table-with-foreign-key.sql");
    Path output = analyzer.analyze(input);
    SchemaInfo schemaInfo = SchemaInfo.read(output.getParent());
    assertReadStaticRelations(schemaInfo);
  }

  void assertReadStaticRelations(SchemaInfo schemaInfo) {
    SchemaDef schema = schemaInfo.findByName("").orElseThrow();

    TableDef tab_1 = schema.findTable("tab_1").orElseThrow();
    TableDef tab_2 = schema.findTable("tab_2").orElseThrow();
    TableDef tab_3 = schema.findTable("tab_3").orElseThrow();
    TableDef tab_4 = schema.findTable("tab_4").orElseThrow();

    assertThat("tab_1 is independent", tab_1.isDependent(), is(false));
    assertThat("tab_2 is dependent", tab_2.isDependent(), is(true));
    assertThat("tab_3 is dependent", tab_3.isDependent(), is(true));
    assertThat("tab_4 is dependent", tab_4.isDependent(), is(true));

    assertThat(tab_2.getAncestors().get(0).getName(), is("tab_1"));
    assertThat(tab_3.getAncestors().get(0).getName(), is("tab_2"));
    assertThat(tab_3.getAncestors().get(1).getName(), is("tab_1"));

    assertThat("tab_2 depends on tab_1", tab_2.dependsOn(tab_1), is(true));
    assertThat("tab_3 depends on tab_2", tab_3.dependsOn(tab_2), is(true));
    assertThat("tab_4 depends on tab_1", tab_4.dependsOn(tab_1), is(true));

    List<RelationDef> relations = schema.getRelations();

    assertThat(relations.size(), is(3));

    RelationDef rel_1_2 = relations.get(0);
    RelationDef rel_2_3 = relations.get(1);

    assertThat(rel_2_3.getParent().orElseThrow(), is(rel_1_2));

    ColumnPair pair_1_2 = rel_1_2.getColumnPairs().iterator().next();

    assertThat(pair_1_2.getLeft().getName(), is("col_1_1"));
    assertThat(pair_1_2.getRight().getName(), is("col_2_1"));

    List<ColumnPair> pair_2_3 = new ArrayList<>(rel_2_3.getColumnPairs());

    assertThat(pair_2_3.get(0).getLeft().getName(), is("col_2_1"));
    assertThat(pair_2_3.get(0).getRight().getName(), is("col_3_1"));

    assertThat(pair_2_3.get(1).getLeft().getName(), is("col_2_2"));
    assertThat(pair_2_3.get(1).getRight().getName(), is("col_3_2"));
  }

  @Test
  public void testReadAllSchemas() throws IOException {
    Path input = workingDir.resolve("multiple-schemas-create.sql");
    SchemaInfo schemaInfo = analyzer.read(input);
    assertReadAllSchemas(schemaInfo);
  }

  @Test
  public void testAnalyzeAllSchemas() {
    Path input = workingDir.resolve("multiple-schemas-create.sql");
    Path output = analyzer.analyze(input);
    SchemaInfo schemaInfo = SchemaInfo.read(output.getParent());
    assertReadAllSchemas(schemaInfo);
  }

  void assertReadAllSchemas(SchemaInfo schemaInfo) {
    SchemaDef schema1 = schemaInfo.findByName("SCHEMA1").orElseThrow();
    TableDef table1_1 = schema1.findTable("tabLE1").orElseThrow();
    assertThat(table1_1.getColumns().get(0).getName(), is("COLUMN1"));
    assertThat(table1_1.getColumns().get(1).getName(), is("COLUMN2"));

    SchemaDef schema2 = schemaInfo.findByName("SCHEMA2").orElseThrow();
    TableDef table1_2 = schema2.findTable("tabLE1").orElseThrow();
    assertThat(table1_2.getColumns().get(0).getName(), is("COLUMNA"));
    assertThat(table1_2.getColumns().get(1).getName(), is("COLUMNB"));
  }
}
