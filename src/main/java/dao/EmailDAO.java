package dao;

import aut.ap.Email;
import aut.ap.Recipient;
import aut.ap.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class EmailDAO {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random random = new Random();

    public void save(Email email, List<User> recipients) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            String code = email.getCode();
            if (code == null || code.isEmpty()) {
                email.setCode(generateUniqueCode(session));
            }
            if (email.getDate() == null) {
                email.setDate(java.time.LocalDateTime.now());
            }
            session.save(email);
            for (User user : recipients) {
                Recipient r = new Recipient();
                r.setEmail(email);
                r.setUser(user);
                r.setRead(false);
                session.save(r);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
            }
            e.printStackTrace();
            throw e;
        }
    }

    public Email findByCode(String code) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Email> query = session.createQuery("from Email where code = :code", Email.class);
            query.setParameter("code", code);
            return query.uniqueResult();
        }
    }

    public List<Email> findReceivedEmailsByUser(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Email> query = session.createQuery("select r.email from Recipient r where r.user.id = :userId order by r.email.date desc", Email.class);
            query.setParameter("userId", userId);
            return query.list();
        }
    }

    public List<Email> findUnreadEmailsByUser(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Email> query = session.createQuery("select r.email from Recipient r where r.user.id = :userId and r.isRead = false order by r.email.date desc", Email.class);
            query.setParameter("userId", userId);
            return query.list();
        }
    }

    public List<Email> findSentEmailsByUser(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Email> query = session.createQuery("from Email e where e.sender.id = :userId order by e.date desc", Email.class);
            query.setParameter("userId", userId);
            return query.list();
        }
    }

    public void markAsRead(int userId, int emailId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query query = session.createQuery("update Recipient r set r.isRead = true where r.user.id = :userId and r.email.id = :emailId");
            query.setParameter("userId", userId);
            query.setParameter("emailId", emailId);
            query.executeUpdate();
            tx.commit();
            System.out.println("successfully marked as read");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void replyToEmail(int userId, String originalCode, String replyBody) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            //Email original = findByCode(originalCode);
            Email original = session.createQuery("from Email where code = :code", Email.class)
                    .setParameter("code", originalCode)
                    .uniqueResult();
            if (original == null) throw new RuntimeException("Original email not found");

            Query<Recipient> query = session.createQuery("from Recipient r where r.email.id = :emailId and r.user.id = :userId", Recipient.class);
            query.setParameter("emailId", original.getId());
            query.setParameter("userId", userId);

            boolean isRecipient = query.uniqueResult() != null;
            boolean isSender = original.getSender().getId() == userId;

            if (!isRecipient && !isSender) throw new RuntimeException("User not authorized to reply");

            Email reply = new Email();
            reply.setSender(session.get(User.class, userId));
            reply.setSubject("[Re] " + original.getSubject());
            reply.setBody(replyBody);
            reply.setCode(generateUniqueCode(session));
            reply.setDate(java.time.LocalDateTime.now());

            session.save(reply);

            List<User> replyRecipients = session.createQuery("select r.user from Recipient r where r.email.id = :emailId", User.class)
                    .setParameter("emailId", original.getId())
                    .list();
            if (!replyRecipients.contains(original.getSender())) {
                replyRecipients.add(original.getSender());
            }

            for (User u : replyRecipients) {
                Recipient r = new Recipient();
                r.setEmail(reply);
                r.setUser(u);
                r.setRead(false);
                session.save(r);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public void forwardEmail(int userId, String originalCode, List<User> newRecipients) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            //Email original = findByCode(originalCode);
            Email original = session.createQuery("from Email where code = :code", Email.class)
                    .setParameter("code", originalCode)
                    .uniqueResult();
            if (original == null)
                throw new RuntimeException("Original email not found");

            Query<Recipient> query = session.createQuery("from Recipient r where r.email.id = :emailId and r.user.id = :userId", Recipient.class);
            query.setParameter("emailId", original.getId());
            query.setParameter("userId", userId);

            boolean isRecipient = query.uniqueResult() != null;
            boolean isSender = original.getSender().getId() == userId;

            if (!isRecipient && !isSender)
                throw new RuntimeException("User not authorized to forward");

            Email forward = new Email();
            forward.setSender(session.get(User.class, userId));
            forward.setSubject("[Fw] " + original.getSubject());
            forward.setBody(original.getBody());
            forward.setCode(generateUniqueCode(session));
            forward.setDate(java.time.LocalDateTime.now());

            session.save(forward);

            for (User u : newRecipients) {
                Recipient r = new Recipient();
                r.setEmail(forward);
                r.setUser(u);
                r.setRead(false);
                session.save(r);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }


    private String generateUniqueCode(Session session) {
        String code;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
            code = sb.toString();
        } while (isCodeExists(session, code));
        return code;
    }

    private boolean isCodeExists(Session session, String code) {
        Query<Long> query = session.createQuery("select count(*) from Email e where e.code = :code", Long.class);
        query.setParameter("code", code);
        return query.uniqueResult() > 0;
    }
}
