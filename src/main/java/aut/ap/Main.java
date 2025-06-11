package aut.ap;

import dao.EmailDAO;
import dao.UserDAO;
import org.hibernate.query.Query;

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
        System.out.println("\n=== Menu for " + currentUser.getEmail() + " ===");
        System.out.println("1. Send Email");
        System.out.println("2. View Inbox");
        System.out.println("3. View Sent Emails");
        System.out.println("4. Logout");
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
                viewSentEmails();
                break;
            case "4":
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
                return;
            }
            recipients.add(user);
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
            System.out.println("Date: " + e.getDate());
            System.out.println("--------------------------");
        }
    }

    private static void viewSentEmails() {
        List<Email> emails = emailDAO.findSentEmailsByUser(currentUser.getId());
        if (emails.isEmpty()) {
            System.out.println("No sent emails.");
            return;
        }
        System.out.println("=== Sent Emails ===");
        for (Email e : emails) {
            System.out.println("Code: " + e.getCode());
            System.out.println("To: ");
            // نمایش گیرنده‌ها با پرس‌وجو جداگانه
            List<User> recipients = getRecipientsOfEmail(e.getId());
            for (User r : recipients) {
                System.out.println(" - " + r.getEmail());
            }
            System.out.println("Subject: " + e.getSubject());
            System.out.println("Date: " + e.getDate());
            System.out.println("--------------------------");
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
