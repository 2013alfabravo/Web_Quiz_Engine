package engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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

    QuizController() {  }

    @PostMapping(path = "/api/quizzes")
    public Quiz addQuiz(@Valid @RequestBody Quiz quiz) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = ((QuizPrincipal)authentication.getPrincipal()).getUser();
        quiz.setUser(currentUser);
        return quizRepository.save(quiz);
    }

    @GetMapping(path = "api/quizzes/{id}")
    public Quiz getQuizById(@PathVariable Long id) {
        Optional<Quiz> quiz = quizRepository.findById(id);
        if (quiz.isPresent()) {
            return quiz.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found for id=" + id);
        }
    }

    @GetMapping(path = "api/quizzes")
    public Page<Quiz> getAllQuizzes(@RequestParam(required = false, defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return quizRepository.findAll(pageable);
    }

    @PostMapping(path = "api/quizzes/{id}/solve")
    public Result solveQuiz(@PathVariable Long id, @RequestBody Answer answer) {
        Quiz quiz = getQuizById(id);
        if (quiz == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found for id=" + id);
        }

        List<Integer> correctOptions = quiz.getAnswer();
        List<Integer> selectedOptions = answer.getAnswer();

        if (correctOptions.size() == selectedOptions.size() && correctOptions.containsAll(selectedOptions)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = ((QuizPrincipal)authentication.getPrincipal()).getUser();
            Completion completion = new Completion(quiz, user);
            completionRepository.save(completion);

            return Result.CORRECT;
        } else {
            return Result.FAIL;
        }
    }

    @PostMapping(path = "api/register")
    public void registerUser(@RequestBody RegistrationRequest request) {
        if (!request.getEmail().matches("(.+)@(.+\\..+)")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email: " + request.getEmail());
        }

        if (request.getPassword().length() < 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password should be at least 5 characters long");
        }

        Optional<User> user = userRepository.findByName(request.getEmail());
        if (user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User " + request.getEmail() + " is already registered");
        } else {
            User newUser = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()));
            userRepository.save(newUser);
        }
    }

    @Transactional
    @DeleteMapping(value = "api/quizzes/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        Optional<Quiz> quiz = quizRepository.findById(id);

        if (quiz.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = ((QuizPrincipal)authentication.getPrincipal()).getUser();

        if (user.getId().equals(quiz.get().getUser().getId())) {
            completionRepository.deleteAllByQuiz(quiz.get());
            quizRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping(value = "api/quizzes/completed")
    public Page<CompletedDTO> getCompletions(@RequestParam(required = false, defaultValue = "0") int page) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = ((QuizPrincipal)authentication.getPrincipal()).getUser();

        Pageable pageable = PageRequest.of(page, 10);
        return completionRepository.findAllByUserOrderByCompletedAtDesc(user.getName(), pageable).map(this::mapToCompletedDTO);
    }

    @GetMapping(value = "/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private CompletedDTO mapToCompletedDTO(Completion completion) {
        CompletedDTO completedDTO = new CompletedDTO();
        completedDTO.setId(completion.getQuiz().getId());
        completedDTO.setCompletedAt(completion.getCompletedAt());
        return completedDTO;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}