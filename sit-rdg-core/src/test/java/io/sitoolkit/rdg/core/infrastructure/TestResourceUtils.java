package io.sitoolkit.rdg.core.infrastructure;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

@Slf4j
public class TestResourceUtils {

  public static Path res2path(Object owner, String resourceName) {
    try {
      return Path.of(owner.getClass().getResource(resourceName).toURI());
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static Path copy(Object owner, String resourceName, Path toDir) {

    try {
      if (toDir.toFile().exists()) {
        FileUtils.deleteDirectory(toDir.toFile());
        log.info("Delete: {}", toDir);
      }

      toDir.toFile().mkdirs();
      log.info("Make directory: {}", toDir);

      Path srcFile = res2path(owner, resourceName);
      Path dstFile = toDir.resolve(srcFile.getFileName());
      Files.copy(srcFile, dstFile);
      log.info("Copy {} to {}", srcFile, dstFile);
      return dstFile;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
