
package models;

import java.util.ArrayList;
import java.util.List;
import play.db.ebean.Model;
import forms.RegisterLoginForm;

public class UserMongoEntity extends Model {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String username;
    public String password;
    public String email;
    public List<String> completedPolls = new ArrayList<String>();
    public UserMongoEntity() {
    }
    public UserMongoEntity(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
    public UserMongoEntity(RegisterLoginForm userForm) {
        if (userForm != null) {
            this.username = userForm.username;
            this.password = userForm.password;
            this.email = userForm.email;
        }
    }
}
