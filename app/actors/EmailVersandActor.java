package actors;

import java.util.List;

import javax.mail.MessagingException;

import play.Logger;
import actors.messages.NewPollParticipantMessage;
import actors.messages.SendEmailMessage;
import akka.actor.UntypedActor;
import beans.EMailBean;

public class EmailVersandActor extends UntypedActor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	@Override
	public void onReceive(final Object message) throws Exception {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> EmailVersandActor.onReceive(Object)");
		}

		if (message instanceof SendEmailMessage) {
			final SendEmailMessage msg = (SendEmailMessage) message;
			sendMessageToAll(msg.recipientList);
		} else {
			unhandled(message);
		}

		if (Logger.isDebugEnabled()) {
			Logger.debug("< EmailVersandActor.onReceive(Object)");
		}
	}

	private void sendMessageToAll(
			final List<NewPollParticipantMessage> emailsList) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> EmailVersandActor.sendMessageToAll()");
		}

		for (final NewPollParticipantMessage pollMsg : emailsList) {
			sendMessage(pollMsg);
		}

		if (Logger.isDebugEnabled()) {
			Logger.debug("< EmailVersandActor.sendMessageToAll()");
		}
	}

	private void sendMessage(final NewPollParticipantMessage pollMsg) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> EmailVersandActor.sendMessage(NewPollParticipantMessage)");
			if (Logger.isTraceEnabled()) {
				Logger.trace("Mail recipient: '" + pollMsg.emailAddress + "'");
			}
		}

		final String from = "msg@localhost";
		final String recipient = pollMsg.emailAddress;
		final String subject = "Danke für Ihre Teilnahme";
		final String message = "Danke für die Teilnahme an der Umfrage "
				+ pollMsg.pollName;

		try {
			EMailBean.postMail(recipient, subject, message, from);
		} catch (MessagingException e) {
			Logger.error(e.getMessage(), e);
		}

		if (Logger.isDebugEnabled()) {
			Logger.debug("< EmailVersandActor.sendMessage(NewPollParticipantMessage)");
		}
	}
}
