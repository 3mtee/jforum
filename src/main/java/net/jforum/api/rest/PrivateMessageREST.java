package net.jforum.api.rest;

import freemarker.template.SimpleHash;
import freemarker.template.Template;
import net.jforum.JForumExecutionContext;
import net.jforum.context.RequestContext;
import net.jforum.context.ResponseContext;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.PrivateMessageDAO;
import net.jforum.dao.UserDAO;
import net.jforum.entities.PrivateMessage;
import net.jforum.entities.User;
import net.jforum.util.preferences.TemplateKeys;

import java.util.List;

/**
 * @author: emtee
 * @date: 5/23/12 2:12 PM
 */
public class PrivateMessageREST extends RESTCommand {
    public void count() {
        try {
            this.authenticate();

            final String email = this.requiredRequestParameter("email");
            final DataAccessDriver dataAccessDriver = DataAccessDriver.getInstance();
            final UserDAO userDAO = dataAccessDriver.newUserDAO();
            final PrivateMessageDAO privateMessageDAO = dataAccessDriver.newPrivateMessageDAO();

            final User user = userDAO.findByEmail(email);

            final List<PrivateMessage> privateMessages = privateMessageDAO.selectFromInbox(user);
            this.setTemplateName(TemplateKeys.API_MESSAGES_COUNT);
            this.context.put("messagesCount", privateMessages.size());

        } catch (Exception e) {
            this.setTemplateName(TemplateKeys.API_ERROR);
            this.context.put("exception", e);
        }
    }

    public Template process(final RequestContext request, final ResponseContext response, final SimpleHash context) {
        JForumExecutionContext.setContentType("text/xml");
        return super.process(request, response, context);
    }
}
