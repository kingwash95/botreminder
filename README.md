# botreminder
This is a telegram bot that sends user reminders for the time that the user has selected. This bot is still under development, but at the moment it has the function of creating user reminders, entering them into the database and sending a user reminder message to the date and time selected by the user. 
### Features

- Сreating user reminders with the command /add. After calling the command, the user needs to enter the date, time and reminder text according to the following pattern: 'dd-mm-yyyy XX: mm, text of the reminder'. 
After that, at the selected date and time, the bot will send the user a reminder text;
- Calling by the user a list of all reminders that were created by him. After calling the command /show, the bot sends a list of all user-created reminders that have not yet arrived. If the user does not have any reminders at the moment, then the bot will notify you with a corresponding message.
