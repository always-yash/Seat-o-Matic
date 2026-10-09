package com.seatomatic.exam.contract;

public interface CapacityService {

    int calculateRequiredCapacity(Long examId);

    boolean hasCapacity(Long roomId, int requiredSeats);
}
