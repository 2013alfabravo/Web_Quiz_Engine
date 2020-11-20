package engine;

import engine.data.*;
import engine.entities.Completion;
import engine.entities.Quiz;
import engine.entities.User;
import engine.repositories.CompletionRepository;
import engine.repositories.QuizRepository;
import engine.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@RestController
public class QuizController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CompletionRepository completionRepository;

    QuizController() {
    }

    @PostMapping(path = "/api/quizzes")
    public Quiz addQuiz(@Valid @RequestBody Quiz quiz) {
        User currentUser = getCurrentUser();
        quiz.setUser(currentUser);
        return quizRepository.save(quiz);
    }

    @GetMapping(path = "api/quizzes/{id}")
    public ResponseEntity<?> getQuizById(@PathVariable Long id) {
        Optional<Quiz> quiz = quizRepository.findById(id);

        if (quiz.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(quiz.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No quiz found for id=" + id);
        }
    }

    @GetMapping(path = "api/quizzes")
    public Page<Quiz> getAllQuizzes(@RequestParam(required = false, defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return quizRepository.findAll(pageable);
    }

    @PostMapping(path = "api/quizzes/{id}/solve")
    public ResponseEntity<?> solveQuiz(@PathVariable Long id, @RequestBody Answer answer) {
        Optional<Quiz> quiz = quizRepository.findById(id);

        if (quiz.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No quiz found for id=" + id);
        }

        List<Integer> correctOptions = quiz.get().getAnswer();
        List<Integer> selectedOptions = answer.getAnswer();

        if (correctOptions.size() == selectedOptions.size() && correctOptions.containsAll(selectedOptions)) {
            User user = getCurrentUser();
            Completion completion = new Completion(quiz.get(), user);
            completionRepository.save(completion);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(Result.CORRECT);
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Result.FAIL);
        }
    }

    @PostMapping(path = "api/register")
    public ResponseEntity<String> registerUser(@RequestBody RegistrationRequest request) {
        if (!request.getEmail().matches("(.+)@(.+\\..+)")) {
            return ResponseEntity.badRequest()
                    .body("Invalid email: " + request.getEmail());
        }

        if (request.getPassword().length() < 5) {
            return ResponseEntity.badRequest()
                    .body("Password should be at least 5 characters long");
        }

        Optional<User> user = userRepository.findByName(request.getEmail());
        if (user.isPresent()) {
            return ResponseEntity.badRequest()
                    .body("User " + request.getEmail() + " is already registered");
        } else {
            User newUser = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()));
            userRepository.save(newUser);
            return ResponseEntity.ok()
                    .body("You have been successfully registered");
        }
    }

    @Transactional
    @DeleteMapping(value = "api/quizzes/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        Optional<Quiz> quiz = quizRepository.findById(id);

        if (quiz.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No quiz found for id=" + id);
        }

        Long quizAuthorID = quiz.get().getUser().getId();
        Long currentUserID = getCurrentUser().getId();

        if (currentUserID.equals(quizAuthorID)) {
            completionRepository.deleteAllByQuiz(quiz.get());
            quizRepository.deleteById(id);

            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("Quiz was successfully deleted");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to delete this quiz");
        }
    }

    @GetMapping(value = "api/quizzes/completed")
    public Page<CompletionDTO> getCompletions(@RequestParam(required = false, defaultValue = "0") int page) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page, 10);

        return completionRepository.findAllByUserOrderByCompletedAtDesc(user.getName(), pageable).map(this::mapToCompletedDTO);
    }

    private User getCurrentUser() {
        return ((QuizPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUser();
    }

    private CompletionDTO mapToCompletedDTO(Completion completion) {
        CompletionDTO completionDTO = new CompletionDTO();
        completionDTO.setId(completion.getQuiz().getId());
        completionDTO.setCompletedAt(completion.getCompletedAt());

        return completionDTO;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}