// Used to create embed messages
const { MessageEmbed } = require('discord.js');

module.exports = async (client, reaction, user) =>  {
    try {
        // Don't listen to bots or non-guild reactions
        if (user.bot || !reaction.message.guild) return;
        /*
        This part of code is used to assign the @News role in the Help Desk Support Server
        You can safely remove this commented code

        if (reaction.message.id === '749384378177683506' && reaction.message.guild) {
            // Get the member who used the reaction
            const member = reaction.message.guild.members.resolve(user.id);
            // Add the "News" role to the member
            if (member && !member.roles.cache.has('749384562211029012')) await member.roles.add('749384562211029012');
        }
        */

        // Check if the reaction is an option of the #help-desk
        let fieldIndex;
        switch (reaction.emoji.name) {
            case '1⃣':
                fieldIndex = 0;
                break;
            case '2⃣':
                fieldIndex = 1;
                break;
            case '3⃣':
                fieldIndex = 2;
                break;
            case '4⃣':
                fieldIndex = 3;
                break;
            case '5⃣':
                fieldIndex = 4;
                break;
            case '6⃣':
                fieldIndex = 5;
                break;
            case '7⃣':
                fieldIndex = 6;
                break;
            case '8⃣':
                fieldIndex = 7;
                break;
            case '9⃣':
                fieldIndex = 8;
                break;
            case '❓':
                fieldIndex = 9;
                break;
        }
        if (isNaN(fieldIndex)) return;

        // Check User Global Cooldown
        const isOnCooldown = await client.checkGCD(user.id);
        if(isOnCooldown) return;

        // Define some useful variables
        const guild = reaction.message.guild, guildID = guild.id;
        const message = reaction.message, member = guild.members.resolve(user.id);

        // Get data from the database
        const hds = client.helpDesksCache;
        let hdChannels, data;
        if (!hds.has(guildID)) {
            data = await client.guildSchema.findOne({guildID: guildID});
            if (!data) {
                // Create the new guild object for the database
                const newGuild = new client.guildSchema({
                    guildID: guildID,
                    helpDesks: [],
                });
                data = newGuild;
                // Save the object in the database
                await newGuild.save().catch(err => console.log(err));
            }
            hds.set(guildID, data.helpDesks.map(helpDesk => helpDesk.channelID));
        }
        const guildhds = hds.get(guildID);
        if (guildhds) hdChannels = guildhds;

        // Check if it's an Help Desk Channel
        if (hdChannels.includes(message.channel.id)) {
            // Check if the bot has all the required permissions in the #help-desk channel
            const requirePerms = ['ADD_REACTIONS', 'SEND_MESSAGES', 'EMBED_LINKS', 'ATTACH_FILES', 'MANAGE_MESSAGES', 'READ_MESSAGE_HISTORY', 'VIEW_CHANNEL', 'MENTION_EVERYONE'];
            if(!message.channel.permissionsFor(guild.me.id).has(requirePerms)) return message.channel.send(message.client.errorEmbed.setDescription(`I'm missing some important permissions to manage the help-desk>:\n**>** ${requirePerms.filter(permission => !message.channel.permissionsFor(message.guild.me.id).has(permission)).join('\n**>** ')}`)).catch();
            // Get data from the database
            if (!data) {
                data = await client.guildSchema.findOne({guildID: guildID});
                if (!data) {
                    data = {
                        guildID: guildID,
                        helpDesks: [],
                    }
                    // Create the new guild object for the database
                    const newGuild = new client.guildSchema(data);
                    // Save the object in the database
                    await newGuild.save().catch(err => console.log(err));
                }
            }

            // Interact with the user who used the #help-desk
            const deskIndex = data.helpDesks.findIndex(hd => hd.channelID === message.channel.id);
            if (deskIndex > -1) {
                // Remove the reaction
                await reaction.users.remove(user);

                const helpDesk = data.helpDesks[deskIndex];
                // If the user used the special question...
                if (fieldIndex === 9) {
                    // Get the role to assign
                    const roleToAssign = await guild.roles.resolve(helpDesk.specialRole);
                    if (roleToAssign) {
                        member.roles.add(roleToAssign.id)
                            .then(() => {
                                // Create the embed message to send the user
                                // If user's DMs are closed send a message in the #help-desk notifying the user
                                const embed = new MessageEmbed().setDescription(`I assigned you the @${roleToAssign.name} role in ${guild.name}.`).setColor(client.mainColor);
                                user.send(embed)
                                    .catch(() => message.channel.send(`<@${user.id}> make sure your DMs are open.\n*If you don't know how check out this article <https://support.discord.com/hc/en-us/articles/217916488-Blocking-Privacy-Settings->*`)
                                        .then(msg => setTimeout(() => msg.delete(), 15000)));
                            })
                            // If the bot can't assign the role report it in the #help-desk
                            .catch(() => message.channel.send(client.errorEmbed.setDescription(`I\'m unable to assign the <@&${roleToAssign.id}> role. If you are a moderator of the server make sure I have \`Manage_Roles\` permissions and that the role I have to assign is under the Help Desk role in the server role hierarchy.\nLearn more with [this article](https://support.discord.com/hc/en-us/articles/214836687-Gestione-dei-Ruoli-101).\n\nIf you are not a moderator of the server report this error to one of them.`)));
                    }
                }
                else {
                    // Get the reply to send to the user
                    const reply = helpDesk.fieldsReplies[fieldIndex];
                    if (!reply) return;
                    // Creater the embed message and send it to the user
                    const embed = new MessageEmbed().setDescription(reply).setColor(client.mainColor);
                    user.send(embed)
                        // If user's DMs are closed send a message in the #help-desk notifying the user
                        .catch(() => message.channel.send(`<@${user.id}> make sure your DMs are open.\n*If you don't know how check out this article <https://support.discord.com/hc/en-us/articles/217916488-Blocking-Privacy-Settings->*`).then(msg => setTimeout(() => msg.delete(), 10000)));
                }
            }
        }
    }
    catch (err) {
        const message = { content: 'Error from MessageReactionAdd event'}
        await client.errorReport(err, message, undefined);
    }
}