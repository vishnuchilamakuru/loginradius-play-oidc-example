package modules;

import com.google.inject.AbstractModule;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.typesafe.config.Config;
import net.minidev.json.JSONObject;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.play.CallbackController;
import org.pac4j.play.LogoutController;
import org.pac4j.play.http.PlayHttpActionAdapter;
import org.pac4j.play.store.PlayCacheSessionStore;
import play.Environment;

import java.util.Optional;

public class SecurityModule extends AbstractModule {

    private final Config configuration;

    public SecurityModule(final Environment environment, final Config configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {

        bind(SessionStore.class).to(PlayCacheSessionStore.class);

        final OidcConfiguration oidcConfiguration = new OidcConfiguration();
        oidcConfiguration.setDiscoveryURI(configuration.getString("oidc.discoveryUri"));
        oidcConfiguration.setClientId(configuration.getString("oidc.clientId"));
        oidcConfiguration.setSecret(configuration.getString("oidc.clientSecret"));
        oidcConfiguration.setClientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);

        addProviderMetadata(oidcConfiguration);

        final OidcClient oidcClient = new OidcClient(oidcConfiguration);
        oidcClient.addAuthorizationGenerator((ctx, session, profile) -> {
            profile.addRole("ROLE_ADMIN");
            return Optional.of(profile);
        });

        final String baseUrl = configuration.getString("baseUrl");
        final Clients clients = new Clients(baseUrl + "/callback",  oidcClient);

        final org.pac4j.core.config.Config config = new org.pac4j.core.config.Config(clients);
        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
        config.setHttpActionAdapter(PlayHttpActionAdapter.INSTANCE);
        bind(org.pac4j.core.config.Config.class).toInstance(config);

        // callback
        final CallbackController callbackController = new CallbackController();
        callbackController.setDefaultUrl("/");
        bind(CallbackController.class).toInstance(callbackController);

        // logout
        final LogoutController logoutController = new LogoutController();
        logoutController.setDefaultUrl("/?defaulturlafterlogout");
        bind(LogoutController.class).toInstance(logoutController);

    }

    private void addProviderMetadata(OidcConfiguration oidcConfiguration) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.appendField("token_endpoint", configuration.getString("oidc.tokenUri"));
        final OIDCProviderMetadata providerMetaData;
        try {
            providerMetaData = OIDCProviderMetadata.parse(jsonObj);
            oidcConfiguration.setProviderMetadata(providerMetaData);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
