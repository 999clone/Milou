package aut.ap;

import jakarta.persistence.*;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Basic(optional = false)
    private String name;

    @Basic(optional = false)
    private String password;

    @Basic(optional = false)
    private String email;

    @Transient
    private boolean justLoggedIn = true;

    public User() {}

    public User(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public void setJustLoggedIn(boolean justLoggedIn) {
        this.justLoggedIn = justLoggedIn;
    }
    public boolean isJustLoggedIn() {
        return justLoggedIn;
    }

    public Integer getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        if (password.length() < 8) {
            System.out.println("weak password");
            return;
        }
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        if (!email.contains("@")) {
            this.email = email.toLowerCase() + "@milou.com";
        }else {
            this.email = email.toLowerCase();
        }
    }
}
