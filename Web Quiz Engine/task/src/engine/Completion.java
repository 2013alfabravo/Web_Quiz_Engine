package engine;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Completion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column
    private LocalDateTime completedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn()
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    @ManyToOne
    @JoinColumn()
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Quiz quiz;

    Completion() {  }

    Completion(Quiz quiz, User user) {
        this.quiz = quiz;
        this.user = user;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
