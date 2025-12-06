Voting Management System

Overview
This is a Java-based Voting Management System that allows users to register candidates, vote with timestamps, view voting history, change votes within 24 hours, and generate a leaderboard of top candidates. The system also supports blocking voter registration after a specific time. Data is stored in MySQL so it persists even when the program is closed.

Features

Register Candidates: Add new candidates to the system.

Cast Votes: Users can vote for candidates, with timestamps recorded.

Get Votes: Retrieve total votes for a specific candidate.

Top N Candidates: Generate a leaderboard of top candidates.

Voting History: Retrieve detailed voting history per voter.

Block Voter Registration: Freeze voter registrations after a specified timestamp.

Change Vote: Voters can change their vote within 24 hours.

Java Classes and Files

MySQLDatabaseManager.java: Handles connection to MySQL.

VotingSystemMySQL.java: Contains all the core logic for registration, voting, and reporting.

Main.java: A sample class to run and test the voting system.

Setup Instructions

Install MySQL server on your computer.

Open MySQL client or Workbench and run the following commands to create the database and tables:

Create the database:
CREATE DATABASE voting_system;

Use the database:
USE voting_system;

Create the candidates table:
CREATE TABLE candidates (
candidate_id VARCHAR(50) PRIMARY KEY,
votes INT DEFAULT 0
);

Create the voters table:
CREATE TABLE voters (
voter_id VARCHAR(50) PRIMARY KEY
);

Create the votes table:
CREATE TABLE votes (
voter_id VARCHAR(50),
candidate_id VARCHAR(50),
timestamp BIGINT,
FOREIGN KEY(voter_id) REFERENCES voters(voter_id),
FOREIGN KEY(candidate_id) REFERENCES candidates(candidate_id)
);

Update MySQLDatabaseManager.java with your MySQL username and password.

Compiling and Running the Project

Place all Java files in the src folder.

If not using Maven, add MySQL Connector jar to your build path.

Compile from src folder:
javac -cp ".;../lib/mysql-connector-java-8.1.0.jar" *.java (for Windows)
javac -cp ".:../lib/mysql-connector-java-8.1.0.jar" *.java (for Linux/Mac)

Run the program:
java -cp ".;../lib/mysql-connector-java-8.1.0.jar" Main (for Windows)
java -cp ".:../lib/mysql-connector-java-8.1.0.jar" Main (for Linux/Mac)

Example Usage

Register candidates: Alice and Bob

Voter1 votes for Alice

Voter2 votes for Bob

Get total votes for Alice

Get top candidate leaderboard

View voter1’s voting history

Notes

Make sure MySQL server is running before executing the program.

Votes, candidates, and voter history are stored in the database permanently.

Users can change their vote only within 24 hours of the original vote.

Voter registration can be blocked after a certain timestamp to stop new voters.