package net.jforum.api.rest;

import freemarker.template.SimpleHash;
import freemarker.template.Template;
import net.jforum.JForumExecutionContext;
import net.jforum.SessionFacade;
import net.jforum.api.rest.RESTCommand;
import net.jforum.context.RequestContext;
import net.jforum.context.ResponseContext;
import net.jforum.dao.DataAccessDriver;
import net.jforum.dao.PrivateMessageDAO;
import net.jforum.dao.UserDAO;
import net.jforum.entities.Post;
import net.jforum.entities.PrivateMessage;
import net.jforum.entities.PrivateMessageType;
import net.jforum.entities.User;
import net.jforum.entities.UserSession;
import net.jforum.util.I18n;
import net.jforum.util.concurrent.Executor;
import net.jforum.util.mail.EmailSenderTask;
import net.jforum.util.mail.PrivateMessageSpammer;
import net.jforum.util.preferences.ConfigKeys;
import net.jforum.util.preferences.SystemGlobals;
import net.jforum.util.preferences.TemplateKeys;
import net.jforum.view.forum.common.AttachmentCommon;
import net.jforum.view.forum.common.PostCommon;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author: emtee
 * @date: 5/23/12 2:12 PM
 */
public class PrivateMessageREST extends RESTCommand {
    public void count() {
        try {
            this.authenticate();

            final String email = this.requiredRequestParameter("email");
            final String messageType = this.requiredRequestParameter("messageType");
            final DataAccessDriver dataAccessDriver = DataAccessDriver.getInstance();
            final UserDAO userDAO = dataAccessDriver.newUserDAO();
            final PrivateMessageDAO privateMessageDAO = dataAccessDriver.newPrivateMessageDAO();

            final User user = userDAO.findByEmail(email);

            int messagesCount = 0;
            if (messageType.equals(PrivateMessageType.INBOX_UNREAD)) {
                messagesCount = privateMessageDAO.inboxUnreadCount(user);
            } else if (messageType.equals(PrivateMessageType.INBOX_TOTAL)) {
                messagesCount = privateMessageDAO.inboxTotalCount(user);
            }
            this.setTemplateName(TemplateKeys.API_MESSAGES_COUNT);
            this.context.put("messagesCount", messagesCount);

        } catch (Exception e) {
            this.setTemplateName(TemplateKeys.API_ERROR);
            this.context.put("exception", e);
        }
    }

    public void writeMessage() {
        try {
            this.authenticate();

            UserDAO userDao = DataAccessDriver.getInstance().newUserDAO();

            String toUsername = this.request.getParameter("toUsername");

            List<User> users = null;

            List<String> userNames = new LinkedList<String>();
            final StringTokenizer tokenizer = new StringTokenizer(toUsername, ",");
            while (tokenizer.hasMoreTokens()) {
                final String token = tokenizer.nextToken().trim();
                userNames.add(token);
            }

            if (userNames.isEmpty()) {
                users = userDao.findByName(toUsername, true);
            } else {
                users = userDao.findByNames(userNames, true);
            }

            // We failed to get the user id?
            if (users == null || users.isEmpty()) {
                this.setTemplateName(TemplateKeys.PM_SENDSAVE_USER_NOTFOUND);
                this.context.put("message", I18n.getMessage("PrivateMessage.userIdNotFound"));
                return;
            }

            final Post post = PostCommon.fillPostFromRequest();
            AttachmentCommon attachments = new AttachmentCommon(this.request, 0);
            attachments.preProcess();
            final String email = requiredRequestParameter("email");
            User fromUser = userDao.findByEmail(email);

            for (User user : users) {
                PrivateMessage pm = new PrivateMessage();
                pm.setPost(post);

                // Sender

                pm.setFromUser(fromUser);

                pm.setToUser(user);

                DataAccessDriver.getInstance().newPrivateMessageDAO().send(pm);
                pm.setHasAttachments(!attachments.getPMAttachments(pm.getId(), 0).isEmpty());

                // If the target user if in the forum, then increments its
                // private message count
                final int userId = user.getId();
                String sid = SessionFacade.isUserInSession(userId);
                if (sid != null) {
                    UserSession us = SessionFacade.getUserSession(sid);
                    us.setPrivateMessages(us.getPrivateMessages() + 1);
                }

                if (user.getEmail() != null && user.getEmail().trim().length() > 0 && SystemGlobals.getBoolValue(ConfigKeys.MAIL_NOTIFY_ANSWERS)) {
                    Executor.execute(new EmailSenderTask(new PrivateMessageSpammer(user)));
                }
                attachments.insertAttachments(pm.getId());
            }

            this.setTemplateName(TemplateKeys.API_MESSAGE_SENT);
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
