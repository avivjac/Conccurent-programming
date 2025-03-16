# Conccurent-Programming
SPL2 - Parallel & Event-Driven System Project Overview This project implements a parallel and event-driven system using a message bus to facilitate communication between microservices. It includes components such as CameraService, LiDarWorker, FusionSlam, and event-based interactions like DetectObjectsEvent and TrackedObjectsEvent.

Technologies Used Java 8+ Multithreading & Synchronization STOMP Protocol JUnit 5 for testing Maven/Gradle for dependency management

Setup & Installation Prerequisites: Install Java 8+ Install Maven or Gradle (if used) Clone the repository: bash Copy git clone https://github.com/your-username/SPL2.git cd SPL2 Build & Run If using Maven, build the project: bash Copy mvn clean install mvn exec:java -Dexec.mainClass="bgu.spl.mics.Main" If using Gradle, run: bash Copy gradle build gradle run

How It Works Microservices Communication:

The MessageBusImpl handles communication between microservices. CameraService sends DetectObjectsEvent, which is handled by LiDarWorker. LiDarWorker processes the event and sends TrackedObjectsEvent to FusionSlam. Concurrency & Synchronization:

Uses synchronized methods and blocking queues for safe parallel processing. Future is used to handle asynchronous operations.
