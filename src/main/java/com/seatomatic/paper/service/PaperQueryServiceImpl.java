package com.seatomatic.paper.service;

import com.seatomatic.paper.contract.PaperQueryService;

/**
 * Concrete implementation wrapper for PaperQueryService, delegating to PaperService.
 */
public class PaperQueryServiceImpl implements PaperQueryService {

    private final PaperService paperService;

    public PaperQueryServiceImpl() {
        this(new PaperService());
    }

    public PaperQueryServiceImpl(PaperService paperService) {
        this.paperService = paperService != null ? paperService : new PaperService();
    }

    @Override
    public String getPaperRequirements(Long examId) {
        return paperService.getPaperRequirements(examId);
    }

    @Override
    public int getRequiredCopies(Long roomId, Long subjectId) {
        return paperService.getRequiredCopies(roomId, subjectId);
    }

    @Override
    public boolean isDistributionComplete(Long roomId) {
        return paperService.isDistributionComplete(roomId);
    }
}
