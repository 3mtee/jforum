package net.jforum.api.rest;

import net.jforum.Command;
import net.jforum.exceptions.APIException;
import net.jforum.util.preferences.TemplateKeys;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: emtee
 * @date: 5/23/12 2:13 PM
 */
public class RESTCommand extends Command {
    @Override
    public void list() {
        try {
            this.authenticate();
            // do nothing here
            // TODO: add implementation
        } catch (Exception e) {
            this.setTemplateName(TemplateKeys.API_ERROR);
            this.context.put("exception", e);
        }
    }

    /**
     * Tries to authenticate the user accessing the API
     *
     * @throws net.jforum.exceptions.APIException
     *          if the authentication fails
     */
    protected void authenticate() {
        final String apiKey = this.requiredRequestParameter("api_key");

        final RESTAuthentication auth = new RESTAuthentication();

        if (!auth.validateApiKey(apiKey)) {
            throw new APIException("The provided API authentication information is not valid");
        }
    }

    /**
     * Retrieves a parameter from the request and ensures it exists
     *
     * @param paramName the parameter name to retrieve its value
     * @return the parameter value
     * @throws APIException if the parameter is not found or its value is empty
     */
    protected String requiredRequestParameter(final String paramName) {
        final String value = this.request.getParameter(paramName);

        if (StringUtils.isBlank(value)) {
            throw new APIException("The parameter '" + paramName + "' was not found");
        }

        return value;
    }
}
