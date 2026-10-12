package com.seatomatic.paper.service;

import com.seatomatic.paper.model.PaperDistribution;
import com.seatomatic.paper.model.dto.PacketSlipDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service generating formatted room packet slips and manifest labels for envelope handover.
 */
public class PacketSlipGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PacketSlipDTO generateSlip(PaperDistribution dist, String examTitle, String examDate, String invigilatorName) {
        if (dist == null) {
            throw new IllegalArgumentException("Distribution record must not be null");
        }

        PacketSlipDTO slip = new PacketSlipDTO();
        slip.setDistributionId(dist.getId());
        slip.setExamId(dist.getExamId());
        slip.setExamTitle(examTitle != null ? examTitle : "Examination #" + dist.getExamId());
        slip.setExamDate(examDate != null ? examDate : "N/A");
        slip.setRoomId(dist.getRoomId());
        slip.setRoomCode(dist.getRoomCode() != null ? dist.getRoomCode() : "Room #" + dist.getRoomId());
        slip.setSubjectId(dist.getSubjectId());
        slip.setSubjectCode(dist.getSubjectCode() != null ? dist.getSubjectCode() : "SUBJ");
        slip.setSubjectName(dist.getSubjectName() != null ? dist.getSubjectName() : "General Subject");
        slip.setSetCode(dist.getSetCode() != null ? dist.getSetCode() : "Set A");

        int studentCount = Math.max(0, dist.getRequired() - dist.getBuffer());
        slip.setStudentCount(studentCount);
        slip.setBufferCount(dist.getBuffer());
        slip.setTotalCopies(dist.getRequired());
        slip.setInvigilatorName(invigilatorName != null ? invigilatorName : "Unassigned");
        slip.setGeneratedAt(LocalDateTime.now().format(DATE_TIME_FORMATTER));

        return slip;
    }

    public List<PacketSlipDTO> generateBatchSlips(List<PaperDistribution> distributions, String examTitle, String examDate) {
        if (distributions == null || distributions.isEmpty()) {
            return List.of();
        }
        List<PacketSlipDTO> slips = new ArrayList<>(distributions.size());
        for (PaperDistribution d : distributions) {
            slips.add(generateSlip(d, examTitle, examDate, null));
        }
        return slips;
    }

    /**
     * Formats a plain-text ASCII slip suitable for thermal printing or log audits.
     */
    public String formatAsciiSlip(PacketSlipDTO slip) {
        if (slip == null) {
            return "";
        }
        return "========================================================\n" +
               "           SEAT-O-MATIC QUESTION PAPER PACKET           \n" +
               "========================================================\n" +
               String.format(" Exam       : %s\n", slip.getExamTitle()) +
               String.format(" Date       : %s\n", slip.getExamDate()) +
               String.format(" Room       : %s\n", slip.getRoomCode()) +
               String.format(" Subject    : %s - %s\n", slip.getSubjectCode(), slip.getSubjectName()) +
               String.format(" Paper Set  : %s\n", slip.getSetCode()) +
               "--------------------------------------------------------\n" +
               String.format(" Students   : %d\n", slip.getStudentCount()) +
               String.format(" Buffer     : %d\n", slip.getBufferCount()) +
               String.format(" Total Pkts : %d\n", slip.getTotalCopies()) +
               "--------------------------------------------------------\n" +
               String.format(" Invigilator: %s\n", slip.getInvigilatorName()) +
               " Recv Sign  : ____________________\n" +
               String.format(" Generated  : %s\n", slip.getGeneratedAt()) +
               "========================================================\n";
    }
}
