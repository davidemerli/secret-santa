package it.davidemerli.secretsanta;

import javafx.util.Pair;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static List<Member> participants = new ArrayList<>();
    private static List<Pair<Member, Member>> matchesToExclude = new ArrayList<>();

    public static void main(String[] args) {
        File currentDir = new File(System.getProperty("user.dir"), "settings");

        try {
            loadParticipants(new File(currentDir, "participants.txt"));
        } catch (Exception ex) {
            System.out.println("Could not load participants, aborting...");
            ex.printStackTrace();

            return;
        }

        try {
            loadMatchesToExclude(new File(currentDir, "exclude-patterns.txt"));
        } catch (Exception ex) {
            System.out.println("Could not load matchesToExclude, aborting...");
            ex.printStackTrace();

            return;
        }

        try {
            MailSender sender = new MailSender(new File(currentDir, "credentials.txt"));

            sendMails(sender, new File(currentDir, "message.txt"));
        } catch (IOException ex) {
            System.out.println("Failed to send mails.");
            ex.printStackTrace();
        }
    }

    private static void sendMails(MailSender mailSender, File messageFile) throws IOException {
        Map<Member, Member> sorting = new LinkedHashMap<>();

        participants.forEach(m -> {
            List<Member> toChoose = participants.stream()
                    .filter(mm -> !sorting.containsValue(mm))
                    .filter(mm -> m != mm)
                    .filter(mm -> !matchesToExclude.contains(new Pair<>(m, mm)))
                    .collect(Collectors.toList());
            if(toChoose.size() > 0) {
                sorting.put(m, toChoose.get(new Random().nextInt(toChoose.size())));
            } else {
                System.out.println("toChoose size is <= 0 (??)");
            }
        });

        StringBuilder message = new StringBuilder();

        SettingsReader.getLinesFromFile(messageFile).stream()
                .map(String::trim)
                .peek(message::append)
                .forEach(s -> message.append("\n"));

        sorting.forEach((from, to) -> {
            String text = message.toString().replaceAll("%from", from.name).replaceAll("%to", to.name);

            try {
                System.out.printf("from %s to %s\n", from.name, to.name);
                mailSender.sendMail(text, from.mail);
            } catch (MessagingException ex) {
                System.out.println(String.format("Couldn't send email to '%s'", to.mail));
                ex.printStackTrace();
            }
        });

        sorting.forEach((from, to) -> System.out.println(from.hash + "\t\t\t\t --------> \t\t\t\t" + to.hash));
    }

    private static void loadParticipants(File file) throws IOException {
        SettingsReader.getLinesFromFile(file).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(s -> {
                    String[] split = s.split(":");
                    if (split.length < 2) {
                        System.out.println(String.format("line '%s' has wrong format, skipping", s));
                    } else {
                        Member m = new Member(split[0], split[1]);

                        if (!participants.contains(m))
                            participants.add(m);
                        else
                            System.out.println(String.format("line '%s' is a duplicate, skipping", s));
                    }
                });
    }

    private static void loadMatchesToExclude(File file) throws IOException {
        SettingsReader.getLinesFromFile(file).stream()
                .map(String::trim)
                .map(s -> s.split(":"))
                .filter(s -> s.length == 2)
                .forEach(split -> {
                    Optional<Member> from = getMemberFromName(split[0]);
                    Optional<Member> to = getMemberFromName(split[1]);
                    if (from.isPresent() && to.isPresent()) {
                        matchesToExclude.add(new Pair<>(from.get(), to.get()));
                    } else {
                        System.out.println(String.format("Please check that both '%s' and '%s' are present", split[0], split[1]));
                    }
                });
    }

    private static Optional<Member> getMemberFromName(String memberName) {
        return participants.stream().filter(member -> member.name.equalsIgnoreCase(memberName)).findFirst();
    }

    private static class Member {
        private final String name, mail;
        private final int hash;

        Member(String name, String mail) {
            this.name = name;
            this.mail = mail;
            this.hash = Math.abs((name + mail).hashCode());
        }
    }
}
