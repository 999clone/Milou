package dao;

import aut.ap.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

public class UserDAO {

    public void save(User user) {
        Transaction tx = null;
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.save(user);
            tx.commit();
        }catch(Exception e) {
            if(tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    public User findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("from User where email = :email", User.class);
            query.setParameter("email", email.toLowerCase());
            return query.uniqueResult();
        }
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }

    public User login(String email, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("from User where email = :email and password = :password", User.class);
            query.setParameter("email", email.toLowerCase());
            query.setParameter("password", password);
            return query.uniqueResult();
        }
    }
}
