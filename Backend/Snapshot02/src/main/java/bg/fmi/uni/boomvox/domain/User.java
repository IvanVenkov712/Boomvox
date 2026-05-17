package bg.fmi.uni.boomvox.domain;
import bg.fmi.uni.boomvox.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "user")
public class User {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Column(name = "username", nullable = false, length = 20, unique = true)
    private String username;

    @Getter
    @Column(name = "email", nullable = false, length = 50, unique = true)
    private String email;

    @Getter
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Getter
    @Column(name = "first_name", nullable = false, length = 20)
    private String firstName;

    @Getter
    @Column(name = "last_name", nullable = false, length = 20)
    private String lastName;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Getter
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "favourites_list_id")
    private FavouritesList favouritesList;

    @Version
    private long version;

    public User(String username, String email, String passwordHash, String firstName, String lastName, UserRole role, FavouritesList favouritesList) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.favouritesList = favouritesList;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", username='" + username + '\'' +
            ", email='" + email + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", role=" + role + '\'' +
            ", favourites=" + favouritesList +
            '}';
    }
}
