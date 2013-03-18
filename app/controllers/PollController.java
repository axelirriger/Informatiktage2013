package controllers;

import java.util.ArrayList;
import java.util.List;
import models.PollMongoEntity;
import models.PollMongoResultEntity;
import play.Logger;
import play.data.Form;
import static play.data.Form.*;
import play.libs.Akka;
import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Result;
import util.PollMongoBL;
import actors.PollActor;
import actors.messages.NewPollParticipantMessage;
import akka.actor.ActorRef;
import akka.actor.EmptyLocalActorRef;
import akka.actor.Props;
import forms.PollEntryForm;
import forms.PollForm;

public class PollController extends Controller {

	private static final String AKKA_POLL_LOOKUP_PREFIX = "/user/";
	private static final Form<PollForm> pollForm = play.data.Form
			.form(PollForm.class);
	private static final PollMongoBL pollMongoBL = new PollMongoBL();

	public static Result showPolls() {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollController.showPolls()");
		}

		long start = System.currentTimeMillis();
		final List<PollMongoEntity> polls = pollMongoBL.getAllPolls();
		long end = System.currentTimeMillis();
		if (Logger.isDebugEnabled()) {
			Logger.debug("PollController.showPolls: Loading in "
					+ (end - start) + " msec");
		}

		final Content html = views.html.polls.render(polls);
		final Result result = ok(views.html.pageframe.render("content", html));

