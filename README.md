# Atomic ID Generator Service

<img width="733" alt="Screenshot 2024-11-29 at 10 43 53 PM" src="https://github.com/user-attachments/assets/7351413c-4eff-4e17-b811-44395a82c723">

## Overview

This project implements an **Atomic ID Generator Service** that generates globally unique IDs (UUIDs) based on a distributed, time-based approach inspired by the **Snowflake ID generation algorithm**. The generated IDs are **64-bit** integers that are guaranteed to be unique, sortable by creation time, and persistent across application restarts.

This ID generator is ideal for distributed systems where unique identifiers are needed, such as in databases, microservices, and any other system where generating non-colliding IDs across different machines is critical.

## Key Features

- **Globally Unique IDs**: Generates unique IDs based on the current timestamp, machine ID, and sequence number.
- **Time-based Sorting**: The generated IDs are sortable by timestamp, ensuring that IDs are always ordered based on the time they were generated.
- **Machine-specific**: Each machine (node) is assigned a unique machine ID, ensuring that IDs generated from different machines do not collide.
- **Persistence**: The system saves the state (timestamp, sequence number) to disk, allowing it to resume generating IDs after a crash or restart without losing data or generating duplicate IDs.
- **Thread-Safe**: The service ensures thread-safety by using `AtomicInteger` for managing the sequence number.

## How It Works

The ID generator produces a **64-bit ID** composed of three main components:
1. **Timestamp** (42 bits): The number of milliseconds since a fixed starting point.
2. **Machine ID** (10 bits): A unique ID assigned to each machine or node in the system.
3. **Sequence Number** (12 bits): A counter that is incremented for every ID generated within the same millisecond. It ensures that multiple IDs can be generated in the same millisecond without collision.

### ID Structure
```
| 42 bits Timestamp | 10 bits Machine ID | 12 bits Sequence Number |
```

- **Timestamp**: The timestamp is based on the number of milliseconds since an arbitrary start date (`2021-10-01`).
- **Machine ID**: This is an identifier for the machine generating the IDs. It's unique across machines to avoid conflicts.
- **Sequence Number**: This ensures that even if multiple IDs are generated in the same millisecond, each ID will be unique.

## Project Components

- **IDGeneratorService**: This is the core service that generates the unique IDs. It uses the `AtomicInteger` to manage the sequence number, and a timestamp to generate unique IDs based on the current millisecond.
- **Persistence**: The service saves and loads the last generated state (timestamp and sequence) to/from a file (`id_gen_state.dat`). This ensures that the service can resume from the last state after a restart without generating duplicate IDs.
- **State Management**: The machine ID, last timestamp, and sequence number are saved and loaded from a file to maintain consistency across sessions.

## Setup and Usage

### Prerequisites
- Java 8 or higher
- A basic understanding of concurrency and ID generation algorithms like Snowflake

### Steps to Use

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yourusername/atomic-id-generator.git
   cd atomic-id-generator
   ```

2. **Compile and Run**:
   - Open the `IDGeneratorService` class and set the `machineId` for your machine (each machine should have a unique ID).
   - Build and run the application.

   ```java
   IDGeneratorService idGeneratorService = new IDGeneratorService(1); // 1 is an example machine ID
   for (int i = 0; i < 10; i++) {
       System.out.println("Generated ID: " + idGeneratorService.generateUniqueID());
   }
   ```

3. **Output Example**:
   The output will look something like this:
   ```
   Generated ID: 141658276125005312
   Generated ID: 141658276126568193
   Generated ID: 141658276127010945
   ```

4. **Persistent ID Generation**:
   The state of the generator (timestamp and sequence number) is automatically saved in `id_gen_state.dat` after each ID generation. If the application restarts, it will load the previous state and continue generating unique IDs.

### Example Output
```bash
Generated ID: 470157775044192338
Generated ID: 470157775044192339
Generated ID: 470157775044192340
Generated ID: 470157775044192341
```
