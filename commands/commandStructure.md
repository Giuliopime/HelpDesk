Each command can have the following properties:  

- name
- description
- args: arguments required by the command (in RegExp)
- usage: example usage of the command
- aliases
- cooldown
- chooseDesk: whether the user needs to choose an #help-desk to run the command
- helpdesk: command category
- embed: command category
- utility: command category
- perms: bot permissions required in the channel where the command is sent
- globalPerms: bot permissions required in the server settings

The code inside `execute:` often has these sections:
- Various checks  
Like arguments' length, for example:
```js
// Embed author can't be over 256 characters
if(text.length > 256)
    return message.channel.send(message.client.errorEmbed.setDescription('Discord allows up to 256 characters for author names.\nCheck all embed limits on [this link](https://discord.com/developers/docs/resources/channel#embed-limits).'))
```
- Database & cache update  
Updates a value in the database and invalidates the cache for the guild settings, example:
```js
await message.client.guildSchema.updateOne({guildID: message.guild.id},  { $set:{ ['helpDesks.' + index + '.embedProperties.author.name']: text } });
await message.client.caches.hdel('settings', message.guild.id); 
```
- Message Reply, which always uses the client embeds created in `bot.js`, example:
```js
message.client.replyEmbed.setDescription('Author set.\nUse `hd?update` to apply the changes to the #help-desk embed.');
await message.channel.send(message.client.replyEmbed);
```