# Create an #help-desk for your Discord!
This is a Discord Bot which is able to create an #help-desk for your server.  
Its code is open-source filled with comments to let you understand how it works.

## Useful Links
- [Invite Help Desk to your Server](https://discord.com/oauth2/authorize?client_id=739796627681837067&scope=bot&permissions=268954832)
- [Support Server](https://discord.gg/4BTXnXu)
- [Donate](https://www.patreon.com/giuliopime)

## Self Hosting
- Download the repository
- Install [NodeJS](https://nodejs.org/en/) (the LTS version)
- Install [mongoDB](https://www.mongodb.com/try/download/community)
- Install [Redis](https://redis.io/download)
- Make sure both services are running
- Create a [Discord Application](https://discord.com/developers/applications) + Bot
- Open the `config.json` file of this repository and replace `yourBotToken` with the Bot Token of the Discord Application you just created.
- Open a terminal inside the folder of this repository and run the following commands:  
  - `npm i`  
  - `node index.js`
- The terminal should out put the following:
```
Launched Help Desk Shard 0
Help Desk's Cache Loaded
Mongoose connection successfully opened!
Help Desk logged into Discord...
Help Desk launched!
```
- (Optional) You can install something like `pm2` to make your bot process run in the background so that you wont need your terminal open all the time:  
To do that hit CTRL-C to stop the current bot process, then do:
  - `npm i pm2 -g` (this installs pm2 globally)
  - `pm2 start index.js`  

**Here ya go! You now have your own bot up and running!**  
To invite it in your server you can use [this website](https://discordapi.com/permissions.html) where you can choose the permissions your bot will have when he'll join your server.  
Insert the Application Client ID inside the `Client ID` field of the website, you can get the Application Client ID from [the website of the 5th step of this guide](https://discord.com/developers/applications).  
Once you have done that you can simply use the link the website generated for you to invite the bot in your server!


 




