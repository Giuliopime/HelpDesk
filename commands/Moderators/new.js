const Discord = require("discord.js");

module.exports = {
    // Info
    name: 'new',
    description: 'Create a new #help-desk channel template',
    aliases: ['newhd'],
    args: false,
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    // Command Category
    helpdesk: true,
    async execute(data, member, message) {
        const client = message.client;
        // Maximum amount of help desks reached
        if(data.helpDesks.length >= 5) {
            client.failureEmbed.setDescription('You already have 5 help desks. I can\'t handle more than that.\nIf you need to delete some simply delete their channel in Discord.');
            return message.channel.send(client.failureEmbed);
        }
        let hdChannel = await message.guild.channels.create('help-desk', {
            topic: 'Help Desk powered by the Official Help Desk Bot -> \'hd?help\' for info.',
            rateLimitPerUser: 5,
        })
        let hdEmbed = new Discord.MessageEmbed()
            .setTitle('[Title]')
            .setAuthor('[Author]', client.user.displayAvatarURL())
            .setThumbnail(client.user.displayAvatarURL())
            .setDescription('[Description]')
            .addFields(
                {name: '\u200b', value: '`1.` [Question example]'},
                    {name: '\u200b', value: '`2.` [Question 2 example]'}
            )
            .setImage(client.user.displayAvatarURL())
            .setFooter('[Footer]')
            .setColor(message.guild.me.displayHexColor);

        let hdMessage = await hdChannel.send(hdEmbed)
        const helpDesk = {
            channelID: hdChannel.id,
            messageID: hdMessage.id,
            // Embed properties
            embedProperties: {
                title: "[Title]",
                description: "[Description]",
                url: undefined,
                timestamp: null,
                color: message.guild.me.displayColor,
                fields: [
                    {name: '\u200b', value: '`1.` [Question example]', inline: false},
                    {name: '\u200b', value: '`2.` [Question 2 example]', inline: false},
                    {name: '\u200b', value: '`?` [Special question]', inline: false}
                ],
                thumbnail: {
                    url: client.user.displayAvatarURL(),
                },
                image: {
                    url: client.user.displayAvatarURL(),
                },
                author: {
                    name: "[Author]",
                    url: undefined,
                    icon_url: client.user.displayAvatarURL(),
                },
                footer: "[Footer]",
            },
            fieldsReplies: [
                "Simple answer",
                "Simple answer 2"
            ],
            specialAnswer: "You can make me assign roles with this special question",
            specialRole: undefined,
        }

        // Update database
        data.helpDesks.push(helpDesk);
        await client.guildSchema.updateOne({guildID: message.guild.id}, {'$push' : {'helpDesks' : helpDesk}});
        // Update bot help desks cache
        message.client.helpDesksCache.set(message.guild.id, data.helpDesks.map(helpDesk => helpDesk.channelID));
        // Reply to the command
        let replyEmbed = new Discord.MessageEmbed().setDescription(`I created a new <#${hdChannel.id}>.\nUse \`hd?tutorial\` to learn how to personalize it.`).setColor('#000');
        await message.channel.send(replyEmbed);
    },
};