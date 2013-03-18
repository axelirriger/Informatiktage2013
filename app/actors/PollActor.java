
package actors;

import java.util.ArrayList;
import java.util.List;
import play.Logger;
import play.libs.Akka;
import actors.messages.NewPollParticipantMessage;
import actors.messages.SendEmailMessage;
import akka.actor.ActorRef;
import akka.actor.EmptyLocalActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class PollActor extends UntypedActor {
    private static final String AKKA_EMAIL_CREATION_PREFIX = "email_versand";
    private static final String AKKA_EMAIL_LOOKUP_PREFIX = "/user/email_versand";
    List<NewPollParticipantMessage> mailList = new ArrayList<NewPollParticipantMessage>();
    
    /*
     * (non-Javadoc)
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(final Object message) throws Exception {
        if (Logger.isDebugEnabled()) {
            Logger.debug("> onReceive(Object)");
        }
    
        if (message instanceof NewPollParticipantMessage) {
            sendMailToParticipants((NewPollParticipantMessage) message);
        } else {
            unhandled(message);
        }
        
        if (Logger.isDebugEnabled()) {
            Logger.debug("< onReceive(Object)");
        }
    }
    
    private void sendMailToParticipants(final NewPollParticipantMessage message) {
        if (Logger.isDebugEnabled()) {
            Logger.debug("> sendMailToParticipants(NewPollParticipantMessage)");
        }
        
        // TODO Ensure that you don't add the same participant twice?
        mailList.add(message);
        if (Logger.isDebugEnabled()) {
            Logger.debug("Poll " + message.pollName + " changed!");
            if (Logger.isTraceEnabled()) {
                for (final NewPollParticipantMessage msg : mailList) {
                    Logger.trace("Entry: '" + msg.emailAddress + "' -> '" + msg.pollName + "'");
                }
            }
        }

        final SendEmailMessage emailMsg = new SendEmailMessage();
        emailMsg.recipientList = this.mailList;
        ActorRef ref = lookupEmailVersandActor();
        ref.tell(emailMsg);
        
        if (Logger.isDebugEnabled()) {
            Logger.debug("< sendMailToParticipants(NewPollParticipantMessage)");
        }
    }
    
    /**
     * Looks up the central email delivery actor. Creates a new one if not already existing
     * @return
     */
    private ActorRef lookupEmailVersandActor() {
        if (Logger.isDebugEnabled()) {
            Logger.debug("> lookupEmailVersandActor()");
        }
    
        ActorRef ref = Akka.system().actorFor(AKKA_EMAIL_LOOKUP_PREFIX);
        if (ref instanceof EmptyLocalActorRef) {
            if (Logger.isDebugEnabled()) {
                Logger.debug("Creating new EmailVersandActor");
            }
            ref = Akka.system().actorOf(new Props(EmailVersandActor.class),
                AKKA_EMAIL_CREATION_PREFIX);
        }
        
        if (Logger.isDebugEnabled()) {
            Logger.debug("< lookupEmailVersandActor()");
        }
        return ref;
    }
}
