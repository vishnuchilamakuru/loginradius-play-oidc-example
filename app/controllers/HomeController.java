package controllers;

import com.google.inject.Inject;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.play.context.PlayContextFactory;
import org.pac4j.play.java.Secure;
import org.pac4j.play.store.PlayCacheSessionStore;
import play.mvc.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public Result index() {
        return ok(views.html.index.render());
    }

    @Secure(clients = "OidcClient")
    public Result oidcIndex(Http.Request req) {
        return protectedIndex(req);
    }

    public Result protectedIndex(Http.Request req) {
        return ok(views.html.protectedindex.render(getProfiles(req)));
    }

    @Inject
    private SessionStore sessionStore;

    private List<UserProfile> getProfiles(Http.Request req) {
        final ProfileManager profileManager = new ProfileManager(PlayContextFactory.INSTANCE.newContext(req), sessionStore);
        final List<UserProfile> profiles = new ArrayList<>();
        if (profileManager.getProfile().isPresent()) {
            profiles.add(profileManager.getProfile().get());
        }
        return profiles;
    }

}
