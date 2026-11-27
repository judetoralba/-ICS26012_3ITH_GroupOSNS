CPU and Disk Scheduling Simulator - Group OSNS (3ITH)
ICS26012 - Operating Systems, 1st Semester AY 2026-2027

Members and algorithms
  Jude  - Priority (Non-Preemptive)   src/osns/cpu/PriorityNonPreemptive.java
  Ken   - Multilevel Queue            src/osns/cpu/MultilevelQueue.java
  Rylev - SSTF                        src/osns/disk/SSTF.java
  Mark  - LOOK                        src/osns/disk/LOOK.java

Java version
  Requires Java 8 or newer (JDK or JRE). Compiled with --release 8, tested on Java 21.
  Uses only the Java standard library (Swing). No database or internet needed.

How to run
  Double-click OSNS_Scheduler.jar
  or:  java -jar OSNS_Scheduler.jar

How to run the algorithm tests (24 hand-solved cases)
  java -cp OSNS_Scheduler.jar osns.test.AlgorithmTests

How to rebuild from source
  javac --release 8 -d build <all .java files under src>
  jar cfe OSNS_Scheduler.jar osns.Main -C build .

Folder layout
  src/osns/Main.java     program entry point
  src/osns/cpu           CpuScheduler interface + CPU algorithms
  src/osns/disk          DiskScheduler interface + disk algorithms
  src/osns/model         shared data and metric formulas
  src/osns/ui            Swing screens, charts, validation, sample data
  src/osns/test          console test runner

Repository link: https://github.com/judetoralba/-ICS26012_3ITH_GroupOSNS.git
