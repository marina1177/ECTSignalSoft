package mpei.mdobro.diploma.domain.parse;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.complex.Complex;


@Data
@Slf4j
@RequiredArgsConstructor
public class HodographObject implements Comparable<HodographObject> {

    private final DefectTypes typeDefect;// for all
    private final Integer deep;// for all
    private final Integer freq;// for all
    private Double displacement; // for all
    public Complex complexNumber;// for all
    public Double defectLength; // wo CALIBRATION
    public Double defectExtension; // wo CALIBRATION
    public boolean firstImMax;
    private boolean firstAmpMax;
    public boolean firstReMax;
    public  boolean detected;

    public int compareTo(HodographObject p) {
        return Double.valueOf(complexNumber.abs()).compareTo(Double.valueOf(p.getComplexNumber().abs()));
    }

    @Override
    public String toString() {
        return "HodographObject{" +
                "displacement=" + displacement +
                ", freq=" + freq +
                ", deep=" + deep +
                ", \ncomplexNumber:" + complexNumber.toString() +
                "}\n";
    }
}
