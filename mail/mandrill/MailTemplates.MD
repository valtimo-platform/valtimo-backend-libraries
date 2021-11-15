# Mandrill
Short guide how to use manidrill templates within cammunda processes

## Templates variables
The following variables are available to use within Mandrill:

- var (all variables available in process example var.naam )
- baseUrl (external url of application)

And within the context of a task:

- taskname
- link (link to access task or complete task)

And from the assignee

- firstname
- lastname
- email

You can configure default templates and Mandrill API key within the application.
For example:
```
jhipster:
     mandrill:
        apiKey: <api_key>
        notificationTemplate: bpc-task-notification
        completionTemplate: bpc-task-completion
```

# Default mandrill API's available:


# Task specific expression

> Note:
> You have to use these expression in the TaskListerner otherwise the task variable is not available. You could use it on Event Type "assignement"
> to send the notification to the assigned user. If you specify it on "create" then the notification will be send
> to all people how are able to complete the task "users who have the role specified in "Candidate groups"" SPAM ALERT


## Task notifcations
You can use the following expression within Camunda:

```
${mandrillMailer.sendNotification(task)}
```

This send out a mail to the assignee using the default Mandrill template.

```
${mandrillMailer.sendNotification(task,'CUSTOM_TEMPLATE')}
```

This send out a mail to the assignee using a custom specified Mandrill template.

``` 
${mandrillMailer.sendNotification(task, "niels.leemburg@ritense.com", "Niels", "Leemburg")}
```

This sends out a mail to the email provided within the method. This only send a
notification if the task is accessible publicly. This is done via the properties
panel within the Camunda Modeller. You can access and add properties in the 
properties tab. Add a property with name `public` and as value `true` to make it accessible publicly.




## Task completion mails

```
${mandrillMailer.sendCompletion(task)}
```

This send out a mail to the assignee using the default Mandrill template.

```
${mandrillMailer.sendCompletion(task,'CUSTOM_TEMPLATE')}
```

This send out a mail to the assignee using a custom specified Mandrill template.

## Normal mail

```
${mandrillMailer.send(execution,'john@ritense.com','Ron Alders','CUSTOM_TEMPLATE')}
```

To send a mandrill mail using a specified template


# Multi language
By default the application prefixes the templates with the language preference of the

For example:

```
${mandrillMailer.sendCompletion(task,'CUSTOM_TEMPLATE')}
```

If it sends a mail to u user with NL as default it will look for a Mandrill template calle "CUSTOM_TEMPLATE_nl"



# Auto assignment of Tasks
 The application assigns a human task automaticly to a user if there is only one person allowed to complete the task. In all other cases you have to
 assign the task to a person within the application manualy.




