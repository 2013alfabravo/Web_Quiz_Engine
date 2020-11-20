package engine.repositories;

import engine.entities.Completion;
import engine.entities.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompletionRepository extends JpaRepository<Completion, Long> {

    @Query("SELECT a FROM Completion a WHERE a.user.id = :id ORDER BY a.completedAt DESC")
    Page<Completion> findAllByUserIdOrderByCompletedAtDesc(@Param("id") Long id, Pageable pageable);

    void deleteAllByQuiz(Quiz quiz);
}
