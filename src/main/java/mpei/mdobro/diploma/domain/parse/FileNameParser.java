package mpei.mdobro.diploma.domain.parse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class FileNameParser {

    private final File filename;

    public DefectTypes getDefectType() {

        String fullName = filename.getName();

        String fileNameWithOutExt = FilenameUtils.removeExtension(fullName);

        String[] wordArray = fileNameWithOutExt.split("_");
        String defectTypeString = wordArray[0];

        if (defectTypeString.contains("calibration")) {
            return DefectTypes.CALIBRATION;
        } else if (defectTypeString.contains("limit"))
            return DefectTypes.LIMIT;
        else if (defectTypeString.contains("rect"))
            return DefectTypes.RECT;
        else if (defectTypeString.contains("tri"))
            return DefectTypes.TRI;
        else if (defectTypeString.contains("poligon"))
            return DefectTypes.PLGN;

        else {
            log.debug("Unknown defect type: {}", defectTypeString);
            return DefectTypes.UNKNOUWN_TYPE;
        }
    }

    public Integer getDefectDeep() {

        String fullName = filename.getName();

        String fileNameWithOutExt = FilenameUtils.removeExtension(fullName);
        ArrayList<String> wordArray = new ArrayList<>(Arrays.asList(fileNameWithOutExt.split("_")));

        int indexWordDeep = wordArray.indexOf("deep");

        Integer deepValue = Integer.valueOf(wordArray.get(indexWordDeep + 1));
        return deepValue;
    }

    public Double getDefectLength() {

        String fullName = filename.getName();
        Double lengthValue = 0.0;

        String fileNameWithOutExt = FilenameUtils.removeExtension(fullName);
        ArrayList<String> wordArray = new ArrayList<>(Arrays.asList(fileNameWithOutExt.split("_")));

        if (wordArray.contains("length")) {
            int indexWordLength = wordArray.indexOf("length");
            lengthValue = Double.valueOf(wordArray.get(indexWordLength + 1));
        }
        return lengthValue;
    }
}
