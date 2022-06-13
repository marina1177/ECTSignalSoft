package mpei.mdobro.diploma.domain.parse;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@RequiredArgsConstructor
public class NDTDataObject {

    private final DefectTypes typeDefect;
    private  String description;
    private final Integer deep;
    private final Integer freq;
    private final Double amplitude;
    private final Double phase;
    private final Double rotatedPhase;
    private final int numberOfExperiment;

    public  boolean detected;

//    public int compareTo(NDTDataObject p) {
//        return Double.valueOf(phase).compareTo(Double.valueOf(p.getPhase()));
//    }

    @Override
    public String toString() {
        return "NDTDataObject{" +
                "freq=" + freq +
                ", deep=" + deep +
                ", \nphase:" + phase +
                ", \nrotated phase:" + rotatedPhase +
                "}\n";
    }
}
