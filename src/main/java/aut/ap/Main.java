package aut.ap;

import dao.EmailDAO;
import dao.UserDAO;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static UserDAO userDAO = new UserDAO();
    private static EmailDAO emailDAO = new EmailDAO();
    private static User currentUser = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        while (true) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showUserMenu();
            }
        }
    }

    private static void showLoginMenu() {
        System.out.println("=== Welcome to Email System ===");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                signUp();
                break;
            case "2":
                login();
                break;
            case "0":
                System.exit(0);
            default:
                System.out.println("Invalid choice!");
        }
    }

    private static void signUp() {
        System.out.println("Enter your name: ");
        String name = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim().toLowerCase();
        if (userDAO.existsByEmail(email)) {
            System.out.println("Email already registered.");
            return;
        }
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);

        userDAO.save(user);
        System.out.println("Registration successful.");
    }

    private static void login() {
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim().toLowerCase();
        if (!email.contains("@"))
            email = email.toLowerCase() + "@milou.com";
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = userDAO.login(email, password);
        if (user == null) {
            System.out.println("Invalid email or password.");
        } else {
            currentUser = user;
            System.out.println("Login successful. Welcome " + currentUser.getEmail());
        }
    }

    private static void showUserMenu() {
        List<Email> emails = emailDAO.findUnreadEmailsByUser(currentUser.getId());
        if (currentUser.isJustLoggedIn() && !emails.isEmpty()) {
            viewUnreadEmails();
            currentUser.setJustLoggedIn(false);
        }
        System.out.println("\n=== Menu for " + currentUser.getEmail() + " ===");
        System.out.println("1. Send Email");
        System.out.println("2. View Inbox");
        System.out.println("3. View Unread Emails");
        System.out.println("4. View sent Emails");
        System.out.println("5. Logout");
        System.out.print("Choose: ");
        String choice = scanner.nextLine();

        switch (choice) {
            case "1":
                sendEmail();
                break;
            case "2":
                viewInbox();
                break;
            case "3":
                viewUnreadEmails();
                break;
            case "4":
                viewSentEmails();
                break;
            case "5":
                currentUser.setJustLoggedIn(false);
                currentUser = null;
                System.out.println("Logged out.");
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private static void sendEmail() {
        System.out.print("Enter recipient emails (comma separated): ");
        String recipientsInput = scanner.nextLine();
        List<String> emails = Arrays.asList(recipientsInput.split(","));
        List<User> recipients = new java.util.ArrayList<>();

        for (String e : emails) {
            String email = e.trim().toLowerCase();
            User user = userDAO.findByEmail(email);
            if (user == null) {
                System.out.println("User not found: " + email);
            }
            recipients.add(user);
        }

        if (recipients.isEmpty()) {
            System.out.println("No valid recipients. Email was not sent.");
            return;
        }

        System.out.print("Enter subject: ");
        String subject = scanner.nextLine();

        System.out.print("Enter body: ");
        String body = scanner.nextLine();

        Email email = new Email();
        email.setSender(currentUser);
        email.setSubject(subject);
        email.setBody(body);

        try {
            emailDAO.save(email, recipients);
            System.out.println("Email sent successfully.");
        } catch (Exception ex) {
            System.out.println("Error sending email: " + ex.getMessage());
        }
    }

    private static void viewInbox() {
        List<Email> emails = emailDAO.findReceivedEmailsByUser(currentUser.getId());
        if (emails.isEmpty()) {
            System.out.println("Inbox is empty.");
            return;
        }
        System.out.println("=== Inbox ===");
        for (Email e : emails) {
            System.out.println("Code: " + e.getCode());
            System.out.println("From: " + e.getSender().getEmail());
            System.out.println("Subject: " + e.getSubject());
            System.out.println("Body: " + e.getBody());
            System.out.println("Date: " + e.getDate());

            System.out.println("--------------------------");
        }
        System.out.println("tools: ");
        System.out.println("1. Mark as Read an Email");
        System.out.println("2. Reply to Email");
        System.out.println("3. Forward the Email");
        System.out.println("4. Exit to Menu");
        System.out.print("Choose: ");
        int n = scanner.nextInt();
        scanner.nextLine();
        if (n == 0){
            return;
        }
        if (n == 1) {
            System.out.println("email's code:");
            String code = scanner.nextLine();

            EmailDAO emailDAO = new EmailDAO();
            Email email = emailDAO.findByCode(code);
            emailDAO.markAsRead(currentUser.getId(), email.getId());
        }
        switch (n){
            case 2:
                replyToEmail();
                break;
            case 3:
                forwardEmail();
                break;
        }
    }


    private static void viewSentEmails() {
        List<Email> emails = emailDAO.findSentEmailsByUser(currentUser.getId());
        if (emails.isEmpty()) {
            System.out.println("No sent emails.");
            return;
        }
        System.err.println("=== Sent Emails ===");
        for (Email e : emails) {
            System.out.println("Code: " + e.getCode());
            System.out.println("To: ");

            List<User> recipients = getRecipientsOfEmail(e.getId());
            for (User r : recipients) {
                System.out.println(" - " + r.getEmail());
            }
            System.out.println("Subject: " + e.getSubject());
            System.out.println("Body: " + e.getBody());
            System.out.println("Date: " + e.getDate());
            System.out.println("--------------------------");
        }
    }

    public static void viewUnreadEmails() {
        List<Email> emails = emailDAO.findUnreadEmailsByUser(currentUser.getId());
        if (emails.isEmpty()) {
            System.out.println("No UnRead emails.");
            return;
        }
        System.out.println("=== Unread Emails ===");
        for (Email e : emails) {
            Recipient recipient = new Recipient(currentUser, e);
            if (recipient == null) {
                continue;
            }
            if (!recipient.isRead()){
                System.out.println("Code: " + e.getCode());
                System.out.println("From: " + e.getSender().getEmail());
                System.out.println("Subject: " + e.getSubject());
                System.out.println("Body: " + e.getBody());
                System.out.println("Date: " + e.getDate());
                System.out.println("--------------------------");
            }
        }
        System.out.println("Choose: ");
        System.out.println("1. Mark as Read an Email");
        System.out.println("2. Reply to Email");
        System.out.println("3. Forward the Email");
        System.out.println("4. Exit to Menu");
        System.out.print("Choose: ");

        int n = scanner.nextInt();
        scanner.nextLine();
        if (n == 0){
            return;
        }
        if (n == 1) {
            System.out.println("email's code:");
            String code = scanner.nextLine();

            EmailDAO emailDAO = new EmailDAO();
            Email email = emailDAO.findByCode(code);
            emailDAO.markAsRead(currentUser.getId(), email.getId());
        }
        switch (n){
            case 2:
                replyToEmail();
                break;
            case 3:
                forwardEmail();
                break;
        }
    }

    private static void replyToEmail() {
        System.out.print("Enter the code of the email you want to reply to: ");
        String code = scanner.nextLine();
        System.out.print("Enter your reply message: ");
        String body = scanner.nextLine();

        try {
            emailDAO.replyToEmail(currentUser.getId(), code, body);
            System.out.println("Reply sent successfully.");
        } catch (Exception e) {
            System.out.println("Failed to send the reply.");
            e.printStackTrace();
        }
    }

    private static void forwardEmail() {
        System.out.print("Enter the code of the email you want to forward: ");
        String code = scanner.nextLine();
        System.out.print("How many recipients do you want to forward to? ");
        int count = Integer.parseInt(scanner.nextLine());

        List<User> newRecipients = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            System.out.print("Enter email of recipient " + (i + 1) + ": ");
            String email = scanner.nextLine().toLowerCase();
            User recipient = userDAO.findByEmail(email);
            if (recipient != null) {
                newRecipients.add(recipient);
            } else {
                System.out.println(" No user found with this email.");
            }
        }

        try {
            emailDAO.forwardEmail(currentUser.getId(), code, newRecipients);
            System.out.println("Email forwarded successfully.");
        } catch (Exception e) {
            System.out.println("Failed to forward the email.");
            e.printStackTrace();
        }
    }



    private static List<User> getRecipientsOfEmail(int emailId) {
        try (var session = util.HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("select r.user from Recipient r where r.email.id = :emailId", User.class);
            query.setParameter("emailId", emailId);
            return query.list();
        }
    }
}
