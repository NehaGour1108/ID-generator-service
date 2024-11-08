package org.example;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class IDGeneratorService {
    private static final long START_TIMESTAMP = 1633046400000L; // Arbitrary start timestamp (e.g., 2021-10-01 00:00:00)
    private static final int MACHINE_ID_BITS = 10;  // Bits for machine ID
    private static final int SEQUENCE_BITS = 12;   // Bits for sequence number
    private static final int MAX_MACHINE_ID = (1 << MACHINE_ID_BITS) - 1;  // Maximum machine ID
    private static final int MAX_SEQUENCE = (1 << SEQUENCE_BITS) - 1;      // Maximum sequence number

    private long machineId;  // Machine ID (should be unique across your machines)
    private AtomicInteger sequence = new AtomicInteger(0);  // Sequence number for current millisecond
    private long lastTimestamp = -1L;  // Last timestamp

    // File to persist the state (machineId, lastTimestamp, sequence)
    private static final String STATE_FILE = "id_gen_state.dat";

    public IDGeneratorService(long machineId) {
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("Machine ID should be between 0 and " + MAX_MACHINE_ID);
        }
        this.machineId = machineId;
        loadState();  // Load previous state (if any)
    }

    // Load the last persisted state
    private void loadState() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(STATE_FILE))) {
            this.lastTimestamp = ois.readLong();
            this.sequence.set(ois.readInt());
        } catch (IOException e) {
            System.out.println("No previous state found, starting fresh.");
        }
    }

    // Save the current state
    private void saveState() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STATE_FILE))) {
            oos.writeLong(lastTimestamp);
            oos.writeInt(sequence.get());
        } catch (IOException e) {
            System.out.println("Failed to persist state.");
        }
    }

    // Generate the next unique ID
    public synchronized long generateUniqueID() {
        long timestamp = System.currentTimeMillis() - START_TIMESTAMP;

        if (timestamp == lastTimestamp) {
            // If we're still in the same millisecond, increment the sequence number
            int seq = sequence.incrementAndGet();
            if (seq > MAX_SEQUENCE) {
                // Sequence exceeded max limit, wait for next millisecond
                while (System.currentTimeMillis() - START_TIMESTAMP == lastTimestamp) {
                    // Busy-wait until the next millisecond
                }
                timestamp = System.currentTimeMillis() - START_TIMESTAMP;
                sequence.set(0);
            }
        } else {
            // Reset sequence for new timestamp
            sequence.set(0);
        }

        // Update the last timestamp
        lastTimestamp = timestamp;

        // Construct the ID based on timestamp, machine ID, and sequence number
        long id = (timestamp << (MACHINE_ID_BITS + SEQUENCE_BITS)) |
                (machineId << SEQUENCE_BITS) |
                sequence.get();

        // Persist the state after each ID generation
        saveState();

        return id;
    }

    public static void main(String[] args) {
        // Simulating an ID generation service with machineId as 1
        IDGeneratorService idGeneratorService = new IDGeneratorService(1);

        // Generate a few IDs and print them
        for (int i = 0; i < 10; i++) {
            System.out.println("Generated ID: " + idGeneratorService.generateUniqueID());
        }
    }
}
