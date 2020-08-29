const Discord = require("discord.js");

module.exports = {
    // Info
    name: 'save',
    description: 'Get a json template of your help-desk',
    aliases: ['savesettings'],
    args: false,
    cooldown: 5,
    // Basic checks
    guildOnly: true,
    chooseDesk: true,
    // Command Category
    helpdesk: true,
    async execute(data, member, message, args, deskIndex) {
        const helpDesk = data.helpDesks[deskIndex];
        const hdChannel = await message.guild.channels.resolve(helpDesk.channelID);
        if(!hdChannel) {
            message.client.errorEmbed.setDescription('I could\'t find the help-desk channel, if it has been deleted run the \`hd?fix\` command.');
            return message.channel.send(message.client.errorEmbed);
        }
        const hdMessage = await hdChannel.messages.fetch(helpDesk.messageID);
        if(!hdMessage) {
            message.client.errorEmbed.setDescription('I could\'t find the help-desk message, if it has been deleted run the \`hd?fix\` command.');
            return message.channel.send(message.client.errorEmbed);
        }
        let json = hdMessage.embeds[0].toJSON();
        let jsonMessage = `\`\`\`json\n${JSON.stringify(json)}\n\`\`\`\nYou can save this text in a text file and re-upload it to the bot if you are gonna use those help-desk settings in the future or in another server via the \`hd?load\` command.`;
        await message.channel.send(jsonMessage)
    },
};