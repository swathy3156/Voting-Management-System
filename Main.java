public class Main {
    public static void main(String[] args) {
        MySQLDatabaseManager.initializeDatabase();
        VotingSystemMySQL votingSystem = new VotingSystemMySQL();

        votingSystem.registerCandidate("Alice");
        votingSystem.registerCandidate("Bob");

        votingSystem.vote(System.currentTimeMillis(), "voter1", "Alice");
        votingSystem.vote(System.currentTimeMillis(), "voter2", "Bob");

        System.out.println("Alice votes: " + votingSystem.getVotes("Alice"));
        System.out.println("Top candidate: " + votingSystem.topNCandidates(1));

        votingSystem.blockVoterRegistration(System.currentTimeMillis() + 10000); // block after 10 sec

        System.out.println("Voter1 history: " + votingSystem.getVotingHistory("voter1"));
    }
}
