
package models;

import java.util.ArrayList;
import java.util.List;
import play.db.ebean.Model;

public class PollMongoResultEntity extends Model {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String participantName;
    public String email;
    public List<Boolean> optionValues = new ArrayList<Boolean>();
}
