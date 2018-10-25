package pl.rosa.mapeditor.viewmodels;

import de.malkusch.validation.constraints.EqualProperties;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@EqualProperties(value = {"password","repeatPassword"}, message = "{Passwords.dontMatch}")
public class RegistrationViewModel {

    @NotEmpty(message = "{NotEmpty.message}")
    @Email(message = "{Email.invalidEmail}")
    private String email;

    @NotEmpty(message = "{NotEmpty.message}")
    private String name;

    @NotEmpty(message = "{NotEmpty.message}")
    private String password;
    private String repeatPassword;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }


}
