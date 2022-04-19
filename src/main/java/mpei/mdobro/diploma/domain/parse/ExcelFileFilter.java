package mpei.mdobro.diploma.domain.parse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;

@Slf4j
@RequiredArgsConstructor
public class ExcelFileFilter implements FileFilter {

    // конструктор
    private final String fileType;

    @Override
    public boolean accept(File file) {

        log.info("file: {}", file);
        if (file == null) {
            return false;
        }
        if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead()) {
            return file.getName().toLowerCase().endsWith(fileType);
        } else {
            return false;
        }
    }
}
