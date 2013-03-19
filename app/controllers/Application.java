package controllers;

import play.mvc.Content;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

	public static Result index() {
		Content html = views.html.index.render("Welcome to myPoll!");
		return ok(views.html.pageframe.render("content", html));
	}

}