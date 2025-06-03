package aut.ap;

import jakarta.persistence.*;

@Entity
@Table(name = "recipients")
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipients_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "email_id")
    private Email email;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public Recipient() {}

    public Recipient(User user, Email email) {
        this.user = user;
        this.email = email;
        this.isRead = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
