package mpei.mdobro.diploma.domain.research;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mpei.mdobro.diploma.domain.parse.HodographObject;

import java.util.List;
import java.util.Map;

@Data
@Slf4j
@RequiredArgsConstructor
public class ProcessResearch {

    private final Map<Integer, List<HodographObject>> map;

    private Map<Integer, Map<Integer, List<HodographObject>>>
            freqToDeepAndLengthAngleLimitList;
    private Map<Integer, Map<Integer, List<HodographObject>>> freqToDeepAndLengthAngleModelList;


}
