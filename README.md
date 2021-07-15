# Idx SYNC (incubation)

(!) Incubation project - not yet usable

## Description

Idx Sync is a folder-synchronization/backup tool that can automatically detect synchronizable folders (sources and targets) 
in the connected file systems / media devices (USB drives, external harddisks) and perform one-way synchronization for 
backup purposes. 

The synchronization will detect added, modified and removed files, and perform the necessary file operations
to synchronize changes between folders.

## Build

To build this project:

with Gradle (default tasks: _clean build shadowJar_):

    gradle

with Maven (default tasks: _clean install_):

    mvn

## Maintenance

To check for updated dependency versions, run:

    gradle dependencyUpdates