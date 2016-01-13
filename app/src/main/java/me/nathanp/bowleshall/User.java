package me.nathanp.bowleshall;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private boolean registered;
    private String lastOnline;
    private boolean online;

    public User() {}

    public User(String fn, String ln, String em) {
        firstName = fn;
        lastName = ln;
        email = em;
        registered = false;
        lastOnline = null;
        online = true;
    }

    public User(String fn, String ln, String em, boolean reg) {
        this(fn, ln, em);
        registered = reg;
    }

    public String getFirstName() {return firstName;}
    public String getLastName() {return lastName;}
    public String getEmail() {return email;}
    public boolean getRegistered() {return registered;}
    public String getLastOnline() {return lastOnline;}
    public boolean getOnline() {return online;}
}
