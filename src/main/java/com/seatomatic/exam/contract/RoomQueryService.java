package com.seatomatic.exam.contract;

public interface RoomQueryService {

    String findAvailableRooms(Long examId);

    int countSeats(Long roomId);

    boolean roomTimingConflict(Long roomId, String examDate, String startTime, String endTime);
}
