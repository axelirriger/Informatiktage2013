
package actors;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import play.Logger;
import actors.messages.NewPollParticipantMessage;
import actors.messages.SendEmailMessage;
import akka.actor.UntypedActor;

public class EmailVersandActor extends UntypedActor {
	
    /*
     * (non-Javadoc)
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(final Object message) throws Exception {
        if (Logger.isDebugEnabled()) {
            Logger.debug("> onReceive(Object)");
        }
        
        if (message instanceof SendEmailMessage) {
            final SendEmailMessage msg = (SendEmailMessage) message;
            sendMessageToAll(msg.recipientList);
        } else {
            unhandled(message);
        }
        
        if (Logger.isDebugEnabled()) {
            Logger.debug("< onReceive(Object)");
        }
    }
    
    
    private void sendMessageToAll(final List<NewPollParticipantMessage> emailsList) {
        if (Logger.isDebugEnabled()) {
            Logger.debug("> sendMessageToAll()");
        }
        
        for (final NewPollParticipantMessage pollMsg : emailsList) {
            sendMessage(pollMsg);
        }
        
        if (Logger.isDebugEnabled()) {
            Logger.debug("< sendMessageToAll()");
        }
    }
    
    private void sendMessage(final NewPollParticipantMessage pollMsg) {
        if (Logger.isDebugEnabled()) {
            Logger.debug("> sendMessage(NewPollParticipantMessage)");
            if (Logger.isTraceEnabled()) {
                Logger.trace("Mail recipient: '" + pollMsg.emailAddress + "'");
            }
        }
        
        FileWriter writer = null;
        try {
            writer = new FileWriter(pollMsg.emailAddress + ".txt");
            writer.write(pollMsg.pollName);
        } catch (IOException e) {
            Logger.error(e.getMessage(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException e) {
                    Logger.error(e.getMessage(), e);
                }
            }
        }
        
        if (Logger.isDebugEnabled()) {
            Logger.debug("< sendMessage(NewPollParticipantMessage)");
        }
    }
}
