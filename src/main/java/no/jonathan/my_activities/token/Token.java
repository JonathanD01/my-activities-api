package no.jonathan.my_activities.token;

import jakarta.persistence.*;
import lombok.*;
import no.jonathan.my_activities.user.User;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tokens")
@ToString
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String token;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime validatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @ToString.Exclude
    private User user;
}
