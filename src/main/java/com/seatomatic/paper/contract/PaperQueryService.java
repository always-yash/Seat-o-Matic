package com.seatomatic.paper.contract;

public interface PaperQueryService {

    String getPaperRequirements(Long examId);

    int getRequiredCopies(Long roomId, Long subjectId);

    boolean isDistributionComplete(Long roomId);
}
