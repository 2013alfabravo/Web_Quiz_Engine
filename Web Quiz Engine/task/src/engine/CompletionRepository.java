package engine;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompletionRepository extends JpaRepository<Completion, Long> {

    @Query("SELECT a FROM Completion a WHERE a.user.name = :name ORDER BY a.completedAt DESC")
    Page<Completion> findAllByUserOrderByCompletedAtDesc(@Param("name") String name, Pageable pageable);

    void deleteAllByQuiz(Quiz quiz);
}
