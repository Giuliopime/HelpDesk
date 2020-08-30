const Discord = require('discord.js');

module.exports = async (client, reaction, user) =>  {
    if(user.bot) return;
    // If the reaction is in the #welcome message
    if(reaction.message.id === '749384378177683506' && reaction.message.guild) {
        // Get the member who used the reaction
        const member = reaction.message.guild.members.resolve(user.id);
        // Add the "News" role to the member
        if(member && !member.roles.cache.has('749384562211029012')) await member.roles.add('749384562211029012');
    }

    // Help Desk Messages
    if(!reaction.message.guild) return;
    let fieldIndex = undefined;
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
    if(isNaN(fieldIndex)) return;

    // Channel cache
    const guild = reaction.message.guild;
    const guildID = guild.id;
    const hds = client.helpDesksCache;
    let hdChannels, data;
    const message = reaction.message;
    const member = guild.members.resolve(user.id);
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
    if(hdChannels.includes(message.channel.id)) {
        // Get data from the database
        if(!data) {
            data = await client.guildSchema.findOne({guildID: guildID});
            if(!data) {
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

        // Reply or assign the role
        const deskIndex = data.helpDesks.findIndex(hd => hd.channelID === message.channel.id);
        if(deskIndex > -1) {
            await reaction.users.remove(user);
            const helpDesk = data.helpDesks[deskIndex];
            if(fieldIndex === 9) {
                const roleToAssign = guild.roles.resolve(helpDesk.specialRole);
                if(roleToAssign) {
                    member.roles.add(roleToAssign.id)
                        .then(()=>{
                            let embed = new Discord.MessageEmbed().setDescription(`I assigned you the @${roleToAssign.name} role in ${guild.name}.`).setColor(client.mainColor);
                            user.send(embed)
                                .catch(()=>message.channel.send(`<@${user.id}> make sure your DMs are open.\n*If you don't know how check out this article <https://support.discord.com/hc/en-us/articles/217916488-Blocking-Privacy-Settings->*`)
                                    .then(msg => setTimeout(()=>msg.delete(), 15000)));
                        })
                        .catch(() => {
                            client.errorEmbed.setDescription(`I\'m unable to assign the <@&${roleToAssign.id}> role. Make sure I have \`Manage_Roles\` permissions and that the role I have to assign is under the Help Desk role in the server role hierarchy.\nLearn more with [this article](https://support.discord.com/hc/en-us/articles/214836687-Gestione-dei-Ruoli-101).`);
                            message.channel.send(client.errorEmbed)
                                .then(msg => setTimeout(() => msg.delete(), 200000))
                        })
                }
            }
            else {
                const reply = helpDesk.fieldsReplies[fieldIndex];
                if(!reply) return;
                let embed = new Discord.MessageEmbed().setDescription(reply).setColor(client.mainColor);
                user.send(embed)
                    .catch(() => message.channel.send(`<@${user.id}> make sure your DMs are open.\n*If you don't know how check out this article <https://support.discord.com/hc/en-us/articles/217916488-Blocking-Privacy-Settings->*`).then(msg => setTimeout(() => msg.delete(), 10000)));
            }
        }
    }
}