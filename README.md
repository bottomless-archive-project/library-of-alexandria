# Library of Alexandria

Library of Alexandria (LoA in short) is a project that aims to collect and archive PDF documents from the internet.

In our modern age new text documents are born in a blink of an eye then (often just as quickly) disappear from the internet. We find it a noble task to save these documents for future generations.

This project aims to support this noble goal in a scalable way. We want to make the archival activity streamlined and easy to do even in a huge (Terabyte / Petabyte) scale. This way we hope that more and more people can start their own collection helping the archiving effort.

## Prerequisites

We tried to keep the prerequisites of the project on the minimum because one of the goals of the project is to make it easy to start archiving even with a limited technical knowledge.

The list of required software to start archiving:
- [Java 11](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
- [MySQL 5.7](https://dev.mysql.com/downloads/mysql/5.7.html)

## Installation

Installing the apps and the prerequisite software is quite straightforward. At this time we provide a guide to the Windows based systems. Installing LoA on Linux systems are supported as well but requires more technical knowledge. An ideal deployment is running the apps in separate VMs or Docker containers but for the sake of simplicity we are not doing that in this guide. In the future we will create a more advanced guide.

### Installing Java

First you need to download the Java 11 Runtime Environment. It's available [here](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html). After the download is complete you should run the installer and follow the directions it provides until the installation is complete.

Once it's done, if you open a command line (write cmd to the Start menu's search bar) you will be able to use the java command. Try to write `java -version`. You should get something similar:

```
java version "11" 2018-09-25
Java(TM) SE Runtime Environment 18.9 (build 11+28)
Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11+28, mixed mode)
```

### Installing MySQL

Download MySQL 5.7 from [here](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html). After the download is complete run the installer and follow the directions it provides. If it's possible install the MySQL Workbench tool as well because you will need it later for administrative tasks.

### Running the crawler

WIP!!!

## Applications

The Library of Alexandria project consist of more than one (usually) scalable applications. Not all of them are mandatory for the archiving effort. Some of them are created for administrating or maintenance purposes.

### Database Application

The LoA project stores it's crawl related data in a MySQL database. Occasionally between releases a database migration is necessary. This application's goal is to streamline this process. Also in the future it will provide basic database administrator tasks (query statistics, initiate the re-crawling of failed tasks etc).

## Domain language

WIP!!!

1. **Vault**: The location where the collected documents are saved.
2. **Document**: A document collected from the internet.
3. **Staging area**: A temporary location where the collected documents placed for post processing before going to the archive.
4. **Source**: ...