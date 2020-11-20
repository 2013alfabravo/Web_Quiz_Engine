package engine;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Answer {
    private List<Integer> answer;

    Answer() {  }

    Answer(List<Integer> answer) {
        this.answer = answer;
    }

    public List<Integer> getAnswer() {
        return answer == null ? new ArrayList<>() : answer;
    }

    public void setAnswer(List<Integer> answer) {
        this.answer = answer;
    }
}
