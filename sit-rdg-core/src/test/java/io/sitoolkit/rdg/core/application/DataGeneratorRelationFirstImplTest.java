package io.sitoolkit.rdg.core.application;

import static org.junit.Assert.assertThat;

import io.sitoolkit.rdg.core.infrastructure.TestResourceUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Test;

@Slf4j
public class DataGeneratorRelationFirstImplTest {

  DataGeneratorRelationFirstImpl dataGenerator = new DataGeneratorRelationFirstImpl();

  @Test
  public void test() throws IOException {
    Path inDir = Path.of("target/gen-in");
    FileUtils.deleteDirectory(inDir.toFile());
    TestResourceUtils.copy(this, "schema.json", inDir);

    Path outDir = Path.of("target/gen-out");
    FileUtils.deleteDirectory(outDir.toFile());

    List<Path> outFiles = dataGenerator.generate(inDir, List.of(outDir));

    for (Path outFile : outFiles) {
      log.info("\n\n{}\n{}", outFile, Files.readString(outFile));
    }

    Path tab_1 = outFiles.get(0);
    Path tab_2 = outFiles.get(1);
    Path tab_3 = outFiles.get(2);

    assertThat(tab_1.getFileName().toString(), Matchers.is("tab_1.csv"));
    assertThat(tab_2.getFileName().toString(), Matchers.is("tab_2.csv"));
    assertThat(tab_3.getFileName().toString(), Matchers.is("tab_3.csv"));
  }
}
