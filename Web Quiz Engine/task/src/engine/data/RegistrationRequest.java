package engine.data;

import javax.validation.constraints.NotEmpty;

@SuppressWarnings("unused")
public class RegistrationRequest {
    @NotEmpty
    private String email;
    @NotEmpty
    private String password;

    RegistrationRequest() { }

    RegistrationRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