		if (Logger.isDebugEnabled()) {
			Logger.debug("< PollController.showPolls()");
		}
		return result;
	}

	public static Result newPoll() {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollController.newPoll()");
		}

		long start = System.currentTimeMillis();
		final Content html = views.html.poll.render(pollForm);
		long end = System.currentTimeMillis();
		if (Logger.isDebugEnabled()) {
			Logger.debug("PollController.newPoll: Rendering in "
					+ (end - start) + " msec");
		}

		final Result result = ok(views.html.pageframe.render("content", html));

		if (Logger.isDebugEnabled()) {
			Logger.debug("< PollController.newPoll()");
		}
		return result;
	}

	public static Result submit() {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollController.submit()");
		}

		final String[] postAction = request().body().asFormUrlEncoded()
				.get("action");
		final String action = postAction[0];
		Result result = null;
		if ("addRow".equalsIgnoreCase(action)) {
			result = addNewOption();
		} else if (action.startsWith("optionsName_")) {
			final String[] splitStrings = action.split("_");
			final String indexString = splitStrings[splitStrings.length - 1];
			int index = Integer.parseInt(indexString);
			result = deleteOption(index);
		} else {
			result = submitPoll();
		}

		if (Logger.isDebugEnabled()) {
			Logger.debug("< PollController.submit()");
		}
		return result;
	}

	private static Result submitPoll() {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollController.submitPoll()");
		}

		final PollForm form = pollForm.bindFromRequest().get();
		if (Logger.isTraceEnabled()) {
			Logger.trace("Poll name: '" + form.pollName + "'");
			Logger.trace("Poll description: '" + form.pollDescription + "'");
			int optionSize = form.optionsName.size();
			for (int i = 0; i < optionSize; i++) {
				Logger.trace("Option " + i + " '" + form.optionsName.get(i)
						+ "'");
			}
		}
		createPollActor(form.pollName);
		Result res = null;
		if (pollMongoBL.loadPoll(form.pollName) == null) {
			final PollMongoEntity pollEntity = new PollMongoEntity();
			pollEntity.pollName = form.pollName;
			pollEntity.pollDescription = form.pollDescription;
			for (final String option : form.optionsName) {
				if (option != null && !option.equals("")) {
					pollEntity.optionsName.add(option);
				}
			}
			if (session().get("username") != null) {
				pollEntity.creator = session().get("username");
			}
			final long start = System.currentTimeMillis();
			pollMongoBL.savePoll(pollEntity);
			final long end = System.currentTimeMillis();
			if (Logger.isDebugEnabled()) {
				Logger.debug("PollController.submit: Saving in "
						+ (end - start) + " msec");
			}
			res = doPoll(pollEntity.pollName);
		} else {
			res = badRequest("Poll already exists");
		}

		if (Logger.isDebugEnabled()) {
			Logger.debug("< PollController.submitPoll()");
		}
		return res;
	}

	private static Result deleteOption(int index) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("PollController.deleteOption() with index " + index);
		}

		final PollForm form = pollForm.bindFromRequest().get();
		form.optionsName.remove(index);
		final Form<PollForm> newPollForm = pollForm.fill(form);
		final long start = System.currentTimeMillis();
		Content html = views.html.poll.render(newPollForm);
		final long end = System.currentTimeMillis();
		if (Logger.isDebugEnabled()) {
			Logger.debug("PollController.submit: Delete option in "
					+ (end - start) + " msec");
		}
		final Result result = ok(views.html.pageframe.render("content", html));

		if (Logger.isDebugEnabled()) {
			Logger.debug("PollController.deleteOption() with index " + index);
		}
		return result;
	}

	private static Result addNewOption() {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollController.addNewOption()");
		}
		
		final PollForm form = pollForm.bindFromRequest().get();
		form.optionsName.add("");
		final Form<PollForm> newPollForm = pollForm.fill(form);
		final long start = System.currentTimeMillis();
		Content html = views.html.poll.render(newPollForm);
		final long end = System.currentTimeMillis();
		if (Logger.isDebugEnabled()) {
			Logger.debug("PollController.submit: Add new Option in "
					+ (end - start) + " msec");
		}
		final Result result = ok(views.html.pageframe.render("content", html));
		
		if (Logger.isDebugEnabled()) {
			Logger.debug("< PollController.addNewOption()");
		}
		return result;
	}

	public static Result read(final String pollName) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollController.read(String)");
			if (Logger.isTraceEnabled()) {
				Logger.trace("Parameter: '" + pollName + "'");
			}
		}
		
		long start = System.currentTimeMillis();
		PollMongoEntity pollEntity = pollMongoBL.loadPoll(pollName);
		long end = System.currentTimeMillis();
		if (Logger.isDebugEnabled()) {
			Logger.debug("PollController.read: Loading in " + (end - start)
					+ " msec");
		}
		final PollForm pf = new PollForm();
		pf.pollName = pollEntity.pollName;
		pf.pollDescription = pollEntity.pollDescription;
		pf.optionsName = new ArrayList<String>(pollEntity.optionsName);
		start = System.currentTimeMillis();
		Content html = views.html.poll.render(pollForm.fill(pf));
		end = System.currentTimeMillis();
	
		if (Logger.isDebugEnabled()) {
			Logger.debug("PollController.read: Rendering in " + (end - start)
					+ " msec");
			Logger.debug("< PollController.read(String)");
		}
		return ok(html);
	}

	public static Result doPoll(String name) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollController.doPoll(String)");
			if (Logger.isTraceEnabled()) {
				Logger.trace("Parameter: '" + name + "'");
			}
		}
		
		Result res = null;
		long start = System.currentTimeMillis();
		final PollMongoEntity pollEntity = pollMongoBL.loadPoll(name);
		long end = System.currentTimeMillis();
		if (Logger.isDebugEnabled()) {
			Logger.debug("PollController.doPoll: Loading in " + (end - start)
					+ " msec");
		}
		
		if (pollEntity != null) {
			final Content html = views.html.doPoll.render(pollEntity,
					pollEntity.results,
					play.data.Form.form(PollEntryForm.class));
			res = ok(views.html.pageframe.render("content", html));
		} else {
			res = badRequest("Poll does not exist");
		}
		
		if (Logger.isDebugEnabled()) {
			Logger.debug("< PollController.doPoll(String)");
		}
		return res;
	}

	public static Result savePoll(String name) {
		if (Logger.isDebugEnabled()) {
			Logger.debug("> PollController.savePoll(String)");
		}
		
		final PollEntryForm pef = play.data.Form.form(PollEntryForm.class)
				.bindFromRequest().get();
		// TODO Better use a cache here or store an object
		pef.participant = session().get("username");
		pef.email = session().get("email");
		final PollMongoResultEntity pe = new PollMongoResultEntity();
		pe.participantName = pef.participant;
		pe.email = pef.email;
		pe.optionValues = new ArrayList<Boolean>(pef.optionValues);
		sendMessageToActor(name, pef.email);
		pollMongoBL.addEntryToPoll(name, pe);
		final Result res = doPoll(name);
		
		if (Logger.isDebugEnabled()) {
			Logger.debug("< PollController.savePoll(String)");
		}
		return res;
	}

	private static void createPollActor(final String name) {
		Props props = new Props(PollActor.class);
		Akka.system().actorOf(props, name);
	}

	private static void sendMessageToActor(final String pollName,
			final String email) {
		ActorRef ref = Akka.system().actorFor(
				AKKA_POLL_LOOKUP_PREFIX + pollName);
		if (!(ref instanceof EmptyLocalActorRef)) {
			final NewPollParticipantMessage pollMessage = new NewPollParticipantMessage();
			pollMessage.emailAddress = email;
			pollMessage.pollName = pollName;
			ref.tell(pollMessage);
		} else {
			// TODO What happens here? Can this happen?
		}
	}
}
