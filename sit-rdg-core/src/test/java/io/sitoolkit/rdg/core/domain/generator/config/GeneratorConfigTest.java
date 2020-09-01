package io.sitoolkit.rdg.core.domain.generator.config;

import static org.hamcrest.core.Is.is;

import io.sitoolkit.rdg.core.domain.schema.SchemaInfo;
import io.sitoolkit.rdg.core.domain.schema.TableDef;
import io.sitoolkit.rdg.core.infrastructure.TestResourceUtils;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

@Slf4j
public class GeneratorConfigTest {

  @Rule public TestName testName = new TestName();

  @Test
  public void ignoreScale() {

    Path dstDir = TestResourceUtils.copyResDir(this, testName.getMethodName());

    SchemaInfo schemaInfo = SchemaInfo.read(dstDir);
    TableDef tab1 = schemaInfo.findTable("", "tab_1").get();
    TableDef tab2 = schemaInfo.findTable("", "tab_2").get();
    TableDef tab3 = schemaInfo.findTable("", "tab_3").get();

    GeneratorConfig generatorConfig = new GeneratorConfigReader().read(dstDir);
    long tab1RowCount = generatorConfig.getRowCount(tab1);
    long tab2RowCount = generatorConfig.getRowCount(tab2);
    long tab3RowCount = generatorConfig.getRowCount(tab3);

    log.info(
        "tab1RowCount:{}, tab2RowCount:{}, tab3RowCount:{}",
        tab1RowCount,
        tab2RowCount,
        tab3RowCount);

    Assert.assertThat(tab1RowCount, is(100L));
    Assert.assertThat(tab2RowCount, is(20L));
    Assert.assertThat(tab3RowCount, is(5L));
  }

  @Test
  public void lineSeparator() {

    Path dstDir = TestResourceUtils.copyResDir(this, testName.getMethodName());

    Path defPath = dstDir.resolve("default");
    Path crPath = dstDir.resolve("carriagereturn");
    Path lfPath = dstDir.resolve("linefeed");

    GeneratorConfigReader reader = new GeneratorConfigReader();

    GeneratorConfig defConfig = reader.read(defPath);
    GeneratorConfig crConfig = reader.read(crPath);
    GeneratorConfig lfConfig = reader.read(lfPath);

    Assert.assertThat(defConfig.getLineSeparator(), is(System.lineSeparator()));
    Assert.assertThat(crConfig.getLineSeparator(), is("\r\n"));
    Assert.assertThat(lfConfig.getLineSeparator(), is("\n"));
  }
}
